/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * 
 * @author ast
 */
public class SpatialDataLayer implements DrawableData
{
    protected String name;
    protected DataSourceInfo source;
    protected DataGrid grid;
    protected DataGrid processedGrid;
    protected int type;
    
    private ArrayList<PreProcessor> processingChain=new ArrayList<PreProcessor>();
    protected ArrayList<String> selectiveFactors = new ArrayList<String>(); //data set specific factors to be applied in uncertainty analysis
    
    private boolean processingChainLoaded=false;
    
    public SpatialDataLayer(String name, DataGrid grid, int type, DataSourceInfo source)
    {
        this.name=name;
        this.grid=grid;
        this.type=type;
        this.source=source;
        
        //set processing chain to default: log(x+1)-transformation and then rescaling for stressors, nothing for ecocomps
        if(type==GlobalResources.DATATYPE_ECOCOMP) {processingChain.add(new IdentityProcessor());}
        else if(type==GlobalResources.DATATYPE_STRESSOR) 
        {
            processingChain.add(new LogTransformer());
            processingChain.add(new Rescaler());
        }  
    }
    
    public boolean processingChainEquals(SpatialDataLayer layer2)
    {
        boolean result=true;
        //not equal if different size
        if(this.processingChain.size()!=layer2.getProcessingChain().size())
        {
            result=false;
        }
        else
        {
            //not equal if different processors or parameters
            for(int i=0; i<this.processingChain.size();i++)
            {
                if(!this.processingChain.get(i).getName().equals(layer2.getProcessingChain().get(i).getName()))
                {
                    result=false;
                }
                else
                {
                    double[] p1=this.processingChain.get(i).getParamValues();
                    double[] p2=layer2.getProcessingChain().get(i).getParamValues();
                    if(p1!=null && p2!=null)
                    {    
                        for(int j=0; j<p1.length;j++)
                        {
                            if(p1[j]!=p2[j]) {result=false;}
                        }
                    }    
                    
                }
            }
        }

        return result;
    }
    
    public SpatialDataLayer clone()
    {
        String newName=this.name;
        DataGrid newGrid = this.grid.clone();
        DataSourceInfo newInfo = new DataSourceInfo();
        newInfo.sourceFile=this.source.sourceFile;
        newInfo.valueField=this.source.valueField;
        newInfo.xField=this.source.xField;
        newInfo.yField=this.source.yField;
        
        SpatialDataLayer clone = new SpatialDataLayer(newName,newGrid,this.type,newInfo);
        clone.selectiveFactors=Helpers.deepArrayListCopy(selectiveFactors);
        return clone;
    }
    
    
    protected boolean isProcessingChainLoaded()
    {
        return processingChainLoaded;
    }
    
    protected boolean hasProcessedGrid()
    {
        return (processedGrid!=null);
    }
    
    protected void setProcessingChainLoaded()
    {
        processingChainLoaded=true;
    }
    
    public String getName()
    {
        return name;
    }
    
    
    public DataGrid getGrid()
    {
        return grid;
    }
    
    public DataSourceInfo getSource()
    {
        return source;
    }
    
    public int getType()
    {
        return type;
    }
    
    public ArrayList<PreProcessor> getProcessingChain()
    {
        return processingChain;
    }
    
    public void setProcessingChain(ArrayList<PreProcessor> chain)
    {
        this.processingChain=chain;
        processedGrid=null;
    }
    
    public void needsReprocessing()
    {
        processedGrid=null;
    }

    
    public DataGrid getProcessedGrid()
    {
        //processed grid exists already
        if(processedGrid!=null)
        {
            return processedGrid;
        }
        //processing chain is empty
        else if(processingChain==null || processingChain.size()<1)
        {
            return grid;
        }
        //processed grid needs to be calculated now
        else 
        {
            processedGrid=processingChain.get(0).process(grid);
            for(int i=1;i<processingChain.size();i++)
            {
                processedGrid=processingChain.get(i).process(processedGrid);
            }
            return processedGrid;
        }        
    }

 
    
    @Override
    public BufferedImage getImage(ImageCreator creator, boolean variation) {
        BufferedImage image;
        if(!variation)
        {
              image = creator.createDataImage(getGrid(), GlobalResources.mappingProject.grid.getDimensions());
                   
         }
         else
         {
             image = creator.createDataImage(getProcessedGrid(), GlobalResources.mappingProject.grid.getDimensions());
         }
        
        return image;
               
    }

    @Override
    public int getDrawingDataType() {
       return GlobalResources.DATATYPE_SPATIAL;
    }
    
    public void addSelectiveFactor(String factorName)
    {
        boolean contained=false;
        for(int i=0; i<selectiveFactors.size();i++)
        {
            if(factorName.equals(selectiveFactors.get(i)))
            {
                contained=true;
            }
        }
        if(!contained) {this.selectiveFactors.add(factorName);}
    }
    
    public void removeSelectiveFactor(String factorName)
    {
        int index=-1;
        for(int i=0; i<selectiveFactors.size();i++)
        {
            if(factorName.equals(selectiveFactors.get(i)))
            {
               index=i;
            }
        }
        if(index>=0) {selectiveFactors.remove(index);}
    }
    
    public boolean isSelectiveFactorAssigned(String factor)
    {
        if(selectiveFactors==null) return false;
        boolean isAssigned=false;
        for(int i=0; i< selectiveFactors.size(); i++)
        {
            if(selectiveFactors.get(i).equals(factor))
            {
                isAssigned=true;
            }
        }
        return isAssigned;
    }
}
