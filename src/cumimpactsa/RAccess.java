/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author andy
 */
public class RAccess 
{
    private static MainWindow mainWindow;
    
    public static double[][] test()
    {
        return new double[5][8];
    }
    
    public static double[][] test2()
    {
        return new double[5][8];
    }
    
    public static void loadProject(String projectFile)
    {
        File f = new File(projectFile);
        mainWindow=new MainWindow();
        mainWindow.setVisible(false);
        mainWindow.loadProject(f,true);
    }
 
    public static int getNrOfStressors()
    {
        if(GlobalResources.mappingProject==null || GlobalResources.mappingProject.stressors==null) return 0;
        else return GlobalResources.mappingProject.stressors.size();
    }
    
    public static int getNrOfEcocomps()
    {
        if(GlobalResources.mappingProject==null || GlobalResources.mappingProject.ecocomps==null) return 0;
        else return GlobalResources.mappingProject.ecocomps.size();
    }
    
    public static float[][] getImpactIndexAdditiveSum()
    {
        return new ImpactIndexAdditive("",GlobalResources.mappingProject.sensitivityScores,false).getGrid().getData();
    }
    
    public static float[][] getImpactIndexAdditiveMean()
    {
        return new ImpactIndexAdditive("",GlobalResources.mappingProject.sensitivityScores,true).getGrid().getData();
    }
    
        
    public static float[][] getImpactIndexDominantSum()
    {
        return new ImpactIndexDominant("",GlobalResources.mappingProject.sensitivityScores,false).getGrid().getData();
    }
    
    public static float[][] getImpactIndexDominantMean()
    {
        return new ImpactIndexDominant("",GlobalResources.mappingProject.sensitivityScores,true).getGrid().getData();
    }
    
        
    public static float[][] getImpactIndexAntagonisticSum()
    {
        return new ImpactIndexDiminishing("",GlobalResources.mappingProject.sensitivityScores,false).getGrid().getData();
    }
    
    public static float[][] getImpactIndexAntagonisticMean()
    {
        return new ImpactIndexDiminishing("",GlobalResources.mappingProject.sensitivityScores,true).getGrid().getData();
    }
    
        
    public static float[][] getDiversityIndex()
    {
        return new DiversityIndex("").getGrid().getData();
    }
    
    public static float[][] getSensitivityIndex()
    {
        return new SensitivityIndex("").getGrid().getData();
    }
    
    public static float[][] getStressorIndex()
    {
        return new StressorIndex("").getGrid().getData();
    }
    
    public static float[][] getWeightedStressorIndex()
    {
        return new WeightedStressorIndex("").getGrid().getData();
    }
    
    public static float getLeft() {return (float) GlobalResources.mappingProject.grid.getWorldCoords(new Point2DInt(0,0)).x;}
    public static float getRight() {return (float) GlobalResources.mappingProject.grid.getWorldCoords(
            new Point2DInt(GlobalResources.mappingProject.grid.getDimensions().x-1,0)).x;}
    public static float getTop() {return (float) GlobalResources.mappingProject.grid.getWorldCoords(new Point2DInt(0,0)).y;}
    public static float getBottom() {return (float) GlobalResources.mappingProject.grid.getWorldCoords(
            new Point2DInt(0,GlobalResources.mappingProject.grid.getDimensions().y-1)).y;}
    public static float getCellSize() {return (float) GlobalResources.mappingProject.grid.getCellSize();}
     public static float getNoDataValue() {return (float) GlobalResources.NODATAVALUE;}
     
    public static String[] getEEFactorNames()
    {
        MorrisSampler ms = new MorrisSampler();
        ms.setup();
        return ms.getFactorNames();
    }
    
    public static float[][][] getEEMaps(boolean rankBased)
    {
        MorrisSampler ms = new MorrisSampler();
        ms.setup();
        ms.calculateEEMaps(rankBased);
        return ms.getEEMaps();
    }
   
    
    public static float[][][][] getEEMapsParallel(boolean rankBased, int threads)
    {
        final EEMapWorker[] workers = new EEMapWorker[threads];

        for(int i=0; i<workers.length; i++)
        {
            workers[i]=new EEMapWorker();
            workers[i].rankBased=rankBased;
            workers[i].working=true; 
        }
        //
        try
        {
            for(int i=0; i<workers.length; i++)
            {
                workers[i].execute();
            }
        }
        catch(Exception e)
        {
     
        }
        
        MorrisSampler[] samplers = new MorrisSampler[threads];
        try
        {
            for(int i=0; i<samplers.length; i++)
            {
                samplers[i] = workers[i].get();
            }
        }
        catch(Exception e) {}
            
        float[][][][] result = new float[threads][][][];
        for(int i=0; i<threads;i++)
        {
            result[i]=samplers[i].getEEMaps();
        }
        return result;
    }
    
    public static float[][] getRegionEEsParallel(int r, boolean rankBased, int threads)
    { 
           
            //create worker threads
            GlobalResources.mappingProject.regions.grid.getUniqueDataValues();
            final MorrisWorker[] workers = new MorrisWorker[threads];
            for(int i=0; i<workers.length; i++)
            {
               workers[i]=new MorrisWorker();
                workers[i].sampleSize=(int) Math.ceil(r*1.0/threads);
                workers[i].workerNr=i+1;
                workers[i].rankBased=rankBased;
                workers[i].working=true; 
            }
            for(int i=0; i<workers.length; i++)
            {
                workers[i].execute();
            }
            MorrisSampler[] samplers = new MorrisSampler[threads];
            try
            {
                for(int i=0; i<samplers.length; i++)
                {
                    samplers[i] = workers[i].get();
                }
            }
            catch(Exception e) 
            {
               System.out.println("Error during EE calculation.");
            }
            for(int i=samplers.length-2; i>=0; i--)
            {
                samplers[i].mergeResults(samplers[i+1]);
            }
            samplers[0].calculateElementaryEffectStatistics(r);
            
            float[][] result=samplers[0].getRegionMuStars();
            return result;
    }
    
    public static int setRegions(String regionFile)
    {
        CsvTableFloat table = new CsvTableFloat(new File(regionFile));
        float[] x = table.getColumn("\"X\"");
        float[] y = table.getColumn("\"Y\"");
        float[] values=table.getColumn("\"value\"");
        
        DataSourceInfo info=new DataSourceInfo();
        info.sourceFile=regionFile;
        info.valueField="\"value\"";
        info.xField="\"X\"";
        info.yField="\"Y\"";
        
        DataGrid grid = GlobalResources.mappingProject.grid.createDataGrid(x, y, values);
        GlobalResources.mappingProject.addData("Regions", grid, GlobalResources.DATATYPE_REGIONS, info);
        return 1;
    }
    
}
