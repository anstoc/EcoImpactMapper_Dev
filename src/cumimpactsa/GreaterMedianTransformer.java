/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.util.ArrayList;

/**
 *
 * @author andy
 * @summary Sets all cells with values greater than the median to 1, and all others to 0.
 */
public class GreaterMedianTransformer extends GeneralProcessor
{

    @Override
    public DataGrid process(DataGrid grid) 
    {
        
        if(grid.isPresenceAbsence()) return grid;
        
        ArrayList<DataLocation> locations = grid.getOrderedDataLocations();
        
        //start with the smallest value
        double currentValue=locations.get(0).value;
        double currentPercent = 0;
        
        float[][] values = GlobalResources.mappingProject.grid.getEmptyGrid(); //filled with 0s und NoData where applicable
        
        int l=0;
         //set all 0s to 0
        /*while(locations.get(l).value==0)
        {
            values[locations.get(l).x][locations.get(l).y] = 0;
            l++;
        }*/
        
        boolean done=false;
        int nextBreak=l;
        
        while(l<locations.size())
        {
            currentValue = locations.get(l).value;
            if(nextBreak==l)
            {
               while(nextBreak<locations.size() && locations.get(l).value == locations.get(nextBreak).value) {nextBreak++;}
            }
            
            while(l<nextBreak)
            {
                float breakPercentile=((float) (nextBreak)/locations.size());
                values[locations.get(l).x][locations.get(l).y] = breakPercentile;
                l++;
            }

        }
        
        for(int x=0;x<values.length;x++)
        {
            for(int y=0; y<values[0].length;y++)
            {
                if(values[x][y]!=grid.getNoDataValue())
                {
                    if(values[x][y]>0.5) {values[x][y]=1;}
                    else {values[x][y]=0;}
            
                }
            }
        }
        
        return new DataGrid(values, 1,0,grid.getNoDataValue());
             
    }

    @Override
    public String getName() 
    {
        return "Greater-than-median transformation";
    }

    @Override
    public PreProcessor clone() 
    {
        return new GreaterMedianTransformer();
    }
    
}
