/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

/**
 *
 * @author ast
 */
public class ResolutionReducer extends GeneralProcessor
{
    public ResolutionReducer()
    {
        super();
        this.paramNames=new String[1];
        paramNames[0]="factor";
        this.paramValues=new double[1];
        paramValues[0]=2;
    }
    
    private double getPresenceValue(double[][] data, int startX, int endX, int startY, int endY)
    {
        if(startX<0) {startX=0;}
        if(endX>=data.length) {endX=data.length-1;}
        if(startY<0) {startY=0;}
        if(endY>=data[0].length) {endY=data[0].length-1;}
        
        int dataCount=0;
        boolean presence=false;
        for(int x=startX; x<=endX; x++)
        {
            for(int y=startY; y<=endY; y++)
            {
                if(data[x][y]!=GlobalResources.NODATAVALUE) 
                {
                    dataCount++;
                    if(data[x][y]>0) {presence=true;}
                }
            }
        }
        if(dataCount==0) {return GlobalResources.NODATAVALUE;}
        else if(!presence) {return 0;}
        else {return 1;}
    }     
  
    private float getMeanValue(float[][] data, int startX, int endX, int startY, int endY)
    {
        if(startX<0) {startX=0;}
        if(endX>=data.length) {endX=data.length-1;}
        if(startY<0) {startY=0;}
        if(endY>=data[0].length) {endY=data[0].length-1;}
        
        int dataCount=0;
        int nonZeroCount=0;
        float sum=0;
        for(int x=startX; x<=endX; x++)
        {
            for(int y=startY; y<=endY; y++)
            {
                if(data[x][y]!=GlobalResources.NODATAVALUE)  //ignore no data
                {
                    dataCount++;
                    if(data[x][y]>0)
                    {    
                        nonZeroCount++;
                        sum+=data[x][y];
                    }
                }
            }
        }
        
        if(dataCount==0) {return GlobalResources.NODATAVALUE;}
        else if(nonZeroCount==0) {return 0;}
        else {return sum/nonZeroCount;}

    }   
    
       private double getMaxValue(double[][] data, int startX, int endX, int startY, int endY)
    {
        if(startX<0) {startX=0;}
        if(endX>=data.length) {endX=data.length-1;}
        if(startY<0) {startY=0;}
        if(endY>=data[0].length) {endY=data[0].length-1;}
        
        int dataCount=0;
        double max=0;
        for(int x=startX; x<=endX; x++)
        {
            for(int y=startY; y<=endY; y++)
            {
                if(data[x][y]!=GlobalResources.NODATAVALUE) 
                {
                    dataCount++;
                    if(data[x][y]>max) {max=data[x][y];}
                }
            }
        }
        if(dataCount==0) {return GlobalResources.NODATAVALUE;}
        else {return max;}

    }   
    
    private void writeBlock(float[][] data, float value, int startX, int endX, int startY, int endY)
    {
        if(startX<0) {startX=0;}
        if(endX>=data.length) {endX=data.length-1;}
        if(startY<0) {startY=0;}
        if(endY>=data[0].length) {endY=data[0].length-1;}
        
        for(int x=startX; x<=endX; x++)
        {
            for(int y=startY; y<=endY; y++)
            {
                data[x][y]=value;
            }
        }

    }      
       
    @Override
    public DataGrid process(DataGrid grid) 
    {
        float[][] data=grid.getData();
        float[][] newData=GlobalResources.mappingProject.grid.getEmptyGrid();
        float max=-1;
        for(int x=0; x<data.length; x+=paramValues[0])
        {
            for(int y=0; y<data[0].length; y+=paramValues[0])
            {
                int startX=x;
                int startY=y;
                int endX=x+(int)(Math.floor(paramValues[0]))-1;
                int endY=y+(int)(Math.floor(paramValues[0]))-1;
                
                float value;
                //if(grid.isPresenceAbsence()) {value=getPresenceValue(data,startX,endX,startY,endY);}
                //else 
                //{
                    value=getMeanValue(data,startX,endX,startY,endY);
                //}
                if(value>max) {max=value;}
                writeBlock(newData, value, startX, endX, startY, endY);
            }
       }
       return new DataGrid(newData,max,0,GlobalResources.NODATAVALUE);
    }

    @Override
    public String getName() {
        return "Reduce Spatial Resolution";
    }

    @Override
    public PreProcessor clone() 
    {
        ResolutionReducer clone=new ResolutionReducer();
        clone.setParamValue(paramNames[0],paramValues[0]);
        return clone;
    }
    
}
