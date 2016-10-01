/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import cumimpactsa.Helpers.*;

/**
 *
 * @author ast
 */
public class MCSimulationManager 
{

    //simulation options
    
    public int simulationRuns = 100;
    public String outputFolder="";
    public String prefix="";
    public int threads=1;
    
    public boolean missingStressorData=true; public double missingStressorDataMin=0; public double missingStressorDataMax=0.333;
    public boolean sensitivityScoreErrors=true; public double sensitivityScoreErrorsMin=0; public double sensitivityScoreErrorsMax=0.333;
    
    public boolean stressLinearDecay=true; public double linearDecayMin=0; public double linearDecayMax=20;
    
    public boolean ecologicalThresholds=true; public double ecologicalThresholdMin=0; public double ecologicalThresholdMax=1;
    
    public boolean reducedAnalysisRes=true; public double reducedAnalysisResMin=1; public double reducedAnalysisResMax=4;
    public boolean improvedStressorRes=true;
    
    public boolean impactsAsSum=true; public boolean impactsAsMean=true;
    
    public boolean transformationNone=true; public boolean transformationLog=true; 
    public boolean transformationCut5=true; public boolean transformationPercentile=true;
    
    public boolean multipleEffectsAdditive=true; public boolean multipleEffectsDominant=true; 
    public boolean multipleEffectsDiminishing=true;
    
    boolean createSpatialOutputs = true;
    boolean addRunsToResults = true;
    
    public boolean mapStressorContributions=true;
    
    //multi-threading options
    public boolean processing=false;
    public int percentCompleted=0;
    
    private ArrayList<CellRankInfo> cellInfos;
    
    public ArrayList<StressorRankInfo> stressorInfos;
    public ArrayList<EcocompRankInfo> ecocompInfos;
    public ArrayList<RegionRankInfo> regionInfos;
    
    public MCSimulationManager()
    {
        GlobalResources.mappingProject.grid.getEmptyGrid(); //must be guaranteed to have been called before cloning a simulation manager for multi-threading, because the first call is not thread safe!
        GlobalResources.mappingProject.regions.getGrid().getUniqueDataValues();
    }
    
    public MCSimulationManager clone()
    {
        MCSimulationManager copy = new MCSimulationManager();
        copy.addRunsToResults=addRunsToResults;
        //copy.cellInfos=cellInfos;
        copy.createSpatialOutputs=createSpatialOutputs;
        //copy.ecocompInfos=ecocompInfos;
        copy.ecologicalThresholdMax=ecologicalThresholdMax;
        copy.ecologicalThresholdMin=ecologicalThresholdMin;
        copy.ecologicalThresholds=ecologicalThresholds;
        copy.impactsAsMean=impactsAsMean;
        copy.impactsAsSum=impactsAsSum;
        copy.improvedStressorRes=improvedStressorRes;
        copy.mapStressorContributions=mapStressorContributions;
        copy.missingStressorData=missingStressorData;
        copy.missingStressorDataMax=missingStressorDataMax;
        copy.missingStressorDataMin=missingStressorDataMin;
        copy.multipleEffectsAdditive=multipleEffectsAdditive;
        copy.multipleEffectsDiminishing=multipleEffectsDiminishing;
        copy.multipleEffectsDominant=multipleEffectsDominant;
        copy.outputFolder=outputFolder;
        copy.percentCompleted=percentCompleted;
        copy.linearDecayMax=linearDecayMax;
        copy.linearDecayMin=linearDecayMin;
        copy.stressLinearDecay=stressLinearDecay;
        copy.reducedAnalysisRes=reducedAnalysisRes;
        copy.reducedAnalysisResMax=reducedAnalysisResMax;
        copy.reducedAnalysisResMin=reducedAnalysisResMin;
        copy.sensitivityScoreErrors=sensitivityScoreErrors;
        copy.sensitivityScoreErrorsMax=sensitivityScoreErrorsMax;
        copy.sensitivityScoreErrorsMin=sensitivityScoreErrorsMin;
        copy.simulationRuns=simulationRuns;
        copy.transformationCut5=transformationCut5;
        copy.transformationLog=transformationLog;
        copy.transformationNone=transformationNone;
        copy.transformationPercentile=transformationPercentile;
        return copy;
    }
    
    //creates a deep copy of the list of spatial data layers, so that each simulation run can be using its own copy
    public ArrayList<SpatialDataLayer> makeLayerListClone(ArrayList<SpatialDataLayer> list)
    {
        ArrayList<SpatialDataLayer> cloneList = new ArrayList<SpatialDataLayer>();
        for(int i=0; i<list.size();i++)
        {
            cloneList.add(list.get(i).clone());
        }
        return cloneList;
    }
    
    public void initializeInfoObjects() 
    {
        SensitivityScoreSet scores = GlobalResources.mappingProject.sensitivityScores;
        stressorInfos = new ArrayList<>();
        for(int i =0; i<GlobalResources.mappingProject.stressors.size();i++)
        {
           StressorRankInfo newInfo = new StressorRankInfo();
           newInfo.name = GlobalResources.mappingProject.stressors.get(i).getName();
           if(this.mapStressorContributions) newInfo.contributionMap = GlobalResources.mappingProject.grid.getEmptyGrid();
           for(int s=0; s<scores.getAllScores().size();s++)
           {
               if(scores.getAllScores().get(s).getStressor().getName().equals(newInfo.name))
               {
                   if(scores.getAllScores().get(s).isActive())
                   {
                       newInfo.active=true;
                       break;
                   }
                   else
                   {
                       newInfo.active=false;
                       break;
                   }
               }
           }
          
           stressorInfos.add(newInfo);
        }
        ecocompInfos=new ArrayList<>();
        for(int i =0; i<GlobalResources.mappingProject.ecocomps.size();i++)
        {
           EcocompRankInfo newInfo = new EcocompRankInfo();
           newInfo.name = GlobalResources.mappingProject.ecocomps.get(i).getName();
           newInfo.cellSum = GlobalResources.mappingProject.ecocomps.get(i).getGrid().getCellSum();
           ecocompInfos.add(newInfo);
        }
        ArrayList<Float> regionCodes = GlobalResources.mappingProject.regions.grid.getUniqueDataValues();
 
        regionInfos = new ArrayList<RegionRankInfo>();
        float[][] regionData = GlobalResources.mappingProject.regions.grid.getData();
        
        for(int r=0; r<regionCodes.size();r++)
        {
            RegionRankInfo newInfo = new RegionRankInfo();
            float code = regionCodes.get(r);
            newInfo.regionCode=code;
            //set region area
            for(int x=0; x<regionData.length; x++)
            {
                for(int y=0; y<regionData[0].length; y++)
                {
                    if(regionData[x][y] == code) {newInfo.nrOfCells++;}
                }
            }
            //newInfo.currentStressorImpact=new double[GlobalResources.mappingProject.stressors.size()];
            //newInfo.stressorPercentSum=new double[GlobalResources.mappingProject.stressors.size()];
            regionInfos.add(newInfo);
        } 
    }
    
   /* public ArrayList<SpatialDataLayer> makeLayerListCloneBySelectiveFactor(ArrayList<SpatialDataLayer> list, String factor)
    {
        ArrayList<SpatialDataLayer> cloneList = new ArrayList<SpatialDataLayer>();
        for(int i=0; i<list.size();i++)
        {
            if(list.get(i).isSelectiveFactorAssigned(factor))
            {
                cloneList.add(list.get(i).clone());
            }
            else
            {
                cloneList.add(list.get(i));
            }
        }
        return cloneList;
    }*/
    
    protected void eraseProcessingChains(ArrayList<SpatialDataLayer> list)
    {
        for(int i=0;i<list.size();i++)
        {
            list.get(i).getProcessingChain().clear();
        }
    }
    
    protected void addToProcessingChains(ArrayList<SpatialDataLayer> list,PreProcessor processor)
    {
        for(int i=0;i<list.size();i++)
        {
            list.get(i).getProcessingChain().add(processor);
        }
    }
    

 
    

    public void runMCSimulation()	
    {
        initializeInfoObjects();
        //initializeRegions();
        if(createSpatialOutputs)
        {
            cellInfos = new ArrayList<CellRankInfo>();
            float[][] data = GlobalResources.mappingProject.grid.getEmptyGrid();
            for(int x=0; x<data.length;x++)
            {
                for(int y=0; y<data[0].length;y++)
                {
                    if(data[x][y]!=GlobalResources.NODATAVALUE)
                    {
                        CellRankInfo newInfo = new CellRankInfo();
                        newInfo.x=x;
                        newInfo.y=y; 
                        cellInfos.add(newInfo);
                    }
                }
            }
        }
        else
        {
            cellInfos=null;
        }
        try
        {
            GlobalResources.statusWindow.setProgress(0);
            GlobalResources.statusWindow.println(this.prefix+" found "+cellInfos.size()+" grid cells with data.");
            //run simulation the specified number of times
            for(int run=0;run<simulationRuns;run++)
            {
                 GlobalResources.statusWindow.println(prefix+": Simulation run: " + run);
                //make deep copies of model inputs   
                ArrayList<SpatialDataLayer> stressors = makeLayerListClone(GlobalResources.mappingProject.stressors);
                eraseProcessingChains(stressors);
                ArrayList<SpatialDataLayer> ecocomps = makeLayerListClone(GlobalResources.mappingProject.ecocomps);
                eraseProcessingChains(ecocomps);
                SensitivityScoreSet scores = GlobalResources.mappingProject.sensitivityScores.clone(stressors, ecocomps);
                stressLinearDecay(stressors);
                setReducedAnalysisRes(stressors, ecocomps);
                setImprovedStressorResolution(stressors);
                addTransformations(stressors);
                addToProcessingChains(stressors,new Rescaler());

                int activeStressors=disableStressorData(stressors, scores);

                changeSensitivityScores(scores);

                setResponseFunctions(scores);
                
            
                //run simulation
                Simulator simulator = new Simulator(stressors,ecocomps, GlobalResources.mappingProject.regions, scores,getMEM(), getImpactModel(),stressorInfos,ecocompInfos,regionInfos,prefix);
                //simulator.getResult().name="Simulation run "+run;
                if(addRunsToResults) {GlobalResources.mappingProject.results.add(simulator.getResult());} 

                updateStressorResults(stressorInfos);
                updateEcocompResults(ecocompInfos);
                updateRegionResults(regionInfos);
                if(createSpatialOutputs) {updateCellResults(simulator.getResult());}
            }

        } 
        catch (Exception e) {
             GlobalResources.statusWindow.println("ERROR IN THREAD: "+this.prefix);
             GlobalResources.statusWindow.println(e);
        }
    }

    public void writeResults()
    {
             GlobalResources.statusWindow.println("Writing Monte Carlo simulation results...");
            writeRegionResults(regionInfos);
            writeStressorResults(stressorInfos);
            writeEcocompResults(ecocompInfos);
            writeStressorContributionResults(regionInfos,stressorInfos);
            if(createSpatialOutputs) {writeCellResults(cellInfos);}
            if(mapStressorContributions) {writeStressorContributionMaps(stressorInfos);}
    }
    

    protected void changeSensitivityScores(SensitivityScoreSet scores)
    {
        if(!this.sensitivityScoreErrors) {return;}
        float[] scoreErrors = new float[scores.size()];
        for(int i=0; i<scoreErrors.length;i++) {scoreErrors[i] = (float) ((scores.getMax()-scores.getMin())-2*(Math.random()*(scores.getMax()-scores.getMin())));} //random errors, can be +/- full score range between original max and min scores
        float parameter = (float) (this.sensitivityScoreErrorsMin+(this.sensitivityScoreErrorsMax-this.sensitivityScoreErrorsMin)*Math.random());
        
        for(int i=0; i<scores.size();i++)
        {
            scores.getAllScores().get(i).changeSensitivtyScore(scores.getInfo(i).getSensitivityScore()+parameter*scoreErrors[i]);
            if(scores.getAllScores().get(i).getSensitivityScore()<scores.getMin()) {scores.getAllScores().get(i).changeSensitivtyScore(scores.getMin());}
            if(scores.getAllScores().get(i).getSensitivityScore()>scores.getMax()) {scores.getAllScores().get(i).changeSensitivtyScore(scores.getMax());}
        }
       
         GlobalResources.statusWindow.println("    "+prefix+"Changed sensitivity weights with errors up to +/- : " + (parameter*(scores.getMax()-scores.getMin())));
    }
    
    /*returns a random set of parameter values
    public double[] getRandomVector2()
    {
        double[] x = new double[9];
        x[0] = Math.round(3*Math.random())/3.0;
        x[1] = Math.round(3*Math.random())/3.0;
        x[2] = Math.round((3*Math.random())/3.0);
        x[3] = Math.round((3*Math.random())/3.0);
        x[4] = Math.round(Math.random());
        x[5] = 0;
        x[6] = Math.round(Math.random());
        x[7] = Math.round(2*Math.random());
        x[8] = Math.round(2*Math.random());
        
        return x;
    }*/
    
    
    protected int disableStressorData(ArrayList<SpatialDataLayer> stressors, SensitivityScoreSet scores)
    {
        if(!this.missingStressorData) {return 0;}
        //set all stressor infos to active
        for(int i=0; i<stressorInfos.size();i++) {stressorInfos.get(i).active=true;}
        
        //randomly select number of stressors to be removed 0.1 0.3 -->
        int removeNr = (int) Math.round((missingStressorDataMin+Math.random()*(missingStressorDataMax-missingStressorDataMin))*stressors.size());
         GlobalResources.statusWindow.println("    "+prefix+": Removing "+removeNr +" stressor layers out of " +stressors.size());
        for(int r=0;r<removeNr;r++)
        {
            //randomly select an item to remove; but set to inactive, rather than removing the stressor!
            int rIndex = (int) Math.floor(Math.random()*stressors.size());
            String removeName = stressors.get(rIndex).getName();
            StressorRankInfo info = getStressorInfoByName(removeName);
            info.active=false;
            boolean isFlagged=false;  //check if already removed (no replacement)
            for(int s=0;s<scores.getAllScores().size();s++)
            {
                if(scores.getAllScores().get(s).getStressor().getName().equals(removeName))
                {
                    if(!scores.getAllScores().get(s).isActive()) {isFlagged=true;}
                    scores.getAllScores().get(s).setActive(false);
                    //isFlagged=true;
                }
            }
            if(isFlagged) r=r-1;
        }
        return stressors.size()-removeNr;
        
    }

    protected void stressLinearDecay(ArrayList<SpatialDataLayer> stressors) 
    {
        if(!this.stressLinearDecay) {return;}
        //create random distance
        double rDistance = linearDecayMin+Math.random()*(linearDecayMax-linearDecayMin);
        
        int layerCount=0;
        for(int i=0; i<stressors.size();i++)
        {
            if(stressors.get(i).isSelectiveFactorAssigned(new IdwSpreader().getName()))
            {
                layerCount++;
                IdwSpreader spreader = new IdwSpreader();
                spreader.setParamValue("distance", rDistance);
                stressors.get(i).getProcessingChain().add(spreader);
            }
        }
         GlobalResources.statusWindow.println("    "+prefix+": Stressor decay distance: "+rDistance+"; for "+layerCount+" data layers.");
    }

    private int getImpactModel() 
    {
        int model=-1;
        String name="";
        if(impactsAsSum && !impactsAsMean) {model = Simulator.IMPACTS_SUM;}
        else if(!impactsAsSum && impactsAsMean) {model = Simulator.IMPACTS_AVG;}
        else
        {
            double r=Math.random();
            if(r<0.5) {model = Simulator.IMPACTS_SUM;name="sum";}
            else {model = Simulator.IMPACTS_AVG;name="mean";}
        }
         GlobalResources.statusWindow.println("    "+prefix+": Using impact model: "+name);
        return model;
    }

    private void setResponseFunctions(SensitivityScoreSet scores) 
    {
        if(!this.ecologicalThresholds) {return;}
        int impactsToChange = (int) Math.round((ecologicalThresholdMin+Math.random()*(ecologicalThresholdMax-ecologicalThresholdMin))*scores.size());
         GlobalResources.statusWindow.println("    "+prefix+": Changing " + impactsToChange + " out of " + scores.size() + " response functions to thresholds.");
        Collections.shuffle(scores.getAllScores()); //shuffling creates a random permutation. Thresholds are assigned to the first impactsToChange entries after shuffling.
        for(int i=0;i<impactsToChange;i++)
        {
            double x0 = Math.random()*0.4+0.3; //location of logistic funmction center
            ThresholdResponse r = new ThresholdResponse();
            r.setX0(x0);
            scores.getAllScores().get(i).setResponseFunction(r);
        }
    }
    
    private void addTransformations(ArrayList<SpatialDataLayer> stressors)
    {
        ArrayList<PreProcessor> options = new ArrayList<PreProcessor>();
        if(transformationNone) {options.add(new IdentityProcessor());}
        if(transformationCut5) {options.add(new PercentCutter());}
        if(transformationLog) {options.add(new LogTransformer());}
        if(transformationPercentile) {options.add(new PercentileTransformer());}
        
        int selected = (int) Math.floor(Math.random()*options.size());
         GlobalResources.statusWindow.println("    "+prefix+": Transformation: " + options.get(selected).getName());
        for(int i=0; i<stressors.size();i++)
        {
            stressors.get(i).getProcessingChain().add(options.get(selected).clone());
        }
    }

    private int getMEM() 
    {
        ArrayList<Integer> options=new ArrayList<Integer>();
        if(multipleEffectsAdditive) {options.add(Simulator.MEM_ADDITIVE);}
        if(multipleEffectsDominant) {options.add(Simulator.MEM_DOMINANT);}
        if(multipleEffectsDiminishing) {options.add(Simulator.MEM_DIMINISHING);}
        
        int selection = (int) Math.floor(Math.random()*options.size());
        
        if(options.get(selection)==Simulator.MEM_ADDITIVE) { GlobalResources.statusWindow.println("    "+prefix+": MEM: Additive");}
        else if(options.get(selection)==Simulator.MEM_DOMINANT) { GlobalResources.statusWindow.println("    "+prefix+": MEM: Dominant");}
        else if(options.get(selection)==Simulator.MEM_DIMINISHING) { GlobalResources.statusWindow.println("    "+prefix+"MEM: Antagonistic");}
        return options.get(selection);
        
    }

    protected void setImprovedStressorResolution(ArrayList<SpatialDataLayer> stressors) 
    {
        if(!this.improvedStressorRes) {return;}
        //improving the resolution is a pre-processing step
        int layerCount=0;
        for(int i=0; i<stressors.size();i++)
        {
            if(stressors.get(i).isSelectiveFactorAssigned(new AreaRefiner().getName()))
            {
                layerCount++;
                AreaRefiner refiner = new AreaRefiner();
                stressors.get(i).getProcessingChain().add(refiner);
            }
        }
         GlobalResources.statusWindow.println("    "+prefix+": Improved resolution for " + layerCount +" data layers.");
    }

    private void setReducedAnalysisRes(ArrayList<SpatialDataLayer> stressors, ArrayList<SpatialDataLayer> ecocomps) 
    {
        if(!this.reducedAnalysisRes) {return;}
        int factor = (int) Math.round(reducedAnalysisResMin + Math.random() * (reducedAnalysisResMax-reducedAnalysisResMin));
        if(!this.reducedAnalysisRes) {return;}
         GlobalResources.statusWindow.println("    "+prefix+": Reducing analysis resolution by factor " + factor);
        if(factor>1) 
        {
            
            for(int i=0; i<stressors.size();i++)
            {
               ResolutionReducer reducer = new ResolutionReducer();
               reducer.setParamValue("factor", factor);
                stressors.get(i).getProcessingChain().add(reducer);
            }
            for(int i=0; i<ecocomps.size();i++)
            {
                ResolutionReducer reducer = new ResolutionReducer();
                reducer.setParamValue("factor", factor);
                ecocomps.get(i).getProcessingChain().add(reducer);
            }
        }
    }


    private void writeRegionResults(ArrayList<RegionRankInfo> regionInfos) 
    {
        CsvTableGeneral table = new CsvTableGeneral();
        table.addColumn("Region");
        table.addColumn("HighestRank");
        table.addColumn("LowestRank");
        table.addColumn("RankRange");
        table.addColumn("InTop25p");
        table.addColumn("InBottom25p");
        
        for(int s=0; s<regionInfos.size(); s++)
        {
            RegionRankInfo info = regionInfos.get(s);
            ArrayList<String> row = new ArrayList<String>();
            row.add(info.regionCode+"");
            row.add(info.maxRank+"");
            row.add(info.minRank+"");
            row.add(info.maxRank-info.minRank+"");
            row.add(info.inTop25p/(0.01*simulationRuns)+"");
            row.add(info.inBottom25p/(0.01*simulationRuns)+"");
            table.addRow(row);
        }
        GlobalResources.statusWindow.println("Writing uncertainty analysis results for regions to:" + new File(outputFolder,"regionranks.csv").getAbsolutePath());
        table.writeToFile(new File(outputFolder,"regionranks.csv").getAbsolutePath());
         
    }

    private void writeStressorContributionResults(ArrayList<RegionRankInfo> regionInfos, ArrayList<StressorRankInfo> stressorInfos) 
    {
        CsvTableGeneral table = new CsvTableGeneral();
        table.addColumn("Region");
        for(int i=0; i<GlobalResources.mappingProject.stressors.size();i++)
        {
            table.addColumn(GlobalResources.mappingProject.stressors.get(i).getName());
        }
        
        /*for(int r=0; r<regionInfos.size(); r++)
        {
            RegionRankInfo info = regionInfos.get(r);
            ArrayList<String> row = new ArrayList<String>();
            row.add(info.regionCode+"");
            for(int s=0; s<GlobalResources.mappingProject.stressors.size();s++)
            {
                double contribution=info.stressorPercentSum[s];
                float included=-1;
                for(int i=0;i<stressorInfos.size();i++)
                {
                    if(stressorInfos.get(i).name.equals(GlobalResources.mappingProject.stressors.get(s).getName()))
                    {
                        included=stressorInfos.get(i).included;
                    }
                }
                row.add(contribution/included+"");
            }
            table.addRow(row);
        }
        GlobalResources.statusWindow.println("Writing uncertainty analysis results for stressors to: "+new File(outputFolder,"stressor_region_contributions.csv").getAbsolutePath());
        table.writeToFile(new File(outputFolder,"stressor_region_contributions.csv").getAbsolutePath());*/
         
    }
    
    private void updateStressorResults(ArrayList<StressorRankInfo> stressorInfos) 
    {
        //count active stressors
        int activeStressors=0;
        for(int i=0; i<stressorInfos.size();i++) {if(stressorInfos.get(i).active) {activeStressors++;}}
        //sort the stressors
       Collections.sort(stressorInfos, new StressorComparator());
       
       int p25 = (int) Math.round(0.25 * activeStressors);
       //int p75 = (int) Math.round(0.75 * activeStressors);
       
       int rank=0;
       for(int i=0; i<stressorInfos.size();i++)
       {
           StressorRankInfo info = stressorInfos.get(i);
           if(info.active)
           {
                rank++;
                float nrank=(float) (1.0-(rank-1.0)/activeStressors);
                if(nrank < info.minRank) 
                    {
                        info.minRank=nrank;
                    }
                if(nrank > info.maxRank) {info.maxRank=nrank;}
                if(rank <= p25) {info.inMostImportant25p++;}
                if(rank > activeStressors-p25 ) {info.inLeastImportant25p++;}
                info.included++;
           }
       }  
    }

    private void writeStressorResults(ArrayList<StressorRankInfo> stressorInfos) 
    {
        CsvTableGeneral table = new CsvTableGeneral();
        table.addColumn("Stressor");
        table.addColumn("Times included");
        table.addColumn("HighestRank");
        table.addColumn("LowestRank");
        table.addColumn("RankRange");
        table.addColumn("InTop25p");
        table.addColumn("InBottom25p");
        
        for(int s=0; s<stressorInfos.size(); s++)
        {
            StressorRankInfo info = stressorInfos.get(s);
            ArrayList<String> row = new ArrayList<String>();
            row.add(info.name);
            row.add(info.included+"");
            row.add(info.maxRank+"");
            row.add(info.minRank+"");
            row.add(info.maxRank-info.minRank+"");
            row.add(info.inMostImportant25p/(0.01*info.included)+"");
            row.add(info.inLeastImportant25p/(0.01*info.included)+"");
            table.addRow(row);
        }
        GlobalResources.statusWindow.println("Writing uncertainty analysis results for stressors to: "+new File(outputFolder,"stressorranks.csv").getAbsolutePath());
        table.writeToFile(new File(outputFolder,"stressorranks.csv").getAbsolutePath());
    }

    private ArrayList<RegionRankInfo> initializeRegions() 
    {
        GlobalResources.statusWindow.println(prefix+ ": Setting up analysis regions;");
        ArrayList<Float> regionCodes = GlobalResources.mappingProject.regions.grid.getUniqueDataValues();
        ArrayList<RegionRankInfo> regionInfos = new ArrayList<RegionRankInfo>();
        float[][] regionData = GlobalResources.mappingProject.regions.grid.getData();
        
        for(int r=0; r<regionCodes.size();r++)
        {
            RegionRankInfo newInfo = new RegionRankInfo();
            float code = regionCodes.get(r);
            newInfo.regionCode=code;
            //set region area
            for(int x=0; x<regionData.length; x++)
            {
                for(int y=0; y<regionData[0].length; y++)
                {
                    if(regionData[x][y] == code) {newInfo.nrOfCells++;}
                }
            }
            //add arrays for stressor contributions in the region
            //newInfo.currentStressorImpact=new double[GlobalResources.mappingProject.stressors.size()];
            //newInfo.stressorPercentSum=new double[GlobalResources.mappingProject.stressors.size()];
            
            regionInfos.add(newInfo);
        } 
        return regionInfos;
    }


    private void updateRegionResults(ArrayList<RegionRankInfo> regionInfos) 
    {
         //calculate mean impact and sort the regions
        for(int i=0; i<regionInfos.size();i++)
        {
            regionInfos.get(i).currentMeanImpact = regionInfos.get(i).currentTotalImpact / regionInfos.get(i).nrOfCells;
        }
       Collections.sort(regionInfos, new RegionComparator());
       
       //save ranks
       int p25 = (int) Math.round(regionInfos.size() * 0.25);
       for(int i=0; i<regionInfos.size();i++)
       {
           RegionRankInfo info = regionInfos.get(i);
           int rank = i+1;
           if(rank < info.minRank) {info.minRank=rank;}
           if(rank > info.maxRank) {info.maxRank=rank;}
           if(rank <= p25) {info.inTop25p++;}
           if(rank > regionInfos.size()-p25) {info.inBottom25p++;}
       
                 
       }
       
       //calculate and store stressor percent contributions
       /*for(int i=0; i<regionInfos.size();i++)
       {
          for(int s=0;s<GlobalResources.mappingProject.stressors.size();s++)
          {
              RegionRankInfo info = regionInfos.get(i);
              info.stressorPercentSum[s]+=100*(info.currentStressorImpact[s]/info.currentTotalImpact);
          }
       }*/

    }

    private void updateEcocompResults(ArrayList<EcocompRankInfo> ecocompInfos) 
    {
        Collections.sort(ecocompInfos, new EcocompComparator());
        int p25 = (int) Math.round(ecocompInfos.size() * 0.25);
        for(int i=0; i<ecocompInfos.size();i++)
        {
           EcocompRankInfo info = ecocompInfos.get(i);
           int rank = i+1;
           if(rank < info.minRank) {info.minRank=rank;}
           if(rank > info.maxRank) {info.maxRank=rank;}
           if(rank <= p25) {info.inMostImportant25p++;}
           if(rank > ecocompInfos.size()-p25) {info.inLeastImportant25p++;}
       }  
    }

    private void writeEcocompResults(ArrayList<EcocompRankInfo> ecocompInfos) 
    {
        CsvTableGeneral table = new CsvTableGeneral();
        table.addColumn("Ecocomp");
        table.addColumn("Cellsum");
        table.addColumn("HighestRank");
        table.addColumn("LowestRank");
        table.addColumn("RankRange");
        table.addColumn("InTop25p");
        table.addColumn("InBottom25p");
        
        for(int i=0; i<ecocompInfos.size(); i++)
        {
            EcocompRankInfo info = ecocompInfos.get(i);
            ArrayList<String> row = new ArrayList<String>();
            row.add(info.name);
            row.add(info.cellSum+"");
            row.add(info.maxRank+"");
            row.add(info.minRank+"");
            row.add(info.maxRank-info.minRank+"");
            row.add(info.inMostImportant25p/(0.01*simulationRuns)+"");
            row.add(info.inLeastImportant25p/(0.01*simulationRuns)+"");
            table.addRow(row);
        }
        GlobalResources.statusWindow.println("Writing uncertainty analysis results for ecosystem components to: "+new File(outputFolder,"ecocompranks.csv").getAbsolutePath());
        table.writeToFile(new File(outputFolder,"ecocompranks.csv").getAbsolutePath());
    }


    private ArrayList<EcocompRankInfo> initializeEcocomps() 
    {
        ArrayList<EcocompRankInfo> infos=new ArrayList<>();
        for(int i =0; i<GlobalResources.mappingProject.ecocomps.size();i++)
        {
           EcocompRankInfo newInfo = new EcocompRankInfo();
           newInfo.name = GlobalResources.mappingProject.ecocomps.get(i).getName();
           newInfo.cellSum = GlobalResources.mappingProject.ecocomps.get(i).getGrid().getCellSum();
           infos.add(newInfo);
        }
        return infos;
    }

    private void updateCellResults(SpatialDataLayer result) 
    {
        if(cellInfos==null) return;
        
        //fill in cellInfos with current values
        for(int i=0; i<cellInfos.size();i++)
        {
            CellRankInfo info = cellInfos.get(i);
            info.currentImpact = result.getGrid().getData()[info.x][info.y];
        }
        
        Collections.sort(cellInfos,new CellComparator());
        
        int p25 = (int) Math.round(cellInfos.size() * 0.25);
        int p05 = (int) Math.round(cellInfos.size() * 0.05);
        int p10 = (int) Math.round(cellInfos.size() * 0.10);
        int p15 = (int) Math.round(cellInfos.size() * 0.15);
        int p20 = (int) Math.round(cellInfos.size() * 0.20);
        int p40 = (int) Math.round(cellInfos.size() * 0.40);
        int p60 = (int) Math.round(cellInfos.size() * 0.60);
        int p80 = (int) Math.round(cellInfos.size() * 0.80);
        for(int i=0; i<cellInfos.size(); i++)
        {
            CellRankInfo info = cellInfos.get(i);
            int rank = i+1;
            if (rank<=p05) {info.inHighest5p++;}
            if(rank<=p10) {info.inHighest10p++;}
            if(rank<=p15) {info.inHighest15p++;}
            if(rank<=p20) {info.inHighest20p++;}
            if(rank<=p25) {info.inHighest25p++;}
            if(rank>cellInfos.size()-(p05)) {info.inLowest5p++;}
            if(rank>cellInfos.size()-(p10)) {info.inLowest10p++;}
            if(rank>cellInfos.size()-(p15)) {info.inLowest15p++;}
            if(rank>cellInfos.size()-(p20)) {info.inLowest20p++;}
            if(rank>cellInfos.size()-(p25)) {info.inLowest25p++;}
            float perc = (float) (1-(1.0*i)/(cellInfos.size()-1));
            if(perc>info.maxPerc) {info.maxPerc=perc;}
            if(perc<info.minPerc) {info.minPerc=perc;}
            info.quantileSum+=perc;
            
            //quintiles
            if(rank<p20) {info.inQuintile[4]++;}
            else if(rank<p40) {info.inQuintile[3]++;}
            else if(rank<p60) {info.inQuintile[2]++;}
            else if(rank<p80) {info.inQuintile[1]++;}
            else {info.inQuintile[0]++;}
            
        }
        
    }

    private void writeCellResults(ArrayList<CellRankInfo> cellInfos) 
    {
        float[][] maxPercentiles = GlobalResources.mappingProject.grid.getEmptyGrid();
        float[][] minPercentiles = GlobalResources.mappingProject.grid.getEmptyGrid();
        float[][] inTop05p = GlobalResources.mappingProject.grid.getEmptyGrid();
        float[][] inTop10p = GlobalResources.mappingProject.grid.getEmptyGrid();
        float[][] inTop15p = GlobalResources.mappingProject.grid.getEmptyGrid();
        float[][] inTop20p = GlobalResources.mappingProject.grid.getEmptyGrid();
        float[][] inTop25p = GlobalResources.mappingProject.grid.getEmptyGrid();
        float[][] inBottom05p = GlobalResources.mappingProject.grid.getEmptyGrid();
        float[][] inBottom10p = GlobalResources.mappingProject.grid.getEmptyGrid();
        float[][] inBottom15p = GlobalResources.mappingProject.grid.getEmptyGrid();
        float[][] inBottom20p = GlobalResources.mappingProject.grid.getEmptyGrid();
        float[][] inBottom25p = GlobalResources.mappingProject.grid.getEmptyGrid();
        float[][] quintile=GlobalResources.mappingProject.grid.getEmptyGrid();
        float[][] quintileVotes=GlobalResources.mappingProject.grid.getEmptyGrid();
        float[][] meanQuantile=GlobalResources.mappingProject.grid.getEmptyGrid();
        
        for(int i=0; i<cellInfos.size();i++)
        {
            CellRankInfo info = cellInfos.get(i);
            maxPercentiles[info.x][info.y] = info.maxPerc;
            minPercentiles[info.x][info.y] = info.minPerc;
            inTop05p[info.x][info.y] = (float) (info.inHighest5p/(0.01*simulationRuns));
            inTop10p[info.x][info.y] = (float) (info.inHighest10p/(0.01*simulationRuns));
            inTop15p[info.x][info.y] = (float) (info.inHighest15p/(0.01*simulationRuns));
            inTop20p[info.x][info.y] = (float) (info.inHighest20p/(0.01*simulationRuns));
            inTop25p[info.x][info.y] = (float) (info.inHighest25p/(0.01*simulationRuns));
            inBottom05p[info.x][info.y] = (float) (info.inLowest5p/(0.01*simulationRuns));
            inBottom10p[info.x][info.y] = (float) (info.inLowest10p/(0.01*simulationRuns));
            inBottom15p[info.x][info.y] = (float) (info.inLowest15p/(0.01*simulationRuns));
            inBottom20p[info.x][info.y] = (float) (info.inLowest20p/(0.01*simulationRuns));
            inBottom25p[info.x][info.y] = (float) (info.inLowest25p/(0.01*simulationRuns));
            
            int highestQ=0;
            int mostVotes=0;
            for(int q=0;q<5;q++)
            {
                if(info.inQuintile[q]>mostVotes)
                {
                    highestQ=q;
                    mostVotes=info.inQuintile[q];
                }    
            }
            quintile[info.x][info.y]=highestQ+1;
            quintileVotes[info.x][info.y]=(float) (mostVotes/(0.01*simulationRuns));
            meanQuantile[info.x][info.y]=(float) (info.quantileSum/(0.01*simulationRuns));
        }
        
        DataGrid maxPGrid = new DataGrid(maxPercentiles, 1, 0, GlobalResources.NODATAVALUE);
        DataGrid minPGrid = new DataGrid(minPercentiles, 1, 0, GlobalResources.NODATAVALUE);
        
        DataGrid top05pGrid = new DataGrid(inTop05p, 100, 0, GlobalResources.NODATAVALUE);
        DataGrid top10pGrid = new DataGrid(inTop10p, 100, 0, GlobalResources.NODATAVALUE);
        DataGrid top15pGrid = new DataGrid(inTop15p, 100, 0, GlobalResources.NODATAVALUE);
        DataGrid top20pGrid = new DataGrid(inTop20p, 100, 0, GlobalResources.NODATAVALUE);
        DataGrid top25pGrid = new DataGrid(inTop25p, 100, 0, GlobalResources.NODATAVALUE);
       
        DataGrid bottom05pGrid = new DataGrid(inBottom05p, 100, 0, GlobalResources.NODATAVALUE);
        DataGrid bottom10pGrid = new DataGrid(inBottom10p, 100, 0, GlobalResources.NODATAVALUE);
        DataGrid bottom15pGrid = new DataGrid(inBottom15p, 100, 0, GlobalResources.NODATAVALUE);
        DataGrid bottom20pGrid = new DataGrid(inBottom20p, 100, 0, GlobalResources.NODATAVALUE);
        DataGrid bottom25pGrid = new DataGrid(inBottom25p, 100, 0, GlobalResources.NODATAVALUE);
        DataGrid quintileGrid = new DataGrid(quintile, 5, 1, GlobalResources.NODATAVALUE);
        DataGrid quintileVotesGrid= new DataGrid(quintileVotes, 100, 0, GlobalResources.NODATAVALUE);
        DataGrid meanQuantileGrid= new DataGrid(meanQuantile, 100, 0, GlobalResources.NODATAVALUE);
        
        CsvTableGeneral table;
        
        if(!GlobalResources.releaseVersion)
        {
            GlobalResources.statusWindow.println("Writing uncertainty analysis results as regular grids to folder: "+outputFolder);
            DataSourceInfo maxPInfo = new DataSourceInfo();
            maxPInfo.sourceFile=new File(outputFolder, "maxp.csv").getAbsolutePath();
            maxPInfo.valueField="value";
            maxPInfo.xField="x";
            maxPInfo.yField="y";
            SpatialDataLayer maxPLayer = new SpatialDataLayer("Maximum quantiles", maxPGrid, GlobalResources.DATATYPE_SPATIAL, maxPInfo);        
            table = GlobalResources.mappingProject.grid.createTableFromLayer(maxPLayer, false);
            table.writeToFile(maxPInfo.sourceFile);        
            GlobalResources.mappingProject.results.add(maxPLayer);
        
            DataSourceInfo minPInfo = new DataSourceInfo();
            minPInfo.sourceFile=new File(outputFolder, "minp.csv").getAbsolutePath();
            minPInfo.valueField="value";
            minPInfo.xField="x";
            minPInfo.yField="y";
            SpatialDataLayer minPLayer = new SpatialDataLayer("Minimum quantiles", minPGrid, GlobalResources.DATATYPE_SPATIAL, minPInfo);        
            table = GlobalResources.mappingProject.grid.createTableFromLayer(minPLayer, false);
            table.writeToFile(minPInfo.sourceFile);        
            GlobalResources.mappingProject.results.add(minPLayer);
            
            DataSourceInfo quintileInfo = new DataSourceInfo();
            quintileInfo.sourceFile=new File(outputFolder, "quintiles.csv").getAbsolutePath();
            quintileInfo.valueField="value";
            quintileInfo.xField="x";
            quintileInfo.yField="y";
            SpatialDataLayer quintileLayer = new SpatialDataLayer("Quintile - majority vote", quintileGrid, GlobalResources.DATATYPE_SPATIAL, quintileInfo);        
            table = GlobalResources.mappingProject.grid.createTableFromLayer(quintileLayer, false);
            table.writeToFile(quintileInfo.sourceFile);        
            GlobalResources.mappingProject.results.add(quintileLayer);

            DataSourceInfo quintileVotesInfo = new DataSourceInfo();
            quintileVotesInfo.sourceFile=new File(outputFolder, "quintileVotes.csv").getAbsolutePath();
            quintileVotesInfo.valueField="value";
            quintileVotesInfo.xField="x";
            quintileVotesInfo.yField="y";
            SpatialDataLayer quintileVotesLayer = new SpatialDataLayer("Quintile votes", quintileVotesGrid, GlobalResources.DATATYPE_SPATIAL, quintileVotesInfo);        
            table = GlobalResources.mappingProject.grid.createTableFromLayer(quintileVotesLayer, false);
            table.writeToFile(quintileVotesInfo.sourceFile);        
            GlobalResources.mappingProject.results.add(quintileVotesLayer);

            DataSourceInfo meanQuantileInfo = new DataSourceInfo();
            meanQuantileInfo.sourceFile=new File(outputFolder, "meanPercentiles.csv").getAbsolutePath();
            meanQuantileInfo.valueField="value";
            meanQuantileInfo.xField="x";
            meanQuantileInfo.yField="y";
            SpatialDataLayer meanQuantilesLayer = new SpatialDataLayer("Mean percentile", meanQuantileGrid, GlobalResources.DATATYPE_SPATIAL, meanQuantileInfo);        
            table = GlobalResources.mappingProject.grid.createTableFromLayer(meanQuantilesLayer, false);
            table.writeToFile(meanQuantileInfo.sourceFile);        
            GlobalResources.mappingProject.results.add(meanQuantilesLayer);
        }
    
        DataSourceInfo top05pInfo = new DataSourceInfo();
        top05pInfo.sourceFile=new File(outputFolder, "highest05p.csv").getAbsolutePath();
        top05pInfo.valueField="value";
        top05pInfo.xField="x";
        top05pInfo.yField="y";
        SpatialDataLayer top05pLayer = new SpatialDataLayer("% in highest 5%", top05pGrid, GlobalResources.DATATYPE_SPATIAL, top05pInfo);        
        table = GlobalResources.mappingProject.grid.createTableFromLayer(top05pLayer, false);
        table.writeToFile(top05pInfo.sourceFile);        
        
        DataSourceInfo top10pInfo = new DataSourceInfo();
        top10pInfo.sourceFile=new File(outputFolder, "highest10p.csv").getAbsolutePath();
        top10pInfo.valueField="value";
        top10pInfo.xField="x";
        top10pInfo.yField="y";
        SpatialDataLayer top10pLayer = new SpatialDataLayer("% in highest 10%", top10pGrid, GlobalResources.DATATYPE_SPATIAL, top10pInfo);        
        table = GlobalResources.mappingProject.grid.createTableFromLayer(top10pLayer, false);
        table.writeToFile(top10pInfo.sourceFile);        
        if(!GlobalResources.releaseVersion) GlobalResources.mappingProject.results.add(top10pLayer);
        
        DataSourceInfo top15pInfo = new DataSourceInfo();
        top15pInfo.sourceFile=new File(outputFolder, "highest15p.csv").getAbsolutePath();
        top15pInfo.valueField="value";
        top15pInfo.xField="x";
        top15pInfo.yField="y";
        SpatialDataLayer top15pLayer = new SpatialDataLayer("% in highest 15%", top15pGrid, GlobalResources.DATATYPE_SPATIAL, top15pInfo);        
        table = GlobalResources.mappingProject.grid.createTableFromLayer(top15pLayer, false);
        table.writeToFile(top15pInfo.sourceFile);        
  
        
        DataSourceInfo top20pInfo = new DataSourceInfo();
        top20pInfo.sourceFile=new File(outputFolder, "highest20p.csv").getAbsolutePath();
        top20pInfo.valueField="value";
        top20pInfo.xField="x";
        top20pInfo.yField="y";
        SpatialDataLayer top20pLayer = new SpatialDataLayer("% in highest 20%", top20pGrid, GlobalResources.DATATYPE_SPATIAL, top20pInfo);        
        table = GlobalResources.mappingProject.grid.createTableFromLayer(top20pLayer, false);
        table.writeToFile(top20pInfo.sourceFile);        
        
        DataSourceInfo top25pInfo = new DataSourceInfo();
        top25pInfo.sourceFile=new File(outputFolder, "highest25p.csv").getAbsolutePath();
        top25pInfo.valueField="value";
        top25pInfo.xField="x";
        top25pInfo.yField="y";
        SpatialDataLayer top25pLayer = new SpatialDataLayer("% in highest 25%", top25pGrid, GlobalResources.DATATYPE_SPATIAL, top25pInfo);        
        table = GlobalResources.mappingProject.grid.createTableFromLayer(top25pLayer, false);
        table.writeToFile(top25pInfo.sourceFile);        
        if(!GlobalResources.releaseVersion)GlobalResources.mappingProject.results.add(top25pLayer);
        
        DataSourceInfo bottom05pInfo = new DataSourceInfo();
        bottom05pInfo.sourceFile=new File(outputFolder, "lowest05p.csv").getAbsolutePath();
        bottom05pInfo.valueField="value";
        bottom05pInfo.xField="x";
        bottom05pInfo.yField="y";
        SpatialDataLayer bottom05pLayer = new SpatialDataLayer("% in lowest 5%", bottom05pGrid, GlobalResources.DATATYPE_SPATIAL, bottom05pInfo);        
        table = GlobalResources.mappingProject.grid.createTableFromLayer(bottom05pLayer, false);
        table.writeToFile(bottom05pInfo.sourceFile);        
        
        DataSourceInfo bottom10pInfo = new DataSourceInfo();
        bottom10pInfo.sourceFile=new File(outputFolder, "lowest10p.csv").getAbsolutePath();
        bottom10pInfo.valueField="value";
        bottom10pInfo.xField="x";
        bottom10pInfo.yField="y";
        SpatialDataLayer bottom10pLayer = new SpatialDataLayer("% in lowest 10%", bottom10pGrid, GlobalResources.DATATYPE_SPATIAL, bottom10pInfo);        
        table = GlobalResources.mappingProject.grid.createTableFromLayer(bottom10pLayer, false);
        table.writeToFile(bottom10pInfo.sourceFile);        
        if(!GlobalResources.releaseVersion) GlobalResources.mappingProject.results.add(bottom10pLayer);
        
        DataSourceInfo bottom15pInfo = new DataSourceInfo();
        bottom15pInfo.sourceFile=new File(outputFolder, "lowest15p.csv").getAbsolutePath();
        bottom15pInfo.valueField="value";
        bottom15pInfo.xField="x";
        bottom15pInfo.yField="y";
        SpatialDataLayer bottom15pLayer = new SpatialDataLayer("% in lowest 15%", bottom15pGrid, GlobalResources.DATATYPE_SPATIAL, bottom15pInfo);        
        table = GlobalResources.mappingProject.grid.createTableFromLayer(bottom15pLayer, false);
        table.writeToFile(bottom15pInfo.sourceFile);        
        
        DataSourceInfo bottom20pInfo = new DataSourceInfo();
        bottom20pInfo.sourceFile=new File(outputFolder, "lowest20p.csv").getAbsolutePath();
        bottom20pInfo.valueField="value";
        bottom20pInfo.xField="x";
        bottom20pInfo.yField="y";
        SpatialDataLayer bottom20pLayer = new SpatialDataLayer("% in lowest 20%", bottom20pGrid, GlobalResources.DATATYPE_SPATIAL, bottom20pInfo);        
        table = GlobalResources.mappingProject.grid.createTableFromLayer(bottom20pLayer, false);
        table.writeToFile(bottom20pInfo.sourceFile);        
        
        DataSourceInfo bottom25pInfo = new DataSourceInfo();
        bottom25pInfo.sourceFile=new File(outputFolder, "lowest25p.csv").getAbsolutePath();
        bottom25pInfo.valueField="value";
        bottom25pInfo.xField="x";
        bottom25pInfo.yField="y";
        SpatialDataLayer bottom25pLayer = new SpatialDataLayer("% in lowest 25%", bottom25pGrid, GlobalResources.DATATYPE_SPATIAL, bottom25pInfo);        
        table = GlobalResources.mappingProject.grid.createTableFromLayer(bottom25pLayer, false);
        table.writeToFile(bottom25pInfo.sourceFile);        
        if(!GlobalResources.releaseVersion) GlobalResources.mappingProject.results.add(bottom25pLayer);
        
    }

public StressorRankInfo getStressorInfoByName(String name) 
    {
        StressorRankInfo info=null;
        if(stressorInfos==null) {return null;}
        for(int i=0; i<stressorInfos.size();i++)
        {
            if(stressorInfos.get(i).name.equals(name)) {info=stressorInfos.get(i);}
        }
        return info;
    }

    void mergeResults(MCSimulationManager mcm2) 
    {
        //for stressors
        for(int i=0; i<stressorInfos.size();i++)
        {
            for(int j=0; j<mcm2.stressorInfos.size(); j++)
            {
                if(stressorInfos.get(i).name.equals(mcm2.stressorInfos.get(j).name))
                {
                    stressorInfos.get(i).inLeastImportant25p = stressorInfos.get(i).inLeastImportant25p + mcm2.stressorInfos.get(j).inLeastImportant25p;
                    stressorInfos.get(i).inMostImportant25p = stressorInfos.get(i).inMostImportant25p + mcm2.stressorInfos.get(j).inMostImportant25p;
                    stressorInfos.get(i).included = stressorInfos.get(i).included + mcm2.stressorInfos.get(j).included;
                    if(mcm2.stressorInfos.get(j).maxRank>stressorInfos.get(i).maxRank) {stressorInfos.get(i).maxRank=stressorInfos.get(j).maxRank;}
                    if(mcm2.stressorInfos.get(j).minRank<stressorInfos.get(i).minRank) {stressorInfos.get(i).minRank=stressorInfos.get(j).minRank;}
                    if(mapStressorContributions) {Helpers.addToArray2d(stressorInfos.get(i).contributionMap,mcm2.stressorInfos.get(j).contributionMap);}
                }
            }
        }
        
        //for ecocomps
        for(int i=0; i<ecocompInfos.size();i++)
        {
            for(int j=0; j<mcm2.ecocompInfos.size(); j++)
            {
                if(ecocompInfos.get(i).name.equals(mcm2.ecocompInfos.get(j).name))
                {
                    ecocompInfos.get(i).inLeastImportant25p = ecocompInfos.get(i).inLeastImportant25p + mcm2.ecocompInfos.get(j).inLeastImportant25p;
                    ecocompInfos.get(i).inMostImportant25p = ecocompInfos.get(i).inMostImportant25p + mcm2.ecocompInfos.get(j).inMostImportant25p;   
                    if(mcm2.ecocompInfos.get(j).maxRank>ecocompInfos.get(i).maxRank) {ecocompInfos.get(i).maxRank=ecocompInfos.get(j).maxRank;}
                    if(mcm2.ecocompInfos.get(j).minRank<ecocompInfos.get(i).minRank) {ecocompInfos.get(i).minRank=ecocompInfos.get(j).minRank;}
                }
            }
        }
        
        //for regions
        for(int i=0; i<regionInfos.size();i++)
        {
            for(int j=0; j<mcm2.regionInfos.size(); j++)
            {
                if(regionInfos.get(i).regionCode==mcm2.regionInfos.get(j).regionCode)
                {
                    regionInfos.get(i).inTop25p = regionInfos.get(i).inTop25p + mcm2.regionInfos.get(j).inTop25p;
                    regionInfos.get(i).inBottom25p = regionInfos.get(i).inBottom25p + mcm2.regionInfos.get(j).inBottom25p;   
                    if(mcm2.regionInfos.get(j).maxRank>regionInfos.get(i).maxRank) {regionInfos.get(i).maxRank=regionInfos.get(j).maxRank;}
                    if(mcm2.regionInfos.get(j).minRank<regionInfos.get(i).minRank) {regionInfos.get(i).minRank=regionInfos.get(j).minRank;}
                    /*for(int s=0;s<GlobalResources.mappingProject.stressors.size();s++)
                    {
                        regionInfos.get(i).stressorPercentSum[s]+=mcm2.regionInfos.get(i).stressorPercentSum[s];
                    }*/
                }
            }
        }        
        
        //for spatial results
        if(createSpatialOutputs)
        {
            
            //sort both by x and y coordinates
            Collections.sort(cellInfos,new CellComparatorXY());
            Collections.sort(mcm2.cellInfos,new CellComparatorXY());
            
            
            //now the two MCSimulationManager's cell lists are in the same order.
            for(int i=0; i<cellInfos.size();i++)
            {
                cellInfos.get(i).inHighest5p = cellInfos.get(i).inHighest5p + mcm2.cellInfos.get(i).inHighest5p; 
                cellInfos.get(i).inHighest10p = cellInfos.get(i).inHighest10p + mcm2.cellInfos.get(i).inHighest10p; 
                cellInfos.get(i).inHighest15p = cellInfos.get(i).inHighest15p + mcm2.cellInfos.get(i).inHighest15p; 
                cellInfos.get(i).inHighest20p = cellInfos.get(i).inHighest20p + mcm2.cellInfos.get(i).inHighest20p; 
                cellInfos.get(i).inHighest25p = cellInfos.get(i).inHighest25p + mcm2.cellInfos.get(i).inHighest25p;
                cellInfos.get(i).inLowest5p = cellInfos.get(i).inLowest5p + mcm2.cellInfos.get(i).inLowest5p; 
                cellInfos.get(i).inLowest10p = cellInfos.get(i).inLowest10p + mcm2.cellInfos.get(i).inLowest10p; 
                cellInfos.get(i).inLowest15p = cellInfos.get(i).inLowest15p + mcm2.cellInfos.get(i).inLowest15p; 
                cellInfos.get(i).inLowest20p = cellInfos.get(i).inLowest20p + mcm2.cellInfos.get(i).inLowest20p; 
                cellInfos.get(i).inLowest25p = cellInfos.get(i).inLowest25p + mcm2.cellInfos.get(i).inLowest25p;  
                if(mcm2.cellInfos.get(i).maxPerc>cellInfos.get(i).maxPerc) {cellInfos.get(i).maxPerc=cellInfos.get(i).maxPerc;}
                if(mcm2.cellInfos.get(i).minPerc<cellInfos.get(i).minPerc) {cellInfos.get(i).minPerc=cellInfos.get(i).maxPerc;}
                cellInfos.get(i).quantileSum =  cellInfos.get(i).quantileSum + mcm2.cellInfos.get(i).quantileSum;
                for(int q=0; q<5; q++)
                {
                    cellInfos.get(i).inQuintile[q] =  cellInfos.get(i).inQuintile[q] + mcm2.cellInfos.get(i).inQuintile[q];
                }
            }
        }
        simulationRuns+=mcm2.simulationRuns;   
    }

    private void writeStressorContributionMaps(ArrayList<StressorRankInfo> stressorInfos) 
    {

       
       float[][] totalImpacts = GlobalResources.mappingProject.grid.getEmptyGrid();
       for(int x=0; x<totalImpacts.length; x++)
           for(int y=0; y<totalImpacts[0].length; y++)
           {   
               if(totalImpacts[x][y]!=GlobalResources.NODATAVALUE)
               {
                    for(int s=0;s<stressorInfos.size();s++)
                    {
                        StressorRankInfo info = stressorInfos.get(s);
                        totalImpacts[x][y]+=info.contributionMap[x][y]/info.included;
                    }
               }
           }  
       
     
       //now scale all grids to percent
        for(int x=0; x<totalImpacts.length; x++)
           for(int y=0; y<totalImpacts[0].length; y++)
           {   
               if(totalImpacts[x][y]!=GlobalResources.NODATAVALUE)
               {
                    for(int s=0;s<stressorInfos.size();s++)
                    {
                        StressorRankInfo info = stressorInfos.get(s);
                        info.contributionMap[x][y]=100*(info.contributionMap[x][y]/info.included)/totalImpacts[x][y];
                    }
               }
           } 
        
        
        //create data grids and write to CSV
        for(int i=0; i<stressorInfos.size();i++)
        {
            DataGrid contGrid = new DataGrid(stressorInfos.get(i).contributionMap , 100, 0, GlobalResources.NODATAVALUE);
            DataSourceInfo sourceInfo = new DataSourceInfo();
            sourceInfo.sourceFile=new File(outputFolder, "cont_" + stressorInfos.get(i).name+".csv").getAbsolutePath();
            sourceInfo.valueField="value";
            sourceInfo.xField="x";
            sourceInfo.yField="y";
            SpatialDataLayer contLayer = new SpatialDataLayer("Contributions from "+stressorInfos.get(i).name, contGrid, GlobalResources.DATATYPE_SPATIAL, sourceInfo);        
            CsvTableGeneral table = GlobalResources.mappingProject.grid.createTableFromLayer(contLayer, false);
            GlobalResources.statusWindow.println("Writing stressor contributions to: "+sourceInfo.sourceFile);
            table.writeToFile(sourceInfo.sourceFile); 
        }
    }
}