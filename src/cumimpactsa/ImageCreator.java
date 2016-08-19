/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.*;

/**
 * @summary Helper class creating images from data
 * @author ast
 */
public class ImageCreator 
{
    int xSize=-1;
    int ySize=-1;
    
    Color noDataColor = new Color(30,30,30); //TODO: Make editable
    public static Color minColor = new Color(50,50,200);
    public static Color midColor = new Color(0,255,0);
    public static Color maxColor = new Color(230,10,10);
    public static boolean quantileStretch=false;
    
    public ImageCreator(int xSize, int ySize)
    {
        this.xSize=xSize;
        this.ySize=ySize;
    }
    
    public BufferedImage createStudyAreaImage(float[][] aoi, Point2DInt originalDimensions)
    {

        BufferedImage image = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_RGB);
        
        float[][] imageData=transformGrid(new DataGrid(aoi,1,0,GlobalResources.NODATAVALUE),originalDimensions);
        
        
        for(int x=0; x<xSize;x++)
        {
            for(int y=0;y<ySize;y++)
            {
                if(imageData[x][y]==0) {image.setRGB(x,y,Color.BLACK.getRGB());}
                else {image.setRGB(x, y, Color.GRAY.getRGB());}
            }
        }  
        
        return image;
    }
    
    public BufferedImage createDataImage(DataGrid data, Point2DInt originalDimensions)
    {
        
        if(quantileStretch)
        {
            PercentileTransformer stretcher=new PercentileTransformer(); 
            data = stretcher.process(data);
        }
        
        BufferedImage image = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_RGB);
        
        float[][] imageData=transformGrid(data,originalDimensions);
        

        
        for(int x=0; x<xSize;x++)
        {
            for(int y=0;y<ySize;y++)
            {
                
                if(imageData[x][y]==GlobalResources.NODATAVALUE) {image.setRGB(x,y,this.noDataColor.getRGB());}
                else 
                    {
                        image.setRGB(x, y, interpolateColor(imageData[x][y],data.getMin(),data.getMax()).getRGB());
                    }
            }
        }  
        
        return image;
    }
    
    
    public BufferedImage createVerticalColorScaleImage()
    {
        
        BufferedImage image = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_RGB);
        
        
        for(int x=0; x<xSize;x++)
        {
            for(int y=0;y<ySize;y++)
            {
               
                        image.setRGB(x, ySize-y-1, interpolateColor(y,0,ySize-1).getRGB());
            }
        }  
        
        return image;
    }
    
    private Color interpolateColor(double value, double min, double max)
    {
        
        Color result=null;
        
        double middle=(max-min)/2;
        if(value<=middle)
        {
            double proportion=(value-min)/(middle-min);
            
            int red = (int) (proportion * midColor.getRed() + (1-proportion) * minColor.getRed());
            int green = (int) (proportion * midColor.getGreen() + (1-proportion) * minColor.getGreen());
            int blue = (int) (proportion * midColor.getBlue() + (1-proportion) * minColor.getBlue()); 
            
            result = new Color(red, green, blue);
        }
        else
        {
            double proportion=(max-value)/(max-middle);
            
            int red = (int) (proportion * midColor.getRed() + (1-proportion) * maxColor.getRed());
            int green = (int) (proportion * midColor.getGreen() + (1-proportion) * maxColor.getGreen());
            int blue = (int) (proportion * midColor.getBlue() + (1-proportion) * maxColor.getBlue()); 
            

            
            result = new Color(red, green, blue);
           
           
        }
       
        return result;
    }
    
    /**
     * 
     * @param grid 
     * @param startX
     * @param startY
     * @param xSize
     * @param ySize
     * @param returnMax If true, maximum is returned, otherwise mean
     * @return mean or max of neighborhood
     */
    private float getNeighborhoodStats(DataGrid grid, int startX,int startY, int xSize, int ySize, boolean returnMax)
    {
        float[][] data = grid.getData();
        if(startX+xSize>=data.length) {xSize=data.length-startX-1;}
        if(startY+ySize>=data.length) {ySize=data.length-startY-1;}
        
        //double[][] hood = new double[xSize][ySize];
        
        float max=data[startX][startY];
        float sum=0;
        int count=0;
        
        for(int x=0;x<xSize;x++)
        {
            for(int y=0; y<ySize;y++)
            {
                float value=data[startX+x][startY+y];
                if(value!=grid.getNoDataValue())
                {
                    if(value>max) {max=value;}
                    count++;
                    sum+=value;
                }
            }
        }
        float mean;
        if(count>0) {mean=sum/count;}
        else {mean=GlobalResources.NODATAVALUE;}
      
        if(returnMax) {return max;}
        else {return mean;}
        
    }
    
    private float[][] transformGrid(DataGrid grid, Point2DInt originalDimensions)
    {
        float[][] original = grid.getData();
        float[][] imageGrid=new float[xSize][ySize];
        
        double xMultiplier = ((double)originalDimensions.x)/((double) xSize);
        double yMultiplier = ((double)originalDimensions.y)/((double) ySize);
        
        int hoodSizeX = (int) Math.floor(xMultiplier); //size of neighborhood from which maximum value is to be displayed
        int hoodSizeY = (int) Math.floor(yMultiplier); //size of neighborhood from which maximum value is to be displayed
        
        for(int x=0; x<xSize; x++)
        {
            for(int y=0; y<ySize;y++)
            {
                int dataX=(int) Math.floor(xMultiplier*x);
                int dataY= (int) Math.floor(yMultiplier*y);
                if(hoodSizeX==0 && hoodSizeY==0)
                {
                    imageGrid[x][y]=original[dataX][dataY];
                }
                else
                {
                   imageGrid[x][y]=getNeighborhoodStats(grid,dataX,dataY,hoodSizeX,hoodSizeY,true);
                }
            }
        }
        
        return imageGrid;
    }
  
}
