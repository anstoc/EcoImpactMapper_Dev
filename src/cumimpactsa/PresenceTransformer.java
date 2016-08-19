/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

/**
 *
 * @author andy
 * @summary This processor replaces all values >0 with 1, and all others with 0.
 */
public class PresenceTransformer extends GeneralProcessor 
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
                        if(values[x][y]>0) {newValues[x][y]=1;}
                        else {newValues[x][y]=0;}
                    }
                }
            }
            return new DataGrid(newValues, 1 ,0,grid.getNoDataValue());

    }

    @Override
    public String getName() {
        return "Replace values with presence-absence";
    }

    @Override
    public PreProcessor clone() {
        return new PresenceTransformer();
    }

}

