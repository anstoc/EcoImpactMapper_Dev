/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *@summary This class provides a collection of helper functions.
 * @author ast
 */
public abstract class Helpers 
{
    

   public final static String OK = "ok"; 
   private static String error = OK;
    
    //low pass filter as moving average
    public static DataGrid lowPassFilter(DataGrid grid, int kernelSize)
    {
        float[][] data = grid.getData();
        float[][] filtered=GlobalResources.mappingProject.grid.getEmptyGrid();
        float max = 0;
        for(int x=0; x<data.length;x++)
        {
            for(int y=0; y<data[0].length; y++)
            {
                if(data[x][y]!=grid.getNoDataValue())
                {
                    int startX=Math.max(0, x-kernelSize);
                    int endX = Math.min(x+kernelSize, data.length-1);
                    int startY = Math.max(0, y-kernelSize);
                    int endY = Math.min(y+kernelSize, data[0].length-1);
                    
                    double valueSum=0;
                    int valueCount = 0;
                    
                    for(int rx=startX; rx<=endX; rx++)
                    {
                        for(int ry=startY; ry<=endY; ry++)
                        {
                            if(data[rx][ry]!=grid.getNoDataValue())
                            {
                                valueSum+=data[rx][ry];
                                valueCount++;
                            }
                        }
                    }
                    
                    filtered[x][y]=(float) (valueSum/valueCount);
                    if(filtered[x][y]>max) {max=filtered[x][y];}
                }
                
            }
        }
        
        return new DataGrid(filtered,max,0,grid.getNoDataValue());
        
    }
   
    public static float[] stringListToFloatArray(ArrayList<String> stringList)
    {
        float[] numList = new float[stringList.size()];
        error = OK;
        
        try
        {
            for(int i = 0; i < stringList.size(); i++)
            {
       
                numList[i] = Float.parseFloat(stringList.get(i));
            }
        }
        
        catch(Exception e)
        {
            error = "Error while parsing numbers from text. //// " + e.getMessage() + " //// " + e.getStackTrace();
        }
        
        
        return numList;
    }
    
    public static double[] stringListToDoubleArray(ArrayList<String> stringList)
    {
        double[] numList = new double[stringList.size()];
        error = OK;
        
        try
        {
            for(int i = 0; i < stringList.size(); i++)
            {
       
                numList[i] = Double.parseDouble(stringList.get(i));
            }
        }
        
        catch(Exception e)
        {
            error = "Error while parsing numbers from text. //// " + e.getMessage() + " //// " + e.getStackTrace();
        }
        
        
        return numList;
    }
    

    
    /**
     * @summary Returns last function call's error message. If no error, returns Helpers.OK.
     * @return Error string.
     */
    public static String getError()
    {
        return error;
    }
    
    public static double[][] deepArrayCopy(double[][] original)
    {
        /*double[][] copy = original.clone();
        for (int i = 0; i < copy.length; i++) 
        {
            copy[i] = copy[i].clone();
        }*/
        
        double[][] copy = new double[original.length][original[0].length];
        
        for (int i = 0; i < copy.length; i++) 
        {
            copy[i] = Arrays.copyOf(original[i],original[i].length);
        }
        
        return copy;
    }
    
    public static float[][] deepArrayCopy(float[][] original)
    {
        /*double[][] copy = original.clone();
        for (int i = 0; i < copy.length; i++) 
        {
            copy[i] = copy[i].clone();
        }*/
        
        float[][] copy = new float[original.length][original[0].length];
        
        for (int i = 0; i < copy.length; i++) 
        {
            copy[i] = Arrays.copyOf(original[i],original[i].length);
        }
        
        return copy;
    }
    
    public static void addToArray2d(float[][] toArray, float[][] addArray)
    {
        for(int x=0; x<toArray.length; x++)
        {
            for(int y=0; y<toArray[0].length;y++)
            {
                toArray[x][y] += addArray[x][y];
            }
        }
    }
    
    public static ArrayList<String> deepArrayListCopy(ArrayList<String> list)
    {
        ArrayList<String> copy = new ArrayList<String>();
        for(int i=0; i<list.size();i++) {copy.add(new String(list.get(i)));}
        return copy;
    }

    //removes parameter value from processor
    static String cleanProcessorName(String procName) 
    {
        if(!procName.contains("[")) return procName;
        else return procName.substring(0, procName.indexOf("["));
    }

    static float getProcessorParam(String procName) 
    {
        float value=-1;
        if(!procName.contains("[")) return value;
        else
        {
            String substring=procName.substring(procName.indexOf("[")+1, procName.indexOf("]"));
            try
            {
                value=Float.parseFloat(substring);
            }
            catch(Exception e)
            {
                value=-1;
            }
        }
        return value;
    }
}
