/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author ast
 */
public class ImpactIndexDiminishing extends SpatialDataLayer 
{
     private SensitivityScoreSet scores;
    
     public ImpactIndexDiminishing(String saveFileName, SensitivityScoreSet sensitivityScores, boolean avg)  //avg: if true, divides by nr of ecocomps in each cell
     {
         
        super("Impact index",null,GlobalResources.DATATYPE_SPATIAL,null);
        
        this.scores=sensitivityScores;
        source = new DataSourceInfo();
        source.sourceFile=saveFileName;
        source.xField="x";
        source.yField="y";
        source.valueField="value";
        this.name=GlobalResources.getDateTime() + " Impact Index (antagonistic)";
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
                        ArrayList<Double> summands = new ArrayList<Double>();
                        int nonZeroImpacts=0;
                                
                        for(int i=0; i<eImpacts.size(); i++)
                        {
                            ImpactInfo impact = eImpacts.get(i);
                            double summand = impact.getSensitivityScore() * impact.getResponseFunction().getResponse(impact.getStressor().getProcessedGrid().getData()[x][y]);

                            if(avg) {summand=summand/ecocompSum[x][y];}
                            if(Double.isNaN(summand)) {summand=0;}
                            if(summand>0) {nonZeroImpacts++;}
                            eImpacts.get(i).sortField=summand;
                        }
                        
                        //sort impacts
                        Collections.sort(eImpacts,new ImpactComparator());
                
                        //go through impacts, largest to smallest, weight according to number
                        for(int i=0;i<nonZeroImpacts;i++)
                        {
                            double weight = (nonZeroImpacts-i)/((double) nonZeroImpacts);
                            double weightedSummand=weight*eImpacts.get(i).sortField;
                            values[x][y]+=weightedSummand;
                             eImpacts.get(i).addToContribution(weightedSummand);
                            eImpacts.get(i).sortField=0;
                        }
                          if(values[x][y]>max) {max=values[x][y];}
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
