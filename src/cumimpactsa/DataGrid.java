/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 
 * @author ast
 */
public class DataGrid 
{
    private float[][] values;
    private float maxValue;
    private float minValue;
    private float noDataValue;
    private ArrayList<Float> uniqueDataValues=null;
    private ArrayList<DataLocation> dataLocations = null;
    private boolean isPresenceAbsence=false;
    private int cellsWithPresence=-1;
    private double cellSum=-1;  //sum of values in all cells
    private int[] areaSizes=null;
    private float[] areaValues=null;
    
    
     public class LocationComparator implements Comparator<DataLocation> 
    {
        @Override
        public int compare(DataLocation i1, DataLocation i2) 
        {
            return (new Float(i1.value)).compareTo(i2.value);
        }
     }
    
    /**
     * @summary Creates a grid with data.
     * @param values The values of the grid.
     * @param max The highest value.
     * @param min The lowest value. 
     * @param noDataValue The no data value.
     */
    public DataGrid(float[][] values, float max, float min, float noDataValue)
    {
        this.values=values;
        this.maxValue=max;
        this.minValue=min;
        this.noDataValue=noDataValue;
        
    }
    
    public boolean isPresenceAbsence()
    {
        if(uniqueDataValues==null)
        {
            createDataInfo();
        }
        return isPresenceAbsence;
    }
   
    public DataGrid clone()
    {
        float[][] clonedValues=Helpers.deepArrayCopy(values);
        return new DataGrid(clonedValues,this.maxValue,this.minValue,this.noDataValue);
    }
    
    
    public int[] getPresenceAreaSizes()
    {
       if(areaSizes==null)
       {
           calculatePresenceAreas();
       }
       return areaSizes;
    }
    
    public ArrayList<Float> getUniqueDataValues()
    {
        if(uniqueDataValues==null)
        {
            createUniqueDataValues();
        }
        return uniqueDataValues;
    }
    
   public void createUniqueDataValues()
    {

        uniqueDataValues=new ArrayList<Float>();
        cellsWithPresence=0;
        cellSum=0;
         //create unique data value list
        for(int x=0; x<values.length; x++)
        {
            for(int y=0; y<values[0].length; y++)
            {
                float value=values[x][y];               
                if(value!=noDataValue)
                {
                    
                    if(value>0) 
                    {
                        cellsWithPresence++;
                        cellSum+=value;
                    }
                    //check if list contains this value
                    boolean contained=false;
                    for(int i=uniqueDataValues.size()-1; i>=0; i--)
                    {
                        if(uniqueDataValues.get(i).equals(value))
                        {
                            contained=true;
                            break;
                        }
                    }
                    if(!contained) //not noData and not yet in unique value list
                    {
                        uniqueDataValues.add(value);
                    }
                }    
               
            }
        }
        //TODO check that results are still the same...
        //Collections.sort(uniqueDataValues);
        if(uniqueDataValues.size()>2) {this.isPresenceAbsence=false;}
        else {isPresenceAbsence=true;}

    }

    public void createDataInfo()
    {

        cellsWithPresence=0;
        cellSum=0;
        ArrayList<Double> valuesFound=new ArrayList<Double>();
        isPresenceAbsence=true;
         //create unique data value list
        for(int x=0; x<values.length; x++)
        {
            for(int y=0; y<values[0].length; y++)
            {
                double value=values[x][y];               
                if(value!=noDataValue)
                {
                    
                    if(value>0) 
                    {
                        cellsWithPresence++;
                        cellSum+=value;
                    }
                    if(isPresenceAbsence)
                    {
                        //check if list contains this value
                        boolean contained=false;
                        for(int i=valuesFound.size()-1; i>=0; i--)
                        {
                            if(valuesFound.get(i).equals(value))
                            {
                                contained=true;
                                break;
                            }
                        }
                        if(!contained) //not noData and not yet in unique value list
                        {
                            valuesFound.add(value);
                            if(valuesFound.size()>2) {isPresenceAbsence=false;}
                        }
                    }
                }    
               
            }
        }
    }
    
    //faster for small, slower for large data sets
    
   /* public void createDataInfo2()
    {

        OrderedUniqueDoubleList uniqueDataValues=new OrderedUniqueDoubleList();
        cellsWithPresence=0;
        cellSum=0;
         //create sorted unique data value list
        for(int x=0; x<values.length; x++)
        {
            for(int y=0; y<values[0].length; y++)
            {
                double value=values[x][y];               
                if(value!=noDataValue)
                {
                    
                    if(value>0) 
                    {
                        cellsWithPresence++;
                        cellSum+=value;
                    }
                    uniqueDataValues.add(value);
                    //check if list contains this value
                    /*boolean contained=false;
                    for(int i=0; i<uniqueDataValues.size(); i++)
                    {
                        if(uniqueDataValues.get(i).equals(value))
                        {
                            contained=true;
                            break;
                        }
                    }
                    if(!contained) //not noData and not yet in unique value list
                    {
                        uniqueDataValues.add(value);
                    }*/
         /*       }    
               
            }
        }
        //Collections.sort(uniqueDataValues);
        if(uniqueDataValues.size()>2) {this.isPresenceAbsence=false;}
        else {isPresenceAbsence=true;}
        this.uniqueDataValues=uniqueDataValues.getList();
        
        
        
    }*/
    
    public float[][] getData()
    {
        return values;
    }
    
    public float getMax()
    {
        return maxValue;
    }       
    
    public float getMin()
    {
        return minValue;
    }
    
    public float getNoDataValue()
    {
        return noDataValue;
    }

    //returns the number of non-zero cells.
    public int getCellsWithPresence() 
    {
        if(cellsWithPresence==-1) {createDataInfo();}
        return cellsWithPresence;
    }
    //returns the sum of values in all cells with data.
    public double getCellSum() 
    {
        if(this.cellSum==-1) {createDataInfo();}
        return cellSum;
    }

    
    
    //sets area code for (x,y) to code, then checks all neighboring cells and recursively sets code
    private void setNeighborhoodAreaCodes(int[][] areaCodes, int x, int y, int code)
    {
        areaCodes[x][y]=code;      
        //set neighborhood, making sure it doesn't exceed array bounds
        int startX=x;
        int endX=x;
        int startY=y;
        int endY=y;
        if(x>0) {startX=x-1;}
        if(x<areaCodes.length-1) {endX=x+1;}
        if(y>0) {startY=y-1;}
        if(y<areaCodes[0].length-1) {endY=y+1;}
        
        for(int nx=startX;nx<=endX;nx++)
            for(int ny=startY;ny<=endY;ny++)
            {
                if(areaCodes[nx][ny]==0 && values[nx][ny]==values[x][y])
                {
                    setNeighborhoodAreaCodes(areaCodes,nx,ny,code);
                }
             }
    }
    
     //sets area code for (x,y) to code, then checks all neighboring cells and recursively sets code
    private ArrayList<Integer> getNeighborhoodAreaCodes(int[][] areaCodes, int x, int y)
    {
        //set neighborhood, making sure it doesn't exceed array bounds
        int startX=x;
        int endX=x;
        int startY=y;
        int endY=y;
        if(x>0) {startX=x-1;}
        if(x<areaCodes.length-1) {endX=x+1;}
        if(y>0) {startY=y-1;}
        if(y<areaCodes[0].length-1) {endY=y+1;}
        
        ArrayList<Integer> codes=new ArrayList<Integer>();
        //find area codes
        for(int nx=startX;nx<=endX;nx++)
            for(int ny=startY;ny<=endY;ny++)
            {
                if(areaCodes[nx][ny]!=0 && values[nx][ny]==values[x][y])
                {
                    //check if code is already in list
                    boolean exists = false;
                    for(int i=0; i<codes.size();i++)
                    {
                        if(codes.get(i).equals(areaCodes[nx][ny])) {exists=true;}
                    }
                    if(!exists) codes.add(new Integer(areaCodes[nx][ny]));
                }
             }
        return codes;
    }
    
    //returns a raster grid with area codes
    public int[][] calculatePresenceAreas()
    {
        //General procedure: go through data; when finding a non-zero non-no-data cell that is not assigned to an area number it;
        //go through cells left and right of it and recursively expand area

        int currentAreaCode=0;
        int[][] areaCodes = new int[values.length][values[0].length];
        
        for(int x=0;x<values.length;x++)
            for(int y=0;y<values[0].length;y++)
            {
                if(areaCodes[x][y]==0 && values[x][y]>0 && values[x][y]!=noDataValue)
                {
                    currentAreaCode++;
                    setNeighborhoodAreaCodes(areaCodes,x,y,currentAreaCode);
                }
            }
        
         //now use area code raster to calculate cells in each area
        areaSizes=new int[currentAreaCode];
        areaValues=new float[currentAreaCode];
        for(int x=0; x<areaCodes.length;x++)
            for(int y=0;y<areaCodes[0].length;y++)
            {
                int code=areaCodes[x][y];
                if(code>0)
                {
                    areaSizes[code-1]++;
                    areaValues[code-1]=values[x][y];
                }
            }
       return areaCodes;
    }

    //returns a raster grid with area codes, much faster than the above function buty does NOT
    //create area sizes and values.
    public int[][] calculatePresenceAreasFast()
    {
        //General procedure: go through data; when finding a non-zero non-no-data cell that is not assigned to an area number it;
        //go through cells left and right of it and recursively expand area

        int currentAreaCode=0;
        int[][] areaCodes = new int[values.length][values[0].length];
        
        for(int x=0;x<values.length;x++)
            for(int y=0;y<values[0].length;y++)
            {
                if(areaCodes[x][y]==0 && values[x][y]>0 && values[x][y]!=noDataValue)
                {
                    currentAreaCode++;
                    setNeighborhoodAreaCodes(areaCodes,x,y,currentAreaCode);
                }
            }
        
       return areaCodes;
    }
       
    public void mergeAreas(int[][] areaCodes, ArrayList<Integer> codes)
    {
        for(int x=0;x<areaCodes.length;x++)
            for(int y=0;y<areaCodes[0].length;y++)
            {
                for(int i=1; i<codes.size();i++)
                {
                    int code = codes.get(i).intValue();
                    if(areaCodes[x][y]==code)
                    {
                        areaCodes[x][y]=codes.get(0).intValue();
                    }
                }
            }
    }
    
    public float[] getPresenceAreaValues() 
    {
        if(this.areaValues==null)
        {
            calculatePresenceAreas();
        }
       return this.areaValues;
    }
 
    public ArrayList<DataLocation> getOrderedDataLocations()
    {
        if(this.dataLocations==null)
        {
            dataLocations = new ArrayList<DataLocation>();
            for(int x=0; x<values.length; x++)
            {
                for(int y=0; y<values[0].length; y++)
                {
                    double value=values[x][y];               
                    if(value!=noDataValue && value>0)
                    {
                        DataLocation l = new DataLocation();
                        l.x=x;
                        l.y=y;
                        l.value=values[x][y];
                        dataLocations.add(l);
                    }
                }
            }
            
            Collections.sort(dataLocations,new LocationComparator());
            
        }
        
        return dataLocations;
        
    }
    
    public int getXDim()
    {
        return values.length;
    }
    
    public int getYDim()
    {
        return values[0].length;
    }
    
}
