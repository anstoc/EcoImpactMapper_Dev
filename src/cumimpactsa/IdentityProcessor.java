/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ast
 */
public class IdentityProcessor extends GeneralProcessor 
{
    @Override
    public DataGrid process(DataGrid grid) 
    {
        float[][] values = grid.getData();
        float[][] newValues = new float[values.length][values[0].length];

        for(int x=0; x<values.length;x++)
        {
            for(int y=0; y<values[0].length;y++)
            {  
               newValues[x][y]=values[x][y];
            }
        }
        return new DataGrid(newValues, grid.getMax(),grid.getMin(),grid.getNoDataValue());
        
    }

    @Override
    public String getName() {
        return "Identity";
    }

    @Override
    public PreProcessor clone() {
        return new IdentityProcessor();
    }
}
