/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.util.ArrayList;
import java.util.Random;


/**
 *
 * @author ast;
 */
public class AreaRefiner extends GeneralProcessor 
{
    public AreaRefiner()
    {
        paramNames=new String[2];
        paramValues=new double[2];
        paramNames[0]="seed";
        paramNames[1]="Low pass filter distance";
        paramValues[1]=GlobalResources.lowPassFilterDistance;
    }
    
    private class AreaInfo
    {
        public ArrayList<Integer> x = new ArrayList<Integer>();
        ArrayList<Integer> y = new ArrayList<Integer>();
        double originalValue = GlobalResources.NODATAVALUE;
        int cellNr=0;
        double valueSum=0;
        double randomSum=0;
        int areaCode=-1;
        ArrayList<Double> randomValues = new ArrayList<Double>();
    }
    
    //returns the AreaInfo object with the given area code or null if no such area exists
    private AreaInfo getAreaInfo(ArrayList<AreaInfo> list, int areaCode)
    {
        for(int i=0; i<list.size();i++)
        {
            if(list.get(i).areaCode == areaCode) {return list.get(i);}
        }
        
        return null;
        
    }
    
    @Override
    public DataGrid process(DataGrid grid) 
    {
        Random rand=null;
        if(paramValues[0]>0) 
        {
            rand=new Random((long) paramValues[0]);
        }
        
        //get areas
        int[][] areaCodes = grid.calculatePresenceAreasFast();
        float[][] originalData = grid.getData();
        

        //create a list of AreaInfo objects with x, y, area code, etc.
        //ArrayList<AreaInfo> areas = new ArrayList<AreaInfo>();
        
        //TODO this needs to be faster. 
        //1. find number of areas (maximum ofarea codes). (1 pass)
        
        int max=-1;
        for(int x=0; x<originalData.length;x++)
            for(int y=0; y<originalData[0].length;y++)
            {
                if(areaCodes[x][y]>max) {max=areaCodes[x][y];}
            }
        
      
        //2. create two arrays indexed by area numbers. one contains sum of original values in that area, 
        //one contains sum of random values. add random numbers on the way and calculate sums (1 pass)
        
        float[] originalValueSum = new float[max];
        float[] randomValueSum = new float[max];
        float[][] randomValues = new float[areaCodes.length][areaCodes[0].length];
        for(int x=0; x<originalData.length;x++)
            for(int y=0; y<originalData[0].length;y++)
            {
                if(areaCodes[x][y]>0)
                {
                    //create random value
                    double r=0;
                    if(paramValues[0]==0)  {r = Math.random();}
                    else {r=rand.nextDouble();}
                    randomValues[x][y]=(float)r;
                    randomValueSum[areaCodes[x][y]-1] += r;
                    originalValueSum[areaCodes[x][y]-1] += originalData[x][y];
                }
                else
                {
                    randomValues[x][y]=grid.getNoDataValue();
                }
            }
        
        //3. calculate scaling ratio - original sum / random sum - for each area. 
        float[] ratio = new float[max];
        for(int i=0; i<max; i++) {ratio[i]=originalValueSum[i]/randomValueSum[i];}
        //4. go through the grid and scale (4th pass)
        float vmax=0;
        for(int x=0; x<originalData.length;x++)
            for(int y=0; y<originalData[0].length;y++)
            {
                if(areaCodes[x][y]>0)
                {
                    randomValues[x][y]=randomValues[x][y] * (float) ratio[areaCodes[x][y]-1];
                    if(randomValues[x][y]>vmax) {vmax=randomValues[x][y];}
                }
            }
        
        
        //5. low-pass filtering (5th pass)
        //see below old code
        
        /*for(int x=0; x<originalData.length;x++)
        {
            for(int y=0; y<originalData[0].length;y++)
            {
                if(originalData[x][y]!=grid.getNoDataValue())
                {
                    //check if area info exists for the current code, create if not
                    AreaInfo info = getAreaInfo(areas, areaCodes[x][y]);
                    if(info == null) 
                    {
                        info = new AreaInfo();
                        info.areaCode = areaCodes[x][y];
                        info.originalValue = originalData[x][y];
                        areas.add(info);
                    }
                    
                    //fill in area info
                    double r;
                    if(paramValues[0]==0)  {r = Math.random();}
                    else {r=rand.nextDouble();}
                    
                    info.cellNr++;                   
                    info.randomSum+=r;
                    info.randomValues.add(r);
                    info.valueSum+=originalData[x][y];
                    info.x.add(x);
                    info.y.add(y);
                }          
            }
        }
        
        //now go through all area infos, and re-set data to scaled random values
        float max=-1;
        float[][] newData = GlobalResources.mappingProject.grid.getEmptyGrid();
        for(int i=0; i<areas.size();i++)
        {
            AreaInfo info = areas.get(i);
            for(int l=0;l<info.x.size();l++)
            {
                float newValue = (float) (info.randomValues.get(l)*info.valueSum/info.randomSum);
                newData[info.x.get(l)][info.y.get(l)]=newValue;
                if(newValue>max) {max=newValue;}
            }
        }*/
        
        //finally, use low-pass filter (5x5)
        DataGrid unfilteredGrid = new DataGrid(randomValues,vmax,0,grid.getNoDataValue());
    
        //TODO 6 for Micheli et al (26km), 2 for Korpinen et al (25km), 3 for global at 8km resolution
        int filterCells = (int) Math.floor(paramValues[1]/(2*GlobalResources.mappingProject.grid.getCellSize()));
        return Helpers.lowPassFilter(unfilteredGrid, filterCells);
    }

    @Override
    public String getName() 
    {
        return "Refine coarse areas";
    }

    @Override
    public PreProcessor clone() 
    {
        AreaRefiner clone=new AreaRefiner();
        clone.setParamValue(paramNames[0],paramValues[0]);
        return clone;
    }
    
}
