/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.util.ArrayList;
import javax.swing.JFrame;

/**
 *
 * @author ast
 */
public class DiversityIndex extends SpatialDataLayer
{
    public DiversityIndex(String saveFileName)
    {
        super(GlobalResources.getDateTime() + " Diversity index",null,GlobalResources.DATATYPE_SPATIAL,null);
        source = new DataSourceInfo();
        source.sourceFile=saveFileName;
        source.xField="x";
        source.yField="y";
        source.valueField="value";
        
        this.type=GlobalResources.DATATYPE_SPATIAL;
        
        //now create internal data grid
        float[][] data = new float[GlobalResources.mappingProject.grid.getDimensions().x][GlobalResources.mappingProject.grid.getDimensions().y];
        
        //load all processed ecological component grids
        ArrayList<float[][]> ecocompDataList = new ArrayList<float[][]>();
        for(int l=0;l<GlobalResources.mappingProject.ecocomps.size();l++)
        {
            ecocompDataList.add(GlobalResources.mappingProject.ecocomps.get(l).getProcessedGrid().getData());
        }
        
        //sum them up
        float max=0;
        float min=1;
        for(int l=0; l<ecocompDataList.size();l++)
        {
            GlobalResources.mappingProject.setProcessingProgressPercent((int) (100*l/ecocompDataList.size()));
            float[][] ecocompData=ecocompDataList.get(l);
            for(int y=0;y<data[0].length;y++)
            {
                for(int x=0;x<data.length;x++)
                {
                    if(ecocompData[x][y]==GlobalResources.NODATAVALUE || data[x][y]==GlobalResources.NODATAVALUE)
                    {
                        data[x][y]=GlobalResources.NODATAVALUE;
                    }
                    else
                    {
                        data[x][y]=data[x][y]+ecocompData[x][y];
                        if(data[x][y]>max) {max=data[x][y];}
                        if(data[x][y]<min) {min=data[x][y];}
                    }
                }
            }
        }
        
       grid = new DataGrid(data,max,min,GlobalResources.NODATAVALUE);
       
    }
    
    //same, but uses a different set of ecocoligcal componenents provided as argument, not the open project's
    public DiversityIndex(String saveFileName, ArrayList<SpatialDataLayer> ecocomps)
    {
        super(GlobalResources.getDateTime() + " Diversity index",null,GlobalResources.DATATYPE_SPATIAL,null);
        source = new DataSourceInfo();
        source.sourceFile=saveFileName;
        source.xField="x";
        source.yField="y";
        source.valueField="value";
        
        this.type=GlobalResources.DATATYPE_SPATIAL;
        
        //now create internal data grid
        float[][] data = new float[GlobalResources.mappingProject.grid.getDimensions().x][GlobalResources.mappingProject.grid.getDimensions().y];
        
        //load all processed ecological component grids
        ArrayList<float[][]> ecocompDataList = new ArrayList<float[][]>();
        for(int l=0;l<ecocomps.size();l++)
        {
            ecocompDataList.add(ecocomps.get(l).getProcessedGrid().getData());
        }
        
        //sum them up
        float max=0;
        float min=1;
        for(int l=0; l<ecocompDataList.size();l++)
        {
            GlobalResources.mappingProject.setProcessingProgressPercent((int) (100*l/ecocompDataList.size()));
            float[][] ecocompData=ecocompDataList.get(l);
            for(int y=0;y<data[0].length;y++)
            {
                for(int x=0;x<data.length;x++)
                {
                    if(ecocompData[x][y]==GlobalResources.NODATAVALUE || data[x][y]==GlobalResources.NODATAVALUE)
                    {
                        data[x][y]=GlobalResources.NODATAVALUE;
                    }
                    else
                    {
                        data[x][y]=data[x][y]+ecocompData[x][y];
                        if(data[x][y]>max) {max=data[x][y];}
                        if(data[x][y]<min) {min=data[x][y];}
                    }
                }
            }
        }
        
       grid = new DataGrid(data,max,min,GlobalResources.NODATAVALUE);
       
    }
    
    
}
