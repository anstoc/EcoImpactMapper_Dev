/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

/**
 * @summary This pre-processor moves around cells in a data grid. It should only be applied to point stressor data.
 * @author ast
 */
public class ErrorCreatorMovePoints extends GeneralProcessor
{

    @Override
    public DataGrid process(DataGrid grid) 
    {
        float[][] data = new float[grid.getData().length][grid.getData()[0].length];
        
        //go through original grid and set data on the fly
        for(int x=0; x<grid.getData().length;x++)
            for(int y=0; y<grid.getData()[0].length;y++)
            {
                if(grid.getData()[x][y]==grid.getNoDataValue())
                {
                    data[x][y]=grid.getNoDataValue();
                }
                else if(grid.getData()[x][y]!=0) //switch value with a random cell if it's not zero.
                {
                    int swapX = (int) Math.floor(Math.random()*data.length);
                    int swapY = (int) Math.floor(Math.random()*data[0].length);
                    while(data[swapX][swapY]==grid.getNoDataValue())
                    {
                        swapX = (int) Math.floor(Math.random()*data.length);
                        swapY = (int) Math.floor(Math.random()*data[0].length);
                    }
                    data[swapX][swapY] = grid.getData()[x][y];
                }
            }
        
        return new DataGrid(data,grid.getMax(),grid.getMin(),grid.getNoDataValue());

    }

    @Override
    public String getName() 
    {
        return "Move points to random locations";
    }

    @Override
    public PreProcessor clone() 
    {
        return new ErrorCreatorMovePoints();
    }
    
}
