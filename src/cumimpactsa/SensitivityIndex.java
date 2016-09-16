package cumimpactsa;

import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author andy
 * @summary Calculates the sum of the - essentially a weighted stressor index for ecosystem components
 */
public class SensitivityIndex extends SpatialDataLayer
{
    public SensitivityIndex(String saveFileName)
    {
        super(GlobalResources.getDateTime() + " Sensitivity Index",null,GlobalResources.DATATYPE_SPATIAL,null);
        source = new DataSourceInfo();
        source.sourceFile=saveFileName;
        source.xField="x";
        source.yField="y";
        source.valueField="value";
        
        this.type=GlobalResources.DATATYPE_SPATIAL;
        
        //now create internal data grid
        float[][] data = new float[GlobalResources.mappingProject.grid.getDimensions().x][GlobalResources.mappingProject.grid.getDimensions().y];
        
        //load all processed grids and calculate mean of sensitivity scores
        ArrayList<float[][]>ecoDataList = new ArrayList<float[][]>();
        float[] weights = new float[GlobalResources.mappingProject.ecocomps.size()];
        for(int l=0;l<GlobalResources.mappingProject.ecocomps.size();l++)
        {
            ecoDataList.add(GlobalResources.mappingProject.ecocomps.get(l).getProcessedGrid().getData());
            weights[l]=GlobalResources.mappingProject.sensitivityScores.getEcocompAverageScore(GlobalResources.mappingProject.ecocomps.get(l).getName()); 
        }
        
        //sum them up, weighted
        float max=0;
        float min=1;
        for(int l=0; l<ecoDataList.size();l++)
        {
            GlobalResources.mappingProject.setProcessingProgressPercent((int) (100*l/ecoDataList.size()));
            
            float[][] ecoData=ecoDataList.get(l);
            float weight = weights[l];
            for(int y=0;y<data[0].length;y++)
            {
                for(int x=0;x<data.length;x++)
                {
                    if(ecoData[x][y]==GlobalResources.NODATAVALUE || data[x][y]==GlobalResources.NODATAVALUE)
                    {
                        data[x][y]=GlobalResources.NODATAVALUE;
                    }
                    else
                    {
                        data[x][y]=data[x][y]+weight*ecoData[x][y];
                        if(data[x][y]>max) {max=data[x][y];}
                        if(data[x][y]<min) {min=data[x][y];}
                    }
                }
            }
        }
        
       grid = new DataGrid(data,max,min,GlobalResources.NODATAVALUE);
    }
    
}
