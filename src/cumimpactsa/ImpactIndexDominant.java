/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.util.ArrayList;

/**
 *
 * @author ast
 */
public class ImpactIndexDominant  extends SpatialDataLayer
{

     private SensitivityScoreSet scores;
    
     public ImpactIndexDominant(String saveFileName, SensitivityScoreSet sensitivityScores, boolean avg)  //avg: if true, divides by nr of ecocomps in each cell
     {
         
        super("Impact index",null,GlobalResources.DATATYPE_SPATIAL,null);
        
        this.scores=sensitivityScores;
        source = new DataSourceInfo();
        source.sourceFile=saveFileName;
        source.xField="x";
        source.yField="y";
        source.valueField="value";
        this.name=GlobalResources.getDateTime() + " Impact Index (dominant)";
        if(avg) {this.name = this.name + " (mean)";}
        else {this.name = this.name + " (sum)";}
        
        this.type = GlobalResources.DATATYPE_SPATIAL;        
                
         //set all ecocomp-stressor contributions to 0
        for(int i=0; i<scores.size();i++)
        {
            scores.getInfo(i).setContribution(0);
        }
        
               //own value grid for each ecocomp
        float[][] ecocompSum;
        if(!avg)
        {
            ecocompSum = new float[GlobalResources.mappingProject.grid.getDimensions().x][GlobalResources.mappingProject.grid.getDimensions().y];
        }
        else
        {
            DiversityIndex divIndex = new DiversityIndex("not_saved");  //TODO if thr siulation doesn't change ecocomps, calculate only once as a static variable
            ecocompSum = divIndex.getGrid().getData();
        }
        
        float[][] values = GlobalResources.mappingProject.grid.getEmptyGrid();
        
        float max=0;
   
        ArrayList<SpatialDataLayer> ecocomps = GlobalResources.mappingProject.ecocomps;
        ArrayList<SpatialDataLayer> stressors = GlobalResources.mappingProject.stressors;
        //separate calculations for each ecocomp
        for(int e=0; e<ecocomps.size();e++)
        {
            GlobalResources.mappingProject.setProcessingProgressPercent((int) (100*e/ecocomps.size()));
            //get impacts only for this ecocomp
            ArrayList<ImpactInfo> eImpacts = scores.getActiveImpactsForEcocomp(ecocomps.get(e).getName());
            float[][] ecocomp = ecocomps.get(e).getGrid().getData();
            //now go through the grid cell by cell... for each cell, find most important stressor, that's the summand, divide by # of ecocomps if need be, add to result
            for(int x=0; x<GlobalResources.mappingProject.grid.getDimensions().x;x++)
            {
                for(int y=0; y<GlobalResources.mappingProject.grid.getDimensions().y;y++)
                {
                    if(values[x][y]!=GlobalResources.NODATAVALUE && ecocomp[x][y]>0)  //only for present ecocomps
                    {
                        float largestSummand = -1;
                        int largestIndex=-1;
                        
                        for(int i=0; i<eImpacts.size(); i++)
                        {
                            ImpactInfo impact = eImpacts.get(i);
                            float summand = impact.getSensitivityScore() * impact.getResponseFunction().getResponse(impact.getStressor().getProcessedGrid().getData()[x][y]);

                            if(avg) {summand=summand/ecocompSum[x][y];}
                            if(Double.isNaN(summand)) {summand=0;}
                            
                            if(summand > largestSummand)
                            {
                                largestSummand = summand;
                                largestIndex = i;
                            }
                        }
                        
                        values[x][y] = values[x][y] + largestSummand;
                        if(values[x][y]>max) {max=values[x][y];}
                        eImpacts.get(largestIndex).addToContribution(largestSummand);
                    }
                }
            }
            
        }
        
       grid = new DataGrid(values,max,0,GlobalResources.NODATAVALUE);
    
    }
     
     
    public SensitivityScoreSet getScores()
    {
        return scores;
    }
     
}