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
public class PercentileTransformer extends GeneralProcessor
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
        
        return new DataGrid(values, 1,0,grid.getNoDataValue());
        
        
    }

    @Override
    public String getName() 
    {
        return "Percentile (CDF) transformation";
    }

    @Override
    public PreProcessor clone() 
    {
        return new PercentileTransformer();
    }
    
}
