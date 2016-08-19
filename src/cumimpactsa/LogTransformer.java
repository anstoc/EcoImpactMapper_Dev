/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

/**
 *
 * @author ast
 */
public class LogTransformer extends GeneralProcessor
{

     public DataGrid process(DataGrid grid) 
    {

            float[][] values;
            float[][] newValues;
            values = grid.getData();
            newValues = new float[values.length][values[0].length];

            for(int x=0; x<values.length;x++)
            {
                for(int y=0; y<values[0].length;y++)
                {
                    if(values[x][y]==grid.getNoDataValue())
                    {
                        newValues[x][y]=grid.getNoDataValue();
                    }
                    else
                    {
                        newValues[x][y]=(float) Math.log(values[x][y]+1);
                    }
                }
            }
            return new DataGrid(newValues, (float)Math.log(grid.getMax()+1),(float)Math.log(grid.getMin()+1),grid.getNoDataValue());
      
    }

    @Override
    public String getName() {
        return "Log(X+1)-Transformation";
    }

    @Override
    public PreProcessor clone() {
        return new LogTransformer();
    }
        
}
