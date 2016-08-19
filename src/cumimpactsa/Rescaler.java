/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

/**
 *
 * @author ast
 */
public class Rescaler extends GeneralProcessor 
{

    @Override
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
                        newValues[x][y]=values[x][y]/grid.getMax();
                    }
                }
            }
            return new DataGrid(newValues, 1 ,0,grid.getNoDataValue());

    }

    @Override
    public String getName() {
        return "Rescale to maximum 1";
    }

    @Override
    public PreProcessor clone() {
        return new Rescaler();
    }

}
