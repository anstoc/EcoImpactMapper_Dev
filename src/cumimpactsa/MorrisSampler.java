/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

/*import static cumimpactsa.MCSimulationManager.addRunsToResults;
import static cumimpactsa.MCSimulationManager.createSpatialOutputs;
import static cumimpactsa.MCSimulationManager.ecologicalThresholdMin;
import static cumimpactsa.MCSimulationManager.ecologicalThresholds;
import static cumimpactsa.MCSimulationManager.improvedStressorRes;
import static cumimpactsa.MCSimulationManager.makeLayerListClone;
import static cumimpactsa.MCSimulationManager.missingStressorData;
import static cumimpactsa.MCSimulationManager.missingStressorDataMax;
import static cumimpactsa.MCSimulationManager.missingStressorDataMin;
import static cumimpactsa.MCSimulationManager.pointStressLinearDecay;
import static cumimpactsa.MCSimulationManager.reducedAnalysisRes;
import static cumimpactsa.MCSimulationManager.sensitivityScoreErrors;
import static cumimpactsa.MCSimulationManager.simulationRuns;*/
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
 
/**
 *
 * @author ast
 */
public class MorrisSampler
{
    private MorrisFactor[] factors=GlobalResources.mappingProject.morrisFactors;
    public String[] parameterNames = new String[factors.length];
    private float[] factorValues=new float[factors.length];
    //public final float[] deltas = new float[]{1.0/3,1.0/3,1.0/3,1.0/3,1,1,1,1};
    public ArrayList<float[]> elementaryEffects;
    
    
    protected float[][][] regionEEMatrices=null;
    protected float[][][] stressorEEMatrices=null;
    protected float[][][] ecocompEEMatrices=null;
    
    //stochastic model parts - must be changed before processing an orientation matrix, but not during processing it,
    //because otherwise they would confound elementary effects
    float[] scoreErrors;
    private ArrayList<Integer> stressorRemoveOrder;
    private ArrayList<Integer> thresholdResponseOrder;
    private ArrayList<Float> thresholdX0List;
    private float[] areaRefinerSeeds;
    
    
    private float[][] regionMuStarMatrix = null; 
    private float[][] regionSigmaMatrix = null;
    private float[][] regionMuMatrix = null; 
    private float[][] regionSigmaStarMatrix = null;
    private float[] regionCodes = null;
    
    private float[][] stressorMuStarMatrix = null;
    private float[][] stressorSigmaMatrix = null;
    private float[][] stressorMuMatrix = null; 
    private float[][] stressorSigmaStarMatrix = null;
    
    private float[][] ecocompMuStarMatrix = null; 
    private float[][] ecocompSigmaMatrix = null;
    private float[][] ecocompMuMatrix = null; 
    private float[][] ecocompSigmaStarMatrix = null;
    
    private ArrayList<SpatialDataLayer> ecocomps;
    private ArrayList<SpatialDataLayer> stressors;
    private SensitivityScoreSet scores;
    private ArrayList<SpatialDataLayer> originalEcocomps;
    private ArrayList<SpatialDataLayer> originalStressors;
    private SensitivityScoreSet originalScores;
    private SpatialDataLayer originalRegions;
    
    private float[][][] eeMaps=null;
    
    public String prefix="";
    
    public void setup()
    {
        //set up parameter names
        parameterNames=MorrisFactor.getFactorNames();
        
        //make copies of the original data
        originalEcocomps=GlobalResources.mappingProject.ecocomps;
        originalStressors=GlobalResources.mappingProject.stressors;
        originalScores=GlobalResources.mappingProject.sensitivityScores;
        originalRegions=GlobalResources.mappingProject.regions;
        
        /*originalEcocomps=makeLayerListClone(GlobalResources.mappingProject.ecocomps);
        originalStressors=makeLayerListClone(GlobalResources.mappingProject.stressors);
        originalScores=GlobalResources.mappingProject.sensitivityScores.clone(originalStressors, originalEcocomps);
        originalRegions=GlobalResources.mappingProject.regions.clone();*/
    }
    
    //returns a vector representing the index for each parameter's level
    public int[] getRandomVector()
    {
        int[] x = new int[GlobalResources.mappingProject.morrisFactors.length];
        
        for(int i=0; i<x.length;i++)
        {
            x[i]=(int) Math.floor(GlobalResources.mappingProject.morrisFactors[i].getNrOfLevels()*Math.random());
        }
        
        /*x[0] = Math.round(GlobalResources.mappingProject.morrisFactors *Math.random())/3.0;
        x[1] = Math.round(3*Math.random())/3.0;
        x[2] = Math.round((3*Math.random())/3.0);
        x[3] = Math.round((3*Math.random())/3.0);
        x[4] = Math.round(Math.random());
        x[5] = Math.round(Math.random());
        x[6] = Math.round(Math.random());
        x[7] = Math.round(2*Math.random());
        x[8] = Math.round(2*Math.random());*/
        
        return x;
    }
    
    //this matrix does not contain the actual factor values as described by e.g. Saltelli et al., but the indices
    //of the factor values to be taken from the coirresponding MorrisFactor instance
    public int[][] getOrientationMatrix()
    {
        //[steps][parameters]
        int[][] matrix = new int[factorValues.length+1][factorValues.length]; //not: transpose of Morris' matrix, for easier array implementation
        int[] x0=getRandomVector();
        
        ArrayList<Integer> indicesToChange = new ArrayList<Integer>();
        for(int c=0; c<factorValues.length; c++)
        {
            indicesToChange.add(c);
        }
        for(int c=0;c<matrix.length; c++)
        {
            //select a random index to change that has not been changed in this loop (trajectory) yet
            int fIndex;
            if(c==0) {fIndex=(int) Math.floor(Math.random()*factorValues.length);}
            else
            {
                int h = (int) Math.floor(Math.random()*indicesToChange.size());
                fIndex = indicesToChange.get(h);
                indicesToChange.remove(h);
            }                                                                               
            
            //make new line
            int[] x;
            if(c>0) {x = matrix[c-1].clone();}
            else {x=x0;}
            matrix[c] = x;
            
            //create array of options for new factor level
            
            int nrOfLevels=factors[fIndex].getNrOfLevels();
            if(nrOfLevels>1)
            {
                int[] options=new int[nrOfLevels-1];
                int counter=0;
                for(int i=0; i<nrOfLevels; i++)
                {
                    if(i!=x[fIndex])
                    {
                        options[counter]=i;
                        counter++;
                    }
                }
                int newLevel=options[(int) Math.floor(options.length*Math.random())];
                x[fIndex]=newLevel;
            }
        }        
        //logOrientationMatrix(matrix);
        return matrix;
    }
    
    public void logOrientationMatrix(int[][] b)
    {
        GlobalResources.statusWindow.println("        "+prefix + "Orientation matrix:");
        for(int i=0; i<b.length; i++)
        {   String row="        ";
            for(int j=0; j<b[0].length;j++)
            {
                row=row+b[i][j]+"; ";
            }
            GlobalResources.statusWindow.println(row);
        }
    }
    
    //returned array contains one rank for each region
    public MappingResults calculateOutputs(int[] parameters, boolean rankBased)	
    {
        
        //ArrayList<SpatialDataLayer> oldStressors=stressors;
        //ArrayList<SpatialDataLayer> oldEcocomps=ecocomps;
        stressors = makeLayerListClone(originalStressors);
        eraseProcessingChains(stressors);
        ecocomps = makeLayerListClone(originalEcocomps);
        eraseProcessingChains(ecocomps);
        scores = originalScores.clone(stressors, ecocomps);

        //make changes according to model factors
        //first the changes that affect stressors' processing chains
        //linear decay
 
        int levelIndex = parameters[2];
        float decayDistance = factors[2].getLevelCodes()[levelIndex];
        setStressLinearDecay(stressors, decayDistance);
      

        //improved stressor resolution
        float improveRes=factors[5].getLevelCodes()[parameters[5]];
        if(parameters[5]>0) setImprovedStressorResolution(stressors);
        else GlobalResources.statusWindow.println("        "+prefix+": No improved stressor resolution.");

        //reduced analysis resolution
        //float reductionFactor=factors[4].getLevelCodes()[parameters[4]];
        if(parameters[4]>0) setReducedAnalysisRes(stressors, ecocomps, 2);//(int) reductionFactor);
        else GlobalResources.statusWindow.println("        "+prefix+": No reduced analysis resolution.");
        
        //transformation
        addTransformations(stressors, factors[7].getLevelCodes()[parameters[7]]);
        addToProcessingChains(stressors,new Rescaler());

        //remove stressors
        int activeStressors=disableStressorData(stressors, scores, factors[0].getLevelCodes()[parameters[0]]);

        //add sensitivity weight errors
        changeSensitivityScores(scores,factors[1].getLevelCodes()[parameters[1]]);

        //set non-linear responses
        if(parameters[3]>0) setResponseFunctions(scores,factors[3].getLevelCodes()[parameters[3]]);        
   
        //run simulation
        Simulator simulator = new Simulator(stressors,ecocomps,originalRegions,scores,getMEM(factors[8].getLevelCodes()[parameters[8]]), getImpactModel(parameters[6]),prefix);
        
        MappingResults results = new MappingResults();
        //GlobalResources.mappingProject.results.add(simulator.getResult());
        ArrayList<float[]> regionResults; 
        if(rankBased) {regionResults=simulator.getRegionCodesAndRanks(originalRegions.getGrid().getData());} //return region ranks in order in which region codes appear
        else {regionResults=simulator.getRegionNormalizedImpact(originalRegions.getGrid().getData());}
        regionCodes = regionResults.get(0);
        results.regionRanks=regionResults.get(1);
        results.stressorRanks=simulator.getStressorRanks();
        results.ecocompRanks=simulator.getEcocompRanks();
        
        return results;
        
    }
    
    //returned 2d array contains model output for the given parameters;
    //if rankBased==true, then the outputs are CDF-transformed; otherwise they are
    //just rescaled so that the maximum is 1.
    private float[][] calculateSpatialOutputs(int[] parameters, boolean rankBased)	
    {
        stressors = makeLayerListClone(originalStressors);
        eraseProcessingChains(stressors);
        ecocomps = makeLayerListClone(originalEcocomps);
        eraseProcessingChains(ecocomps);
        scores = originalScores.clone(stressors, ecocomps);

        //make changes according to model factors
        //first the changes that affect stressors' processing chains
        //linear decay
 
        int levelIndex = parameters[2];
        float decayDistance = factors[2].getLevelCodes()[levelIndex];
        setStressLinearDecay(stressors, decayDistance);
      

        //improved stressor resolution
        float improveRes=factors[5].getLevelCodes()[parameters[5]];
        if(parameters[5]>0) setImprovedStressorResolution(stressors);
        else GlobalResources.statusWindow.println("        "+prefix+": No improved stressor resolution.");

        //reduced analysis resolution
        //float reductionFactor=factors[4].getLevelCodes()[parameters[4]];
        if(parameters[4]>0) setReducedAnalysisRes(stressors, ecocomps, 2);//(int) reductionFactor);
        else GlobalResources.statusWindow.println("        "+prefix+": No reduced analysis resolution.");
        
        //transformation
        addTransformations(stressors, factors[7].getLevelCodes()[parameters[7]]);
        addToProcessingChains(stressors,new Rescaler());

        //remove stressors
        int activeStressors=disableStressorData(stressors, scores, factors[0].getLevelCodes()[parameters[0]]);

        //add sensitivity weight errors
        changeSensitivityScores(scores,factors[1].getLevelCodes()[parameters[1]]);

        //set non-linear responses
        if(parameters[3]>0) setResponseFunctions(scores,factors[3].getLevelCodes()[parameters[3]]);        
   
        //run simulation
        Simulator simulator = new Simulator(stressors,ecocomps,originalRegions,scores,getMEM(factors[8].getLevelCodes()[parameters[8]]), getImpactModel(parameters[6]),prefix);
        DataGrid result=simulator.getResult().getGrid();
        if(rankBased) {result=new PercentileTransformer().process(result);}
        else {result=new Rescaler().process(result);}
        //.System.out.println("Calculated output map with max: "+result.getMax()+" and min: "+result.getMin());
        return result.getData();
        
    }
    

    
    private  int getMEM(float parameter) 
    {
        String desc;
        if(parameter==0) {
            GlobalResources.statusWindow.println("        "+prefix+": Using multiple stressor effects model: Additive");
            return Simulator.MEM_ADDITIVE;}
        else if(parameter==1) {
            GlobalResources.statusWindow.println("        "+prefix+": Using multiple stressor effects model: Dominant");
            return Simulator.MEM_DOMINANT;}
        else {
            GlobalResources.statusWindow.println("        "+prefix+": Using multiple stressor effects model: Antagonistic");
            return Simulator.MEM_DIMINISHING;}
        
    }
    
    private  int getImpactModel(float parameter)
    {
        int model;
        String desc;
        if(parameter==0) {model = Simulator.IMPACTS_SUM;desc="Sum";}
        else {model = Simulator.IMPACTS_AVG;desc="Mean";}
        GlobalResources.statusWindow.println("        "+prefix+": Aggregating impacts on multiple ecosystem components as "+desc+".");
        return model;
    }
    
    /*protected static void eraseProcessingChainsForSpatialDataType(ArrayList<SpatialDataLayer> list, String spatialDataType)
    {
        for(int i=0;i<list.size();i++)
        {
            if(list.get(i).getSpatialDataType().equals(spatialDataType))
            {
                list.get(i).getProcessingChain().clear();
            }
        }
    }*/
    
    private void addTransformations(ArrayList<SpatialDataLayer> stressors, float parameter)
    {
            PreProcessor transformer=null;
            String name="None";
            if(parameter==0) {transformer=new LogTransformer(); name="Log[X+1]";}
            else if(parameter==1) {transformer=new PercentileTransformer();name="CDF";}
            else if(parameter==2) {transformer=new PercentCutter();name="Cut at 99-Percentile";}
           
            if(transformer!=null)
            {    
                for(int i=0; i<stressors.size();i++)
                {
                    stressors.get(i).getProcessingChain().add(transformer);
                }
            }
             GlobalResources.statusWindow.println("        "+prefix+": Using transformation type: "+name);
        }
    
    
    /*private static void addTransformationsForSpatialDataType(ArrayList<SpatialDataLayer> stressors, float parameter, String spatialDataType)
    {
            PreProcessor transformer;
            if(parameter==0) {transformer=new LogTransformer();}
            else if(parameter==1) {transformer=new PercentCutter();}
            else {transformer=new PercentileTransformer();}
           
            for(int i=0; i<stressors.size();i++)
            {
                if(stressors.get(i).getSpatialDataType().equals(spatialDataType))
                {
                    stressors.get(i).getProcessingChain().add(transformer);
                }
            }
        }*/
    
    private void setImprovedStressorResolution(ArrayList<SpatialDataLayer> stressors) 
    {
        
        //improving the resolution is a pre-processing step
        int layerCount=0;
        for(int i=0; i<stressors.size();i++)
        {
            if(stressors.get(i).isSelectiveFactorAssigned(new AreaRefiner().getName()))
            {
                layerCount++;
                AreaRefiner refiner = new AreaRefiner();
                refiner.setParamValue("seed", areaRefinerSeeds[i]);
                stressors.get(i).getProcessingChain().add(refiner);
                stressors.get(i).needsReprocessing();
            }
        }
        GlobalResources.statusWindow.println("        "+prefix+": Improved resolution for "+layerCount+" stressor layers.");
    }
    
    private void setReducedAnalysisRes(ArrayList<SpatialDataLayer> stressors, ArrayList<SpatialDataLayer> ecocomps, int factor) 
    {
            ResolutionReducer reducer = new ResolutionReducer();
            reducer.setParamValue("factor", factor);
            for(int i=0; i<stressors.size();i++)
            {
                stressors.get(i).getProcessingChain().add(reducer);
                stressors.get(i).needsReprocessing();
            }
            for(int i=0; i<ecocomps.size();i++)
            {
                ecocomps.get(i).getProcessingChain().add(reducer);
                ecocomps.get(i).needsReprocessing();
            }
            GlobalResources.statusWindow.println("        "+prefix+": Reduced analysis resolution by factor "+factor);
    }
    
    
     /*private static void setReducedAnalysisResForDataType(ArrayList<SpatialDataLayer> stressors, int factor, String spatialDataType) 
    {

        if(factor>1) 
        {
            ResolutionReducer reducer = new ResolutionReducer();
            reducer.setParamValue("factor", factor);
            for(int i=0; i<stressors.size();i++)
            {
                if(stressors.get(i).getSpatialDataType().equals(spatialDataType))
                {
                    stressors.get(i).getProcessingChain().add(reducer);
                    stressors.get(i).needsReprocessing();
                }
            }

        }
    }   */
    
    private void setResponseFunctions(SensitivityScoreSet scores, float parameter) 
    {
        int impactsToChange = (int) Math.round(parameter*scores.size());
  
        for(int i=0;i<impactsToChange;i++)
        {
            int changeIndex=thresholdResponseOrder.get(i);
            float x0 = thresholdX0List.get(changeIndex); //location of logistic function inflection point
            ThresholdResponse r = new ThresholdResponse();
            r.setX0(x0);
            scores.getAllScores().get(changeIndex).setResponseFunction(r);
        }
        
        GlobalResources.statusWindow.println("        "+prefix+": Set response functions for "+impactsToChange+" ("+(int) Math.round(parameter*100)+"%) of stressor-ecosystem component combinations to thresholds.");
        
    }
    
    protected void setStressLinearDecay(ArrayList<SpatialDataLayer> stressors, float decayDistance) 
    {
        int layerCount=0;
        
        for(int i=0; i<stressors.size();i++)
        {
            if(stressors.get(i).isSelectiveFactorAssigned(new IdwSpreader().getName()))
            {
                layerCount++;
                if(decayDistance>0)
                {
                    IdwSpreader spreader = new IdwSpreader();
                    spreader.setParamValue("distance", decayDistance);
                    stressors.get(i).getProcessingChain().add(spreader);
                    stressors.get(i).needsReprocessing();
                }
            }
        }
        GlobalResources.statusWindow.println("        "+prefix+": Added linear decay to "+decayDistance+" map units to "+layerCount+" stressor layers.");
    }
    
    
    private int disableStressorData(ArrayList<SpatialDataLayer> stressors, SensitivityScoreSet scores, float parameter) 
    {    
        float removeValue = parameter; //parameter now contains the excat proportion to be removed
        int removeNr = (int) Math.round(removeValue * stressors.size());

        if(removeNr == 0) {return stressors.size();}

        for(int r=0;r<removeNr;r++)
        {
            //select an item to remove from predetermined random permutation; but set to inactive, rather than removing the stressor!
            int rIndex = stressorRemoveOrder.get(r);
            String removeName = stressors.get(rIndex).getName();
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
        GlobalResources.statusWindow.println("        "+prefix+": Excluding "+removeNr+" ("+(int) Math.round(100.0*parameter)+"%) of stressors from calculations.");
        return stressors.size() - removeNr;
    }
    
    protected void changeSensitivityScores(SensitivityScoreSet scores, float parameter)
    {
        
        for(int i=0; i<scores.size();i++)
        {
            scores.getAllScores().get(i).changeSensitivtyScore((float)(scores.getInfo(i).getSensitivityScore()+parameter*this.scoreErrors[i]));
            if(scores.getAllScores().get(i).getSensitivityScore()<scores.getMin()) {scores.getAllScores().get(i).changeSensitivtyScore(scores.getMin());}
            if(scores.getAllScores().get(i).getSensitivityScore()>scores.getMax()) {scores.getAllScores().get(i).changeSensitivtyScore(scores.getMax());}
        }
        GlobalResources.statusWindow.println("        "+prefix+": Added random errors up to +/-"+parameter+" times the original range to sensivity weights.");
    }
    
    protected void setStochasticModelComponents(SensitivityScoreSet scores, ArrayList<SpatialDataLayer> stressors)
    {
        //random sensitivity scores
        scoreErrors = new float[scores.size()];
        for(int i=0; i<scoreErrors.length;i++) {scoreErrors[i] = (float) ((scores.getMax()-scores.getMin())-2*(Math.random()*(scores.getMax()-scores.getMin())));} //random errors, can be up to the score range between original max and min scores
    
        //random seeds for area refiner
        areaRefinerSeeds=new float[stressors.size()];
        for(int i=0; i<areaRefinerSeeds.length;i++) {areaRefinerSeeds[i] = (float) (Math.random() * 10000000);} 
        
        //stressor order = first p% are removed if any
        stressorRemoveOrder=new ArrayList();
        thresholdResponseOrder=new ArrayList();
        thresholdX0List=new ArrayList();
        for(int i=0; i<stressors.size(); i++)
        {
            stressorRemoveOrder.add(i);
        }
        for(int j=0; j<scores.getAllScores().size(); j++)
        {
            thresholdResponseOrder.add(j);
            thresholdX0List.add((float) (0.3+0.4*Math.random())); //determines location of threshold
        }
        Collections.shuffle(stressorRemoveOrder);
        Collections.shuffle(thresholdResponseOrder);
    }
    
     
    
    
    
    //returns matrix with elemntary effects: [parameter][region]
    
    public ElementaryEffects calculateElementaryEffects(int[][] orientationMatrix,boolean rankBased)
    {
        //orientation matrix: [steps][parameters]

       //[steps][regions]
        float[][] regionOutputs = new float[orientationMatrix.length][originalRegions.grid.getUniqueDataValues().size()]; 
        float[][] stressorOutputs = new float[orientationMatrix.length][originalStressors.size()];
        float[][] ecocompOutputs = new float[orientationMatrix.length][originalEcocomps.size()]; 
        setStochasticModelComponents(originalScores, originalStressors);
        for(int step=0; step<orientationMatrix.length; step++)
        {
            int[] pVector=orientationMatrix[step];
            GlobalResources.statusWindow.println("    "+prefix+": Calculating elementary effect for orientation matrix row "+step);
            MappingResults results = calculateOutputs(pVector,rankBased); 
            regionOutputs[step] = results.regionRanks;
            stressorOutputs[step] = results.stressorRanks;
            ecocompOutputs[step] = results.ecocompRanks;
            //GlobalResources.statusWindow.println("            "+prefix+" Model evaluations completed: " + step);
        }
        //calculate elementary effects
        //[parameters][regions]
        ElementaryEffects elementaryEffects = new ElementaryEffects();
        elementaryEffects.regionEEffects = new float[orientationMatrix[0].length][regionOutputs[0].length];
        elementaryEffects.stressorEEffects = new float[orientationMatrix[0].length][stressorOutputs[0].length];
        elementaryEffects.ecocompEEffects = new float[orientationMatrix[0].length][ecocompOutputs[0].length];
        for(int i=0; i<orientationMatrix.length-1;i++)
        {
            
            int[] p1 = orientationMatrix[i];
            int[] p2 = orientationMatrix[i+1];
            //find out which parameter changes
            int changeIndex=-1;
            int changedCount=0;
            for(int p=0; p<p1.length;p++)
            {
                if(p1[p]!=p2[p])
                {
                    changeIndex=p;
                    changedCount++;
                }
            }
            if(changeIndex>-1)
            {
                //calculate delta for this factor
                float delta;
                if(factors[changeIndex].isQualitative() && !factors[changeIndex].isNumericalSortingForced())
                {
                    delta=1;
                }
                else 
                {
                    float diff=factors[changeIndex].getLevelCodes()[p2[changeIndex]]-factors[changeIndex].getLevelCodes()[p1[changeIndex]];
                    float range = factors[changeIndex].getRange();

                    delta = diff/range;
                    //if(changeIndex==1) System.out.println("Diff: "+diff+", Range: "+range+"; Delta: "+delta);
                }



                float[] regionOutputs1 = regionOutputs[i];
                float[] regionOutputs2 = regionOutputs[i+1];
                float[] stressorOutputs1 = stressorOutputs[i];
                float[] stressorOutputs2 = stressorOutputs[i+1];
                float[] ecocompOutputs1 = ecocompOutputs[i];
                float[] ecocompOutputs2 = ecocompOutputs[i+1];

                //[parameter][region]
                float[] regionEffects = new float[regionOutputs1.length];
                for(int r=0; r<regionOutputs1.length;r++)
                {
                    float y1=regionOutputs1[r];
                    float y2=regionOutputs2[r];
                    regionEffects[r] = (y2-y1)/delta;
                }
                elementaryEffects.regionEEffects[changeIndex] = regionEffects;

                //[parameter][stressor]
                float[] stressorEffects = new float[stressorOutputs1.length];
                for(int r=0; r<stressorOutputs1.length;r++)
                {
                    float y1=stressorOutputs1[r];
                    float y2=stressorOutputs2[r];
                    if(y1!=GlobalResources.NODATAVALUE && y2!=GlobalResources.NODATAVALUE)
                    {
                        stressorEffects[r] = (y2-y1)/delta;
                    }
                    else
                    {
                        stressorEffects[r] = GlobalResources.NODATAVALUE;
                    }
                }
                elementaryEffects.stressorEEffects[changeIndex] = stressorEffects;

                //[parameter][region]
                float[] ecocompEffects = new float[ecocompOutputs1.length];
                for(int r=0; r<ecocompOutputs1.length;r++)
                {
                    float y1=ecocompOutputs1[r];
                    float y2=ecocompOutputs2[r];
                    ecocompEffects[r] = (y2-y1)/delta;
                }
                elementaryEffects.ecocompEEffects[changeIndex] = ecocompEffects;
                //System.out.println("    Calculated elementary effects for parameters: "+i+ " of "+ (orientationMatrix.length-1));
            }
        }
        GlobalResources.statusWindow.println("    "+prefix+    "Trajectory completed");
        return elementaryEffects;
    }
    
    /**
        Calculates one float[][] grid for each factor, following a random trajectory through factor space.
        The produced grids contain the absolute values of the elementary effects on each grid cell. 
        Results are stored in an internal variable and can be accessed
    **/
    public void calculateEEMaps(boolean rankBased)
    {
        //set up trajectory
        int[][] orientationMatrix = getOrientationMatrix();
        setStochasticModelComponents(originalScores, originalStressors);
        
        //evaluate model for each orientation matrix row
        float[][][] results=new float[orientationMatrix.length][][];
        for(int step=0; step<orientationMatrix.length; step++)
        {
            int[] pVector=orientationMatrix[step];
            GlobalResources.statusWindow.println("    "+prefix+": Calculating elementary effect for orientation matrix row "+step);
            results[step] = calculateSpatialOutputs(pVector,rankBased); 
        }
        //calculate elementary effects
        eeMaps=new float[results.length-1][][];
        
        for(int i=0; i<orientationMatrix.length-1;i++)
        {
            
            int[] p1 = orientationMatrix[i];
            int[] p2 = orientationMatrix[i+1];
            //find out which parameter changes
            int changeIndex=-1;
            int changedCount=0;
            for(int p=0; p<p1.length;p++)
            {
                if(p1[p]!=p2[p])
                {
                    changeIndex=p;
                    changedCount++;
                }
            }
            if(changeIndex>-1)
            {
                //calculate delta for this factor
                float delta;
                if(factors[changeIndex].isQualitative() && !factors[changeIndex].isNumericalSortingForced())
                {
                    delta=1;
                }
                else 
                {
                    float diff=factors[changeIndex].getLevelCodes()[p2[changeIndex]]-factors[changeIndex].getLevelCodes()[p1[changeIndex]];
                    float range = factors[changeIndex].getRange();

                    delta = diff/range;
                    //if(changeIndex==1) System.out.println("Diff: "+diff+", Range: "+range+"; Delta: "+delta);
                }

                float[][] outputs1=results[i];
                float[][] outputs2=results[i+1];
                
                //[parameter][x][y]
                float[][] eeMap = new float[outputs1.length][outputs1[0].length];
                for(int x=0; x<outputs1.length; x++)
                    for(int y=0; y<outputs1[0].length; y++)
                    {
                        float y1=outputs1[x][y];
                        float y2=outputs2[x][y];
                        if(y1==GlobalResources.NODATAVALUE || y2==GlobalResources.NODATAVALUE)
                        {
                            eeMap[x][y]=GlobalResources.NODATAVALUE;
                        }
                        else
                        {    
                            eeMap[x][y] = Math.abs((y2-y1)/delta);
                        }
                    }
               eeMaps[changeIndex] = eeMap;
            }
        }
    }
    
    public float[][][] getEEMaps()
    {
        if(eeMaps==null) return new float[0][][];
        else return eeMaps;
    }
    
    public String[] getFactorNames()
    {
        if(parameterNames!=null) return parameterNames;
        else return new String[0];
    }
    
    public void processTrajectories(int sampleSize, boolean rankBased)
    {
         //[trajectory][parameter][region]                             
        regionEEMatrices = new float[sampleSize][this.factorValues.length][originalRegions.getGrid().getUniqueDataValues().size()];
        stressorEEMatrices = new float[sampleSize][this.factorValues.length][originalStressors.size()];
        ecocompEEMatrices = new float[sampleSize][this.factorValues.length][originalEcocomps.size()];
        for(int r=0; r<sampleSize; r++)
        {
            //System.out.println(prefix+": Processing trajectory: "+r);
            GlobalResources.statusWindow.println(prefix+"Processing trajectory " + (r+1) + " out of " + sampleSize);
            int[][] b = getOrientationMatrix();
            GlobalResources.statusWindow.println("    "+ prefix+"Created orientation matrix");
            ElementaryEffects eE = calculateElementaryEffects(b,rankBased);
            GlobalResources.statusWindow.println("    "+ prefix+"Calculated elementary effects");
            regionEEMatrices[r] = eE.regionEEffects;
            stressorEEMatrices[r]=eE.stressorEEffects;
            ecocompEEMatrices[r]=eE.ecocompEEffects;
        }
    }
    
    public void calculateElementaryEffectStatistics(int sampleSize)
    {
        
        //calculate mu*'s
        //[parameter][region/stressor/ecocomp]
        regionMuStarMatrix = new float[this.factorValues.length][originalRegions.getGrid().getUniqueDataValues().size()];
        regionMuMatrix  = new float[this.factorValues.length][originalRegions.getGrid().getUniqueDataValues().size()];
        int nregs=originalRegions.getGrid().getUniqueDataValues().size();
        //System.out.println("00000   "+nregs);
        for(int p=0; p<this.factorValues.length; p++)
        {    
            for(int region=0; region<originalRegions.getGrid().getUniqueDataValues().size(); region++)
            {
                float muStarSum=0;
                float muSum=0;
                for(int r=0; r<sampleSize; r++)  
                {   
                    //System.out.println("p="+p+"region="+region+"r="+r);
                    //p=0region=2r=2
                    muStarSum+=Math.abs(regionEEMatrices[r][p][region]);
                    muSum+=regionEEMatrices[r][p][region];
                }
                regionMuStarMatrix[p][region] = muStarSum/sampleSize;
                regionMuMatrix[p][region] = muSum/sampleSize;
            }
        }
        regionSigmaMatrix = new float[this.factorValues.length][originalRegions.getGrid().getUniqueDataValues().size()];
        regionSigmaStarMatrix = new float[this.factorValues.length][originalRegions.getGrid().getUniqueDataValues().size()];
        for(int p=0; p<this.factorValues.length; p++)
        {    
            for(int region=0; region<originalRegions.getGrid().getUniqueDataValues().size(); region++)
            {
                float sqResidualSum=0;
                float sqAbsResidualSum=0;
                for(int r=0; r<sampleSize; r++)  
                {
                    sqResidualSum+=Math.pow(regionEEMatrices[r][p][region]-regionMuMatrix[p][region],2);
                    sqAbsResidualSum+=Math.pow(Math.abs(regionEEMatrices[r][p][region])-regionMuStarMatrix[p][region],2);
                }
                regionSigmaMatrix[p][region] = sqResidualSum/(sampleSize-1);
                regionSigmaStarMatrix[p][region] = sqAbsResidualSum/(sampleSize-1);
            }
        }
        //stressors
        stressorMuStarMatrix = new float[this.factorValues.length][originalStressors.size()];
        stressorMuMatrix  = new float[this.factorValues.length][originalStressors.size()];
        for(int p=0; p<this.factorValues.length; p++)
        {    
            for(int stressor=0; stressor<originalStressors.size(); stressor++)
            {
                float muStarSum=0;
                float muSum=0;
                int stressorIncluded=0;
                for(int r=0; r<sampleSize; r++)  
                {   
                    if(stressorEEMatrices[r][p][stressor]!=GlobalResources.NODATAVALUE)
                    {
                        muStarSum+=Math.abs(stressorEEMatrices[r][p][stressor]);
                        muSum+=stressorEEMatrices[r][p][stressor];
                        stressorIncluded++;
                    }
                }
                
                if(stressorIncluded>0)
                {
                    stressorMuStarMatrix[p][stressor] = muStarSum/stressorIncluded;
                    stressorMuMatrix[p][stressor] = muSum/stressorIncluded;
                }
                else
                {
                    stressorMuStarMatrix[p][stressor] = GlobalResources.NODATAVALUE;
                    stressorMuMatrix[p][stressor] = GlobalResources.NODATAVALUE;;
                }
            }
        }
        stressorSigmaMatrix = new float[this.factorValues.length][originalStressors.size()];
        stressorSigmaStarMatrix = new float[this.factorValues.length][originalStressors.size()];
        for(int p=0; p<this.factorValues.length; p++)
        {    
            for(int stressor=0; stressor<originalStressors.size(); stressor++)
            {
                float sqResidualSum=0;
                float sqAbsResidualSum=0;
                int stressorIncluded=0;
                for(int r=0; r<sampleSize; r++)  
                {
                    if(stressorEEMatrices[r][p][stressor]!=GlobalResources.NODATAVALUE)
                    {
                        sqResidualSum+=Math.pow(stressorEEMatrices[r][p][stressor]-stressorMuMatrix[p][stressor],2);
                        sqAbsResidualSum+=Math.pow(Math.abs(stressorEEMatrices[r][p][stressor])-stressorMuStarMatrix[p][stressor],2);
                        stressorIncluded++;
                    }
                }
                if(stressorIncluded>1)
                {
                    stressorSigmaMatrix[p][stressor] = sqResidualSum/(stressorIncluded-1);
                    stressorSigmaStarMatrix[p][stressor] = sqAbsResidualSum/(stressorIncluded-1);
                }
                else
                {
                    stressorSigmaMatrix[p][stressor] = GlobalResources.NODATAVALUE;
                    stressorSigmaStarMatrix[p][stressor] = GlobalResources.NODATAVALUE;
                }
                    
            }
        }
        //ecocomps
        ecocompMuStarMatrix = new float[this.factorValues.length][originalEcocomps.size()];
        ecocompMuMatrix  = new float[this.factorValues.length][originalEcocomps.size()];
        for(int p=0; p<this.factorValues.length; p++)
        {    
            for(int ecocomp=0; ecocomp<originalEcocomps.size(); ecocomp++)
            {
                float muStarSum=0;
                float muSum=0;
                for(int r=0; r<sampleSize; r++)  
                {   
                    muStarSum+=Math.abs(ecocompEEMatrices[r][p][ecocomp]);
                    muSum+=ecocompEEMatrices[r][p][ecocomp];
                }
                ecocompMuStarMatrix[p][ecocomp] = muStarSum/sampleSize;
                ecocompMuMatrix[p][ecocomp] = muSum/sampleSize;
            }
        }
        ecocompSigmaMatrix = new float[this.factorValues.length][originalEcocomps.size()];
        ecocompSigmaStarMatrix = new float[this.factorValues.length][originalEcocomps.size()];
        for(int p=0; p<this.factorValues.length; p++)
        {    
            for(int ecocomp=0; ecocomp<originalEcocomps.size(); ecocomp++)
            {
                float sqResidualSum=0;
                float sqAbsResidualSum=0;
                for(int r=0; r<sampleSize; r++)  
                {
                    sqResidualSum+=Math.pow(ecocompEEMatrices[r][p][ecocomp]-ecocompMuMatrix[p][ecocomp],2);
                    sqAbsResidualSum+=Math.pow(Math.abs(ecocompEEMatrices[r][p][ecocomp])-ecocompMuStarMatrix[p][ecocomp],2);
                }
                ecocompSigmaMatrix[p][ecocomp] = sqResidualSum/(sampleSize-1);
                ecocompSigmaStarMatrix[p][ecocomp] = sqAbsResidualSum/(sampleSize-1);
            }
        }
       
    }
    
    public void saveResults(String outputFolder)
    {
        GlobalResources.statusWindow.println("Saving results to folder: "+outputFolder);
        
        //regions
        if(!GlobalResources.releaseVersion) saveRegionData(regionMuMatrix, new File(outputFolder,"regions_mu.csv").getAbsolutePath());
        saveRegionData(regionMuStarMatrix, new File(outputFolder,"regions_mustar.csv").getAbsolutePath());
        if(!GlobalResources.releaseVersion) saveRegionData(regionSigmaMatrix, new File(outputFolder,"regions_sigma.csv").getAbsolutePath());
        saveRegionData(regionSigmaStarMatrix, new File(outputFolder,"regions_sigmastar.csv").getAbsolutePath());
        
        //stressors
       if(!GlobalResources.releaseVersion)  saveStressorData(stressorMuMatrix, new File(outputFolder,"stressors_mu.csv").getAbsolutePath());
        saveStressorData(stressorMuStarMatrix, new File(outputFolder,"stressors_mustar.csv").getAbsolutePath());
        if(!GlobalResources.releaseVersion) saveStressorData(stressorSigmaMatrix, new File(outputFolder,"stressors_sigma.csv").getAbsolutePath());
        saveStressorData(stressorSigmaStarMatrix, new File(outputFolder,"stressors_sigmastar.csv").getAbsolutePath());
        
        //ecocomps
        if(!GlobalResources.releaseVersion) saveEcocompData(ecocompMuMatrix, new File(outputFolder,"ecocomps_mu.csv").getAbsolutePath());
        saveEcocompData(ecocompMuStarMatrix, new File(outputFolder,"ecocomps_mustar.csv").getAbsolutePath());
        if(!GlobalResources.releaseVersion)  saveEcocompData(ecocompSigmaMatrix, new File(outputFolder,"ecocomps_sigma.csv").getAbsolutePath());
        saveEcocompData(ecocompSigmaStarMatrix, new File(outputFolder,"ecocomps_sigmastar.csv").getAbsolutePath()); 
    }
    
    private void saveRegionData(float[][] data, String file)
    {
        if(data==null) return;
        
        //[parameter][region]
        CsvTableGeneral table = new CsvTableGeneral();
        table.addColumn("Region");
        for(int i=0; i<this.parameterNames.length; i++)
        {
            table.addColumn(parameterNames[i]);
        }
        //one row per region
        for(int r=0; r<data[0].length; r++)
        {
            ArrayList<String> row = new ArrayList<String>();
            row.add(regionCodes[r]+"");
            for(int p=0; p<data.length; p++)
            {
                row.add(data[p][r]+"");
            }
            table.addRow(row);
        }
        table.writeToFile(file);
    }
    
    protected float[][] getRegionMuStars()
    {
        float[][] muStarMatrix = new float[this.regionMuStarMatrix[0].length][this.parameterNames.length+1];
        
        for(int r=0; r<regionMuStarMatrix[0].length; r++)
        {
            //fill in first column: regions
            muStarMatrix[r][0]=regionCodes[r];
            //fill in for the factors
            for(int p=0; p<regionMuStarMatrix.length; p++)
            {
                muStarMatrix[r][p+1]=regionMuStarMatrix[p][r];
            }
        }
        
        return muStarMatrix;
    }
    
    private void saveStressorData(float[][] data, String file)
    {
        if(data==null) return;
        
        //[parameter][region]
        CsvTableGeneral table = new CsvTableGeneral();
        table.addColumn("Stressor");
        for(int i=0; i<this.parameterNames.length; i++)
        {
            table.addColumn(parameterNames[i]);
        }
        //one row per region
        for(int r=0; r<data[0].length; r++)
        {
            ArrayList<String> row = new ArrayList<String>();
            row.add(originalStressors.get(r).getName());
            for(int p=0; p<data.length; p++)
            {
                row.add(data[p][r]+"");
            }
            table.addRow(row);
        }
        table.writeToFile(file);
    }
    
    private void saveEcocompData(float[][] data, String file)
    {
        if(data==null) return;
        
        //[parameter][region]
        CsvTableGeneral table = new CsvTableGeneral();
        table.addColumn("Ecocomp");
        for(int i=0; i<this.parameterNames.length; i++)
        {
            table.addColumn(parameterNames[i]);
        }
        //one row per region
        for(int r=0; r<data[0].length; r++)
        {
            ArrayList<String> row = new ArrayList<String>();
            row.add(originalEcocomps.get(r).getName());
            for(int p=0; p<data.length; p++)
            {
                row.add(data[p][r]+"");
            }
            table.addRow(row);
        }
        table.writeToFile(file);
    }


    private void reuseSpatialData(ArrayList<SpatialDataLayer> stressors, ArrayList<SpatialDataLayer> oldStressors, ArrayList<SpatialDataLayer> ecocomps, ArrayList<SpatialDataLayer> oldEcocomps, SensitivityScoreSet scores) 
    {
        int stressorCount=0;
        int ecocompCount=0;
        for(int i=0; i<stressors.size();i++)
        {
            SpatialDataLayer stressor1=stressors.get(i);
            SpatialDataLayer stressor2=oldStressors.get(i);
            if(stressor1.processingChainEquals(stressor2))
            {
                for(int s=0;s<scores.size();s++)
                {
                    if(scores.getAllScores().get(s).getStressor()==stressor1) {scores.getAllScores().get(s).setStressor(stressor2);}
                }
                stressors.set(i, stressor2);
                stressorCount++;

            }
        }
        for(int i=0; i<ecocomps.size();i++)
        {
            SpatialDataLayer ecocomp1=ecocomps.get(i);
            SpatialDataLayer ecocomp2=oldEcocomps.get(i);
            if(ecocomp1.processingChainEquals(ecocomp2))
            {
                for(int s=0;s<scores.size();s++)
                {
                    if(scores.getAllScores().get(s).getEcocomp()==ecocomp1) {scores.getAllScores().get(s).setEcocomp(ecocomp2);}
                }
                ecocomps.set(i, ecocomp2);
                ecocompCount++;
            }
        }
        //System.out.println("    Reusing "+stressorCount+" stressor and "+ecocompCount+" ecosystem componenet data sets." );
    }

    void mergeResults(MorrisSampler ms2) 
    {
        //protected float[][][] regionEEMatrices=null;
        //protected float[][][] stressorEEMatrices=null;
        //protected float[][][] ecocompEEMatrices=null;
        float[][][] mergedRegionMatrix = new float[regionEEMatrices.length+ms2.regionEEMatrices.length][regionEEMatrices[0].length][regionEEMatrices[0][0].length];
        float[][][] mergedStressorMatrix = new float[stressorEEMatrices.length+ms2.stressorEEMatrices.length][stressorEEMatrices[0].length][stressorEEMatrices[0][0].length];
        float[][][] mergedEcocompMatrix  = new float[ecocompEEMatrices.length+ms2.ecocompEEMatrices.length][ecocompEEMatrices[0].length][ecocompEEMatrices[0][0].length];
    
        //add first set of results
        for(int r=0; r<regionEEMatrices.length; r++)
        {
            mergedRegionMatrix[r]=regionEEMatrices[r];
            mergedStressorMatrix[r]=stressorEEMatrices[r];
            mergedEcocompMatrix[r]=ecocompEEMatrices[r];
        }
        //add 2nd set of results
        for(int r=0; r<ms2.regionEEMatrices.length; r++)
        {
            mergedRegionMatrix[r+regionEEMatrices.length]=ms2.regionEEMatrices[r];
            mergedStressorMatrix[r+regionEEMatrices.length]=ms2.stressorEEMatrices[r];
            mergedEcocompMatrix[r+regionEEMatrices.length]=ms2.ecocompEEMatrices[r];
        }
        
        this.regionEEMatrices=mergedRegionMatrix;
        this.stressorEEMatrices=mergedStressorMatrix;
        this.ecocompEEMatrices=mergedEcocompMatrix;
        
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
    
}
