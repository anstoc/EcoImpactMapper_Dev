/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.util.Collections;

/**
 *
 * @author ast
 */
public class ImpactIndexMaxEcocomp extends SpatialDataLayer 
{
    SensitivityScoreSet scores;
    final public static int ADDITIVE = 0;
    final public static int DOMINANT = 1;
    final public static int ANTAGONISTIC = 2;
    
     public ImpactIndexMaxEcocomp(String saveFileName, SensitivityScoreSet sensitivityScores, int type)  
     {
     
        super("Impact index",null,GlobalResources.DATATYPE_SPATIAL,null);
        
        this.scores=sensitivityScores;
        source = new DataSourceInfo();
        source.sourceFile=saveFileName;
        source.xField="x";
        source.yField="y";
        source.valueField="value";
        this.name=GlobalResources.getDateTime() + " Impact Index ";
        if(type==ADDITIVE) {this.name = this.name + "(additive";}
        else if(type==DOMINANT) {this.name = this.name+ "(dominant";}
        else {this.name = this.name + "(antagonistic";}
        this.name=this.name+", max)";
        
        this.type = GlobalResources.DATATYPE_SPATIAL;
        
        //needed to calculate average and impact contributions
              
         //set all ecocomp-stressor contributions to 0
        for(int i=0; i<scores.size();i++)
        {
            scores.getInfo(i).setContribution(0);
        }
        
        //now create internal data grid
        float[][] data = GlobalResources.mappingProject.grid.getEmptyGrid();
        //double[][] ecocompSum = new double[GlobalResources.mappingProject.grid.getDimensions().x][GlobalResources.mappingProject.grid.getDimensions().y];;
        
        float max=0;
        float min=999;
        //create a sensitivity score set for each ecosystem component, and create an impact index 
        //for each; then replace all values in the data grid with the new one if it's larger
        String[] ecocompNames=GlobalResources.mappingProject.getEcocompNames();
        //set list of scores for all individual ecocomps up beforehand
        SensitivityScoreSet[] ecocompScoresList = new SensitivityScoreSet[ecocompNames.length];
        for(int i=0; i<ecocompScoresList.length;i++)
        {
            ecocompScoresList[i]=scores.getSubsetForEcocomp(ecocompNames[i],GlobalResources.mappingProject.stressors,GlobalResources.mappingProject.ecocomps);
        }
        
        for(int x=0;x<data.length;x++)
        {
           GlobalResources.mappingProject.setProcessingProgressPercent((int) (100*x/data.length));
           for(int y=0; y<data[0].length; y++)
           {
               if(data[x][y]!=GlobalResources.NODATAVALUE)
               {
                    SensitivityScoreSet maxScoreSet=null;
                    for(int i=0;i<ecocompNames.length;i++)
                    {
                        float sum=0;
                        int contributionIndex=0;
                        int nonZeroAntImpacts=0;
                        SensitivityScoreSet ecocompScores=ecocompScoresList[i];
                        ecocompScores.resetContributions();
                        
                         for(int j=0; j<ecocompScores.getAllScores().size(); j++)
                         {
                            ImpactInfo impact=ecocompScores.getAllScores().get(j);
                            float score = (float) impact.getSensitivityScore();
                            float[][] ecocompData=impact.getEcocomp().getProcessedGrid().getData();
                            float[][] stressorData=impact.getStressor().getProcessedGrid().getData();

                            if(stressorData[x][y]!=GlobalResources.NODATAVALUE || ecocompData[x][y]!=GlobalResources.NODATAVALUE)
                            {
                                float summand=score*ecocompData[x][y]*stressorData[x][y];
                                if(type==ADDITIVE || type==ANTAGONISTIC)
                                {    
                                    sum=sum+summand;
                                    impact.setContribution(summand);
                                    impact.sortField=summand;
                                    if(summand>0) nonZeroAntImpacts++;
                                }
                                else if(type==DOMINANT)
                                {
                                    if(summand>sum) 
                                    {
                                        sum=summand;
                                        contributionIndex=j;
                                    }
                                }
                            }          
                         }
                         if(type==DOMINANT)
                         {
                             ecocompScores.getAllScores().get(contributionIndex).setContribution(sum);
                         }
                         if(type==ANTAGONISTIC)
                         {
                             sum=0;
                             Collections.sort(ecocompScores.getAllScores(), new ImpactComparator());
                             for(int l=0;l<nonZeroAntImpacts;l++)
                             {
                                double weight = (nonZeroAntImpacts-l)/((double) nonZeroAntImpacts);
                                double weightedSummand=weight*ecocompScores.getAllScores().get(l).sortField;
                                sum+=weightedSummand;
                                ecocompScores.getAllScores().get(l).setContribution(weightedSummand);
                                ecocompScores.getAllScores().get(l).sortField=0;
                            }   
                         }
                         
                         //check if summand for this ecosystem componenent is larger than current impact
                         //at this location
                         if(sum>data[x][y])
                         {
                             data[x][y]=sum;
                             if(sum>max) max=sum;
                             if(sum<min) min=sum;
                             maxScoreSet=ecocompScores;
                         }
                    }
                    if(maxScoreSet!=null) scores.addContributionsFromSubset(maxScoreSet);
               }
           }
        }
       grid = new DataGrid(data,max,min,GlobalResources.NODATAVALUE);
    }
     
    public SensitivityScoreSet getScores()
    {
        return scores;
    }
     
}
