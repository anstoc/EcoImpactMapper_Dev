package cumimpactsa;

import java.io.File;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Calculates impact in each cell for each stressor-ecosystem component combination. 
 * @author andy
 */
public class IndividualImpactMapper 
{
    private SensitivityScoreSet scores;
    private ArrayList<IndividualImpact> impactList;
    
    public void calculateIndividualImpacts(SensitivityScoreSet sensitivityScores, boolean avg)  //avg: if true, divides by nr of ecocomps in each cell
    {

        impactList=new ArrayList<IndividualImpact>();
        //needed to calculate average and impact contributions
        DiversityIndex divIndex = new DiversityIndex("not_saved");
        float[][] ecocompSum = divIndex.getGrid().getData();
        
        for(int i=0; i<sensitivityScores.size();i++)
        {
            GlobalResources.mappingProject.setProcessingProgressPercent((int) (100*i/sensitivityScores.size()));
            
            ImpactInfo impact = sensitivityScores.getInfo(i);
            
            float score = (float) impact.getSensitivityScore();
            float[][] ecocompData=impact.getEcocomp().getProcessedGrid().getData();
            float[][] stressorData=impact.getStressor().getProcessedGrid().getData();
            float z=0;
            for(int x=0;x<ecocompData.length;x++)
            {
                for(int y=0; y<ecocompData[0].length; y++)
                {
                    if(stressorData[x][y]!=GlobalResources.NODATAVALUE && ecocompData[x][y]!=GlobalResources.NODATAVALUE && ecocompData[x][y]!=0)
                    {
                        float summand=score*ecocompData[x][y]*stressorData[x][y];
                        if(avg)
                        {
                            summand=summand/ecocompSum[x][y];
                        }
                        if(!Double.isNaN(summand))
                        {
                           impactList.add(new IndividualImpact(x,y,impact,summand));
                        }
                    }
                }
            }
        }
    }

    void writeToFile(File selectedFile) 
    {
       CsvTableGeneral table = new CsvTableGeneral();
       table.addColumn("x");
       table.addColumn("y");
       table.addColumn("stressor");
       table.addColumn("ecocomp");
       table.addColumn("impact");
            
        for(int i=0;i<impactList.size();i++)
        {
           IndividualImpact ii = impactList.get(i);
           Point2D worldCoords=GlobalResources.mappingProject.grid.getWorldCoords(new Point2DInt(ii.x,ii.y));
           ArrayList<String> row = new ArrayList<String>();
           row.add(worldCoords.x+"");
           row.add(worldCoords.y+"");
           row.add(ii.impactInfo.getStressor().getName());
           row.add(ii.impactInfo.getEcocomp().getName());
           row.add(ii.impact+"");
           table.addRow(row);
        }    
        table.writeToFile(selectedFile.getAbsolutePath());

    }
   
}
