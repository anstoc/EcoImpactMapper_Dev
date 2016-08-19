/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.util.ArrayList;

/**
 *
 * @author ast
 */
public class WeightedStressorIndex extends SpatialDataLayer
{
    public WeightedStressorIndex(String saveFileName)
    {
        super(GlobalResources.getDateTime() + " Stressor index (weighted)",null,GlobalResources.DATATYPE_SPATIAL,null);
        source = new DataSourceInfo();
        source.sourceFile=saveFileName;
        source.xField="x";
        source.yField="y";
        source.valueField="value";
        
        this.type=GlobalResources.DATATYPE_SPATIAL;
        
        //now create internal data grid
        float[][] data = new float[GlobalResources.mappingProject.grid.getDimensions().x][GlobalResources.mappingProject.grid.getDimensions().y];
        
        //load all processed grids and calculate mean of sensitivity scores
        ArrayList<float[][]> stressorDataList = new ArrayList<float[][]>();
        float[] weights = new float[GlobalResources.mappingProject.stressors.size()];
        for(int l=0;l<GlobalResources.mappingProject.stressors.size();l++)
        {
            stressorDataList.add(GlobalResources.mappingProject.stressors.get(l).getProcessedGrid().getData());
            weights[l]=GlobalResources.mappingProject.sensitivityScores.getStressorAverageScore(GlobalResources.mappingProject.stressors.get(l).getName());
            
        }
        
        //sum them up, weighted
        float max=0;
        float min=1;
        for(int l=0; l<stressorDataList.size();l++)
        {
            GlobalResources.mappingProject.setProcessingProgressPercent((int) (100*l/stressorDataList.size()));
            
            float[][] stressorData=stressorDataList.get(l);
            float weight = weights[l];
            for(int y=0;y<data[0].length;y++)
            {
                for(int x=0;x<data.length;x++)
                {
                    if(stressorData[x][y]==GlobalResources.NODATAVALUE || data[x][y]==GlobalResources.NODATAVALUE)
                    {
                        data[x][y]=GlobalResources.NODATAVALUE;
                    }
                    else
                    {
                        data[x][y]=data[x][y]+weight*stressorData[x][y];
                        if(data[x][y]>max) {max=data[x][y];}
                        if(data[x][y]<min) {min=data[x][y];}
                    }
                }
            }
        }
        
       grid = new DataGrid(data,max,min,GlobalResources.NODATAVALUE);
       
    }
}
