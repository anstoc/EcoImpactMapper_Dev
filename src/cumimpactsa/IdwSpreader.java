/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

/**
 * @summary Spreads the influence of point stressor data by means of an inverse distance weighted sum.
 * @author ast
 */
public class IdwSpreader extends GeneralProcessor
{

    public IdwSpreader()
    {
        paramNames=new String[1];
        paramValues=new double[1];
        paramNames[0]="distance";
        paramValues[0]=20000.0; //default: spread up to 20000 map units
    }
    
    @Override
    public DataGrid process(DataGrid grid) 
    {
        float[][] data = new float[grid.getData().length][grid.getData()[0].length];
        int spreadDist = (int) Math.round(paramValues[0]/GlobalResources.mappingProject.grid.getCellSize());
        
        //go through grid and calculate pressure in neighborhhod;
        float newMin = grid.getMax();
        float newMax = grid.getMin();
        for(int x=0; x<grid.getData().length;x++)
            for(int y=0; y<grid.getData()[0].length;y++)
            {
                if(grid.getData()[x][y]==grid.getNoDataValue())
                {
                    data[x][y]=grid.getNoDataValue();
                }
                else  
                {
                    //neighborhood boundaries
                    int hoodStartX = (int) Math.max(0, x-spreadDist);
                    int hoodEndX = (int) Math.min(grid.getData().length-1, x+spreadDist);
                    int hoodStartY = (int) Math.max(0, y-spreadDist);
                    int hoodEndY = (int) Math.min(grid.getData()[0].length-1, y+spreadDist);
                    
                    //calculate value
                    float value = 0;
                    for(int hx=hoodStartX;hx<=hoodEndX;hx++)
                    {
                        for(int hy=hoodStartY;hy<=hoodEndY;hy++)
                        {
                            double distance=Math.sqrt( (hx-x)*(hx-x) + (hy-y)*(hy-y) );
                            if(distance<=spreadDist && grid.getData()[hx][hy]!=grid.getNoDataValue())
                            {
                                value += grid.getData()[hx][hy] * (1 - distance/(spreadDist+1));
                            }
                        }
                    }
                    data[x][y]=value;
                    if(value>newMax) {newMax=value;}
                    if(value<newMin) {newMin=value;}
                }
            }
        
        return new DataGrid(data,newMax,newMin,grid.getNoDataValue());
    }

    @Override
    public String getName() {
        return "IDW spreading of stress";
    }

    @Override
    public IdwSpreader clone() {
         IdwSpreader clone=new IdwSpreader();
        clone.setParamValue(paramNames[0],paramValues[0]);
        return clone;
    }
    
}
