/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author ast
 */
public class StressorAreaRelationships
{
    private class DataItem
    {
        int x;
        int y;
        double divIndex;
        double[] stressorValues; //order like GlobalResources.mappingProject.stressors
    }
   
    
    private class ItemComparator implements Comparator<DataItem> 
    {
        @Override
        public int compare(DataItem i1, DataItem i2) 
        {
            return (new Double(i2.divIndex)).compareTo(new Double(i1.divIndex));
        }
    }
    
    private ArrayList<DataItem> items = new ArrayList(); 
    double[] percentArea;
    double[] divIndex;
    double[][] percentStressors;
    
    public StressorAreaRelationships()
    {
        //calculate diversity index
        DiversityIndex index = new DiversityIndex("");
        
        GlobalResources.mappingProject.setProcessingProgressPercent(10);
        
        //make list of data for sorting by div. index.
        float[][] divData=index.grid.getData();
        for(int x=0; x<GlobalResources.mappingProject.grid.getDimensions().x;x++)
        {
            for(int y=0; y<GlobalResources.mappingProject.grid.getDimensions().y; y++)
            {
                DataItem item = new DataItem();
                item.x=x;
                item.y=y;
                item.divIndex=index.getGrid().getData()[x][y];
                //item.stressorNames=new String[GlobalResources.mappingProject.stressors.size()];
                item.stressorValues=new double[GlobalResources.mappingProject.stressors.size()];
                boolean noData=false;
                for(int s=0; s<GlobalResources.mappingProject.stressors.size(); s++)
                {
                    item.stressorValues[s]=GlobalResources.mappingProject.stressors.get(s).getGrid().getData()[x][y];
                    if(item.stressorValues[s]==GlobalResources.NODATAVALUE)
                    {
                        noData=true;
                    }
                    
                }
                
                if(item.divIndex!=GlobalResources.NODATAVALUE && !noData)
                {
                     items.add(item);
                }
                
            }

        }

       //this created a list of items. Now sort them
       GlobalResources.mappingProject.setProcessingProgressPercent(15);
       Collections.sort(items, new ItemComparator());
       GlobalResources.mappingProject.setProcessingProgressPercent(20);    
       
       //items are now sorted i. Go through and add up stressor values
       percentArea = new double[items.size()+1];
       divIndex = new double[items.size()+1];
       percentStressors=new double[GlobalResources.mappingProject.stressors.size()][items.size()+1];
       double[] stressorSum = new double[GlobalResources.mappingProject.stressors.size()];
       
       for(int i=1; i<=items.size();i++)
       {
           percentArea[i]=100.0*((double)i)/items.size();
           divIndex[i]=items.get(i-1).divIndex;
           for(int s=0; s<items.get(i-1).stressorValues.length; s++)
           {
               percentStressors[s][i]=percentStressors[s][i-1]+items.get(i-1).stressorValues[s];
               stressorSum[s]+=items.get(i-1).stressorValues[s];
           }
       }
       
       //calculate percentages
       for(int i=1; i<=items.size();i++)
       {
           GlobalResources.mappingProject.setProcessingProgressPercent((int) (20+80*i/items.size()));
           for(int s=0; s<items.get(i-1).stressorValues.length; s++)
           {
               percentStressors[s][i]=100*percentStressors[s][i]/stressorSum[s];
           }
       }
    }
    
    public void writeToFile(String filename)
    {
              
        //set up table
        CsvTableGeneral table = new CsvTableGeneral();
        table.addColumn("PercentArea");
        table.addColumn("DivIndex");
        for(int s=0; s<GlobalResources.mappingProject.getStressorNames().length; s++)
        {
            table.addColumn(GlobalResources.mappingProject.getStressorNames()[s]);
        }
        
        double nextIndexValue;
        //add rows
        for(int i=0; i<this.percentArea.length; i++)
        {
            if(i+1 < this.divIndex.length)
            {
                nextIndexValue=divIndex[i+1];
            }
            else
            {
                nextIndexValue=-9999;
            }
            
            if(nextIndexValue!=divIndex[i])
            {
                ArrayList<String> row = new ArrayList<String>();
                row.add(new Double(percentArea[i]).toString());
                row.add(new Double(this.divIndex[i]).toString());
                for(int s=0; s<GlobalResources.mappingProject.stressors.size();s++)
                {
                    row.add(new Double(this.percentStressors[s][i]).toString());
                }
            
                table.addRow(row);
            }
        }
    
        //write to file
        table.writeToFile(filename);
    }
    
}