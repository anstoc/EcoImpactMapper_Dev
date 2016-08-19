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
import java.util.Date;
import javax.swing.JOptionPane;
 
/**
 *
 * @author ast
 */
public class MorrisSampler
{
    
    public String[] parameterNames=new String[]{"0Missing stressor data","1Sensitivity score errors","2Spreading of point stressors","3Ecological thresholds",
                                    "4Reduced analysis resolution", "5Improved stressor resolution","6Impact model","7Transformation","8Multiple effects model"};

    private double[] parameterValues=new double[]{0,0,0,0,0,0,0,0,0};
    //public final double[] deltas = new double[]{1.0/3,1.0/3,1.0/3,1.0/3,1,1,1,1};
    public ArrayList<double[]> elementaryEffects;
    
    
    protected double[][][] regionEEMatrices=null;
    protected double[][][] stressorEEMatrices=null;
    protected double[][][] ecocompEEMatrices=null;
    
    //stochastic model parts - must be changed before processing an orientation matrix, but not during processing it,
    //because otherwise 
    double[] scoreErrors;
    private ArrayList<Integer> stressorRemoveOrder;
    private ArrayList<Integer> thresholdResponseOrder;
    private ArrayList<Double> thresholdX0List;
    private double[] areaRefinerSeeds;
    
    
    private double[][] regionMuStarMatrix = null; 
    private double[][] regionSigmaMatrix = null;
    private double[][] regionMuMatrix = null; 
    private double[][] regionSigmaStarMatrix = null;
    private float[] regionCodes = null;
    
    private double[][] stressorMuStarMatrix = null;
    private double[][] stressorSigmaMatrix = null;
    private double[][] stressorMuMatrix = null; 
    private double[][] stressorSigmaStarMatrix = null;
    
    private double[][] ecocompMuStarMatrix = null; 
    private double[][] ecocompSigmaMatrix = null;
    private double[][] ecocompMuMatrix = null; 
    private double[][] ecocompSigmaStarMatrix = null;
    
    private ArrayList<SpatialDataLayer> ecocomps;
    private ArrayList<SpatialDataLayer> stressors;
    private SensitivityScoreSet scores;
    private ArrayList<SpatialDataLayer> originalEcocomps;
    private ArrayList<SpatialDataLayer> originalStressors;
    private SensitivityScoreSet originalScores;
    private SpatialDataLayer originalRegions;
    
    public String prefix="";
    
    public void setup()
    {
        originalEcocomps=makeLayerListClone(GlobalResources.mappingProject.ecocomps);
        originalStressors=makeLayerListClone(GlobalResources.mappingProject.stressors);
        originalScores=GlobalResources.mappingProject.sensitivityScores.clone(originalStressors, originalEcocomps);
        originalRegions=GlobalResources.mappingProject.regions.clone();
    }
    
    //returns a random set of parameter values
    public double[] getRandomVector()
    {
        double[] x = new double[parameterValues.length];
        x[0] = Math.round(3*Math.random())/3.0;
        x[1] = Math.round(3*Math.random())/3.0;
        x[2] = Math.round((3*Math.random())/3.0);
        x[3] = Math.round((3*Math.random())/3.0);
        x[4] = Math.round(Math.random());
        x[5] = Math.round(Math.random());
        x[6] = Math.round(Math.random());
        x[7] = Math.round(2*Math.random());
        x[8] = Math.round(2*Math.random());
        
        return x;
    }
    
    public double[][] getOrientationMatrix()
    {
        //[steps][parameters]
        double[][] matrix = new double[parameterValues.length+1][parameterValues.length]; //not: transpose of Morris' matrix die to easier array implementation
        double[] x0=getRandomVector();
        
        ArrayList<Integer> indicesToChange = new ArrayList<Integer>();
        for(int c=0; c<parameterValues.length; c++)
        {
            indicesToChange.add(c);
        }
        for(int c=0;c<matrix.length; c++)
        {
            //select a random index to change
            int pIndex;
            if(c==0) {pIndex=(int) Math.floor(Math.random()*parameterValues.length);}
            else
            {
                int h = (int) Math.floor(Math.random()*indicesToChange.size());
                pIndex = indicesToChange.get(h);
                indicesToChange.remove(h);
            }                                                                               
            
            //this is necessary to exclude x0 from trajectory - Saltelly et al. stress that the model is never evaluated for x0
            //now change the parameter for the new line
            double[] x;
            if(c>0) {x = matrix[c-1].clone();}
            else {x=x0;}
            matrix[c] = x;

            if(pIndex==0) //missing stressor data
            {
                double[] options = new double[3];
                if(x[0]==0) {options[0]=1.0/3;options[1]=2.0/3;options[2]=1;}
                else if(x[0]==1.0/3) {options[0]=0;options[1]=2.0/3;options[2]=1;}
                else if(x[0]==2.0/3) {options[0]=0;options[1]=1.0/3;options[2]=1;}
                else if(x[0]==1) {options[0]=0;options[1]=1.0/3;options[2]=2.0/3;}
                
                x[0] = options[(int) Math.round(2*Math.random())];
                
            }
            else if(pIndex==1) //sensitivity score errors
            {
                double[] options = new double[3];
                if(x[1]==0) {options[0]=1.0/3;options[1]=2.0/3;options[2]=1;}
                else if(x[1]==1.0/3) {options[0]=0;options[1]=2.0/3;options[2]=1;}
                else if(x[1]==2.0/3) {options[0]=0;options[1]=1.0/3;options[2]=1;}
                else if(x[1]==1) {options[0]=0;options[1]=1.0/3;options[2]=2.0/3;}
                
                x[1] = options[(int) Math.round(2*Math.random())];
            }
            else if(pIndex==2) //point stressor spread
            {
                double[] options = new double[3];
                if(x[2]==0) {options[0]=1.0/3;options[1]=2.0/3;options[2]=1;}
                else if(x[2]==1.0/3) {options[0]=0;options[1]=2.0/3;options[2]=1;}
                else if(x[2]==2.0/3) {options[0]=0;options[1]=1.0/3;options[2]=1;}
                else if(x[2]==1) {options[0]=0;options[1]=1.0/3;options[2]=2.0/3;}
                
                x[2] = options[(int) Math.round(2*Math.random())];
            }
            else if(pIndex==3) //thresholds
            {
                double[] options = new double[3];
                if(x[3]==0) {options[0]=1.0/3;options[1]=2.0/3;options[2]=1;}
                else if(x[3]==1.0/3) {options[0]=0;options[1]=2.0/3;options[2]=1;}
                else if(x[3]==2.0/3) {options[0]=0;options[1]=1.0/3;options[2]=1;}
                else if(x[3]==1) {options[0]=0;options[1]=1.0/3;options[2]=2.0/3;}
                
                x[3] = options[(int) Math.round(2*Math.random())];
            }
            else if(pIndex==4) //reduced analysis res 
            {
                /*double[] options = new double[3];
                if(x[4]==0) {options[0]=1.0/3;options[1]=2.0/3;options[2]=1;}
                else if(x[4]==1.0/3) {options[0]=0;options[1]=2.0/3;options[2]=1;}
                else if(x[4]==2.0/3) {options[0]=0;options[1]=1.0/3;options[2]=1;}
                else if(x[4]==1) {options[0]=0;options[1]=1.0/3;options[2]=2.0/3;}*/
                
                
                if(x[4] == 0) {x[4] = 1;}
                else {x[4] = 0;}
            }
            else if(pIndex==5) //imporved stressor res
            {
                if(x[5]==0) {x[5]=1;}
                else {x[5]=0;}
            }
            else if(pIndex==6) //impact model
            {
                if(x[6]==0) {x[6]=1;}
                else {x[6]=0;}
            }
            else if(pIndex==7) //transformation
            {
                double[] options = new double[2];
                if(x[7]==0) {options[0]=1;options[1]=2;}
                else if(x[7]==1) {options[0]=0; options[1]=2;}
                else {options[0]=0; options[1]=1;}
                
                x[7]=options[(int) Math.round(Math.random())];
            }
            else if(pIndex==8) //transformation
            {
                double[] options = new double[2];
                if(x[8]==0) {options[0]=1;options[1]=2;}
                else if(x[8]==1) {options[0]=0; options[1]=2;}
                else {options[0]=0; options[1]=1;}
                
                x[8]=options[(int) Math.round(Math.random())];
            }
        }        
        return matrix;
    }
    
    //returned array contains one rank for each region
    public MappingResults calculateOutputs(double[] parameters)	
    {
        
        ArrayList<SpatialDataLayer> oldStressors=stressors;
        ArrayList<SpatialDataLayer> oldEcocomps=ecocomps;
        stressors = makeLayerListClone(originalStressors);
        eraseProcessingChains(stressors);
        ecocomps = makeLayerListClone(originalEcocomps);
        scores = originalScores.clone(stressors, ecocomps);

        //make changes according to model factors
        //System.out.println("    "+prefix+" Setting point stress lin decay");
        //first the changes that affect stressors
        if(parameters[2]>0) 
        {    
            setPointStressLinearDecay(stressors, parameters[2]);
        }

        if(parameters[5]>0) setImprovedStressorResolution(stressors);

        if(parameters[4]>0) setReducedAnalysisRes(stressors, ecocomps, 2);

        addTransformations(stressors, parameters[7]);
        addToProcessingChains(stressors,new Rescaler());
        
        //speed up calculations by replacing spatial data with unchanged processing chains with their last, fully processed counterpart

        if(oldStressors!=null && oldEcocomps!=null && oldStressors.size()>0 && oldEcocomps.size()>0)
        {
            reuseSpatialData(stressors, oldStressors, ecocomps, oldEcocomps, scores);
        }
        
        int activeStressors=disableStressorData(stressors, scores, parameters[0]);

        changeSensitivityScores(scores,parameters[1]);

        if(parameters[3]>0) setResponseFunctions(scores,parameters[3]);        
   
        //run simulation
        Simulator simulator = new Simulator(stressors,ecocomps,originalRegions,scores,getMEM(parameters[8]), getImpactModel(parameters[6]),prefix);
        //get results
        //System.out.println("    "+prefix+" Disabling stressors");
        MappingResults results = new MappingResults();
        //GlobalResources.mappingProject.results.add(simulator.getResult());
        ArrayList<float[]> regionResults = simulator.getRegionCodesAndRanks(originalRegions.getGrid().getData()); //return region ranks in order in which region codes appear
        regionCodes = regionResults.get(0);
        results.regionRanks=regionResults.get(1);
        results.stressorRanks=simulator.getStressorRanks();
        results.ecocompRanks=simulator.getEcocompRanks();
        
        return results;
        
    }
    
      //returned object contains one rank for each region, stressor and ecocomp
      //this version is more computaionally efficient as it only reprocesses spatial data
      //that change as consequence of parameters
    /*public MappingResults calculateOutputs(double[] parameters, double[] lastParameters)	
    {
        
        Simulator simulator=null;

        //first call, there are no old parameters. Create everything from scratch.
        if(lastParameters==null)
        {
            return calculateOutputs(parameters);
        }
        
        //set up model inputs
        //reset scores on each run (may not always be needed, but is computationally cheap)
        scores = GlobalResources.mappingProject.sensitivityScores.clone(stressors, ecocomps);
        
        //ecocomps need only be changed if analysis resolution changes
        if(parameters[4]!=lastParameters[4])
        {
            ecocomps = makeLayerListClone(GlobalResources.mappingProject.ecocomps);
        }
        
        //stressors need to be completely changed if analysis resolution changes or transformation changes
        if(parameters[4]!=lastParameters[4] || parameters[7]!=lastParameters[7])
        {
            //make deep copies of model inputs
            stressors = makeLayerListClone(GlobalResources.mappingProject.stressors);
            MCSimulationManager.eraseProcessingChains(stressors);
       
            //make changes according to model factors
            int activeStressors = GlobalResources.mappingProject.stressors.size();
            disableStressorData(stressors, scores, parameters[0]);
            changeSensitivityScores(scores,parameters[1]);
            if(parameters[2]>0) 
            {    
                setPointStressLinearDecay(stressors, parameters[2]);
            }
            if(parameters[3]>0) setResponseFunctions(scores,parameters[3]);
            if(parameters[4]>0) setReducedAnalysisRes(stressors, ecocomps, (int) parameters[4]);
            if(parameters[5]>0) setImprovedStressorResolution(stressors);
            addTransformations(stressors, parameters[7]);
                
            //add rescaling as default processing
            MCSimulationManager.addToProcessingChains(stressors,new Rescaler());
                
             //run simulation
        
            simulator = new Simulator(stressors,ecocomps, scores,getMEM(parameters[8]), getImpactModel(parameters[6]));
        }
        //only pre-processing for points changed; thus, only point spatial data need to be recreated
        //note that in the morris simulation, only one parameter changes at a time. i.e. 2 and 5 can't change simultaneously
        else if(parameters[2]!=lastParameters[2])
        {
            //make deep copies of point model inputs
            stressors = MCSimulationManager.makeLayerListCloneBySpatialDataType(GlobalResources.mappingProject.stressors,GlobalResources.SPATIALDATATYPE_POINT);
            eraseProcessingChainsForSpatialDataType(stressors,GlobalResources.SPATIALDATATYPE_POINT);
       
            //make changes according to model factors
            int activeStressors = GlobalResources.mappingProject.stressors.size();
            disableStressorData(stressors, scores, parameters[0]);
            changeSensitivityScores(scores,parameters[1]);
            if(parameters[2]>0) 
            {    
                setPointStressLinearDecay(stressors, parameters[2]);
            }
            if(parameters[3]>0) setResponseFunctions(scores,parameters[3]);
            if(parameters[4]>0) setReducedAnalysisResForDataType(stressors, (int) parameters[4],GlobalResources.SPATIALDATATYPE_POINT);
            //if(parameters[5]>0) setImprovedStressorResolution(stressors); NOT NEEDED AS POINT DATA, NOT POLYGON DATA, HAVE CHANGED
            addTransformationsForSpatialDataType(stressors, parameters[7],GlobalResources.SPATIALDATATYPE_POINT);
                
            //add rescaling as default processing
            MCSimulationManager.addToProcessingChainsBySpatialDataType(stressors,new Rescaler(),GlobalResources.SPATIALDATATYPE_POINT);
                
             //run simulation
        
            simulator = new Simulator(stressors,ecocomps, scores,getMEM(parameters[8]), getImpactModel(parameters[6]));
        }
        //only pre-processing for polygons changed; thus, only polygon spatial data need to be recreated
        else if(parameters[5]!=lastParameters[5])
        {
            //make deep copies of polygon model inputs
            stressors = MCSimulationManager.makeLayerListCloneBySpatialDataType(GlobalResources.mappingProject.stressors,GlobalResources.SPATIALDATATYPE_POLYGON);
            eraseProcessingChainsForSpatialDataType(stressors,GlobalResources.SPATIALDATATYPE_POLYGON);
       
            //make changes according to model factors
            int activeStressors = GlobalResources.mappingProject.stressors.size();
            disableStressorData(stressors, scores, parameters[0]);
            changeSensitivityScores(scores,parameters[1]);
            //if(parameters[2]>0) CAN'T HAVE CHANGED BECAUSE X5 has CHANGED, NOT X2- NO ACTION NEEDED
            //{    
             //   setPointStressLinearDecay(stressors, parameters[2]);
            //}
            if(parameters[3]>0) setResponseFunctions(scores,parameters[3]);
            if(parameters[4]>0) setReducedAnalysisResForDataType(stressors, (int) parameters[4],GlobalResources.SPATIALDATATYPE_POLYGON);
            if(parameters[5]>0) setImprovedStressorResolution(stressors);
            addTransformationsForSpatialDataType(stressors, parameters[7],GlobalResources.SPATIALDATATYPE_POLYGON);
                
            //add rescaling as default processing
            MCSimulationManager.addToProcessingChainsBySpatialDataType(stressors,new Rescaler(),GlobalResources.SPATIALDATATYPE_POLYGON);
                
             //run simulation
        
            simulator = new Simulator(stressors,ecocomps, scores,getMEM(parameters[8]), getImpactModel(parameters[6]));
        }
        
        //get results
        MappingResults results = new MappingResults();
        
        ArrayList<double[]> regionResults = simulator.getRegionCodesAndRanks(GlobalResources.mappingProject.regions.getGrid().getData()); //return region ranks in order in which region codes appear
        regionCodes = regionResults.get(0);
        results.regionRanks=regionResults.get(1);
        results.stressorRanks=simulator.getStressorRanks();
        results.ecocompRanks=simulator.getEcocompRanks();
        
        return results;
        
    }*/
    
    private  int getMEM(double parameter) 
    {
        if(parameter==0) {return Simulator.MEM_ADDITIVE;}
        else if(parameter==1) {return Simulator.MEM_DOMINANT;}
        else {return Simulator.MEM_DIMINISHING;}
    }
    
    private  int getImpactModel(double parameter)
    {
        int model;
        String desc;
        if(parameter==0) {model = Simulator.IMPACTS_SUM;desc="Sum";}
        else {model = Simulator.IMPACTS_AVG;desc="Mean";}
        
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
    
    private void addTransformations(ArrayList<SpatialDataLayer> stressors, double parameter)
    {
            PreProcessor transformer;
            if(parameter==0) {transformer=new LogTransformer();}
            else if(parameter==1) {transformer=new PercentCutter();}
            else {transformer=new PercentileTransformer();}
           
            for(int i=0; i<stressors.size();i++)
            {
               stressors.get(i).getProcessingChain().add(transformer);
            }
        }
    
    
    /*private static void addTransformationsForSpatialDataType(ArrayList<SpatialDataLayer> stressors, double parameter, String spatialDataType)
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
        int polygonDataCount=0;
        for(int i=0; i<stressors.size();i++)
        {
            if(stressors.get(i).getSpatialDataType().equals(GlobalResources.SPATIALDATATYPE_POLYGON))
            {
                polygonDataCount++;
                AreaRefiner refiner = new AreaRefiner();
                refiner.setParamValue("seed", areaRefinerSeeds[i]);
                stressors.get(i).getProcessingChain().add(refiner);
            }
        }
        
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
    
    private void setResponseFunctions(SensitivityScoreSet scores, double parameter) 
    {
        int impactsToChange = (int) Math.round(parameter*scores.size());
  
        //Collections.shuffle(scores.getAllScores()); //shuffling creates a random permutation. Thresholds are assigned to the first impactsToChange entries after shuffling.
        for(int i=0;i<impactsToChange;i++)
        {
            int changeIndex=thresholdResponseOrder.get(i);
            double x0 = thresholdX0List.get(changeIndex); //location of logistic function center
            ThresholdResponse r = new ThresholdResponse();
            r.setX0(x0);
            scores.getAllScores().get(changeIndex).setResponseFunction(r);
        }
    }
    
    protected void setPointStressLinearDecay(ArrayList<SpatialDataLayer> stressors, double parameter) 
    {
        double rDistance = 20000 * parameter;
        int pointDataCount=0;
        for(int i=0; i<stressors.size();i++)
        {
            if(stressors.get(i).getSpatialDataType().equals(GlobalResources.SPATIALDATATYPE_POINT))
            {
                pointDataCount++;
                IdwSpreader spreader = new IdwSpreader();
                spreader.setParamValue("distance", rDistance);
                stressors.get(i).getProcessingChain().add(spreader);
                stressors.get(i).needsReprocessing();
            }
        }

    }
    
    
    private int disableStressorData(ArrayList<SpatialDataLayer> stressors, SensitivityScoreSet scores, double parameter) 
    {    
        double removeValue = (1.0/3)*parameter;
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
      
        return stressors.size() - removeNr;
    }
    
    protected void changeSensitivityScores(SensitivityScoreSet scores, double parameter)
    {
        //double changeScoreWeight=parameter * 0.5;
       
        
        for(int i=0; i<scores.size();i++)
        {
            scores.getAllScores().get(i).changeSensitivtyScore((float)(scores.getInfo(i).getSensitivityScore()+parameter*this.scoreErrors[i]));
            if(scores.getAllScores().get(i).getSensitivityScore()<scores.getMin()) {scores.getAllScores().get(i).changeSensitivtyScore(scores.getMin());}
            if(scores.getAllScores().get(i).getSensitivityScore()>scores.getMax()) {scores.getAllScores().get(i).changeSensitivtyScore(scores.getMax());}
        }

    }
    
    protected void setStochasticModelComponents(SensitivityScoreSet scores, ArrayList<SpatialDataLayer> stressors)
    {
        //random sensitivity scores
        scoreErrors = new double[scores.size()];
        for(int i=0; i<scoreErrors.length;i++) {scoreErrors[i] = 0.5*(scores.getMax()-scores.getMin())-(Math.random()*(scores.getMax()-scores.getMin()));} //random errors, can be +/- 50% of score range between original max and min scores
    
        //random seeds for area refiner
        areaRefinerSeeds=new double[stressors.size()];
        for(int i=0; i<areaRefinerSeeds.length;i++) {areaRefinerSeeds[i] = Math.random() * 10000000;} 
        
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
            thresholdX0List.add(0.3+0.4*Math.random());
        }
        Collections.shuffle(stressorRemoveOrder);
        Collections.shuffle(thresholdResponseOrder);
    }
    
     
    
    
    
    //returns matrix with elemntary effects: [parameter][region]
    
    public ElementaryEffects calculateElementaryEffects(double[][] orientationMatrix)
    {
        //orientation matrix: [steps][parameters]
        
       //[steps][regions]
        float[][] regionOutputs = new float[orientationMatrix.length][originalRegions.grid.getUniqueDataValues().size()]; 
        float[][] stressorOutputs = new float[orientationMatrix.length][originalStressors.size()]; 
        float[][] ecocompOutputs = new float[orientationMatrix.length][originalEcocomps.size()]; 
        setStochasticModelComponents(originalScores, originalStressors);
        for(int step=0; step<orientationMatrix.length; step++)
        {
            double[] pVector=orientationMatrix[step];
            //System.out.println(    "Calculating outputs...");
            MappingResults results = calculateOutputs(pVector);
            regionOutputs[step] = results.regionRanks;
            stressorOutputs[step] = results.stressorRanks;
            ecocompOutputs[step] = results.ecocompRanks;
            System.out.println("    "+prefix+" Model evaluations completed: " + step);
        }
        //System.out.println("    Calculating EEs for trajectory");
        //now calculate elementary effects!
        //[parameters][regions]
        ElementaryEffects elementaryEffects = new ElementaryEffects();
        elementaryEffects.regionEEffects = new double[orientationMatrix[0].length][regionOutputs[0].length];
        elementaryEffects.stressorEEffects = new double[orientationMatrix[0].length][stressorOutputs[0].length];
        elementaryEffects.ecocompEEffects = new double[orientationMatrix[0].length][ecocompOutputs[0].length];
        for(int i=0; i<orientationMatrix.length-1;i++)
        {
            
            double[] p1 = orientationMatrix[i];
            double[] p2 = orientationMatrix[i+1];
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
            double delta;
            if(changeIndex<4) {delta=p2[changeIndex]-p1[changeIndex];}
            else {delta=1;}
            
            float[] regionOutputs1 = regionOutputs[i];
            float[] regionOutputs2 = regionOutputs[i+1];
            float[] stressorOutputs1 = stressorOutputs[i];
            float[] stressorOutputs2 = stressorOutputs[i+1];
            float[] ecocompOutputs1 = ecocompOutputs[i];
            float[] ecocompOutputs2 = ecocompOutputs[i+1];
            
            //[parameter][region]
            double[] regionEffects = new double[regionOutputs1.length];
            for(int r=0; r<regionOutputs1.length;r++)
            {
                double y1=regionOutputs1[r];
                double y2=regionOutputs2[r];
                regionEffects[r] = (y2-y1)/delta;
            }
            elementaryEffects.regionEEffects[changeIndex] = regionEffects;
            
            //[parameter][region]
            double[] stressorEffects = new double[stressorOutputs1.length];
            for(int r=0; r<stressorOutputs1.length;r++)
            {
                double y1=stressorOutputs1[r];
                double y2=stressorOutputs2[r];
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
            double[] ecocompEffects = new double[ecocompOutputs1.length];
            for(int r=0; r<ecocompOutputs1.length;r++)
            {
                double y1=ecocompOutputs1[r];
                double y2=ecocompOutputs2[r];
                ecocompEffects[r] = (y2-y1)/delta;
            }
            elementaryEffects.ecocompEEffects[changeIndex] = ecocompEffects;
            //System.out.println("    Calculated elementary effects for parameters: "+i+ " of "+ (orientationMatrix.length-1));
        }
        System.out.println("    "+prefix+    "Trajectory completed");
        return elementaryEffects;
    }
    
    public void processTrajectories(int sampleSize)
    {
         //[trajectory][parameter][region]
        regionEEMatrices = new double[sampleSize][this.parameterValues.length][originalRegions.getGrid().getUniqueDataValues().size()];
        stressorEEMatrices = new double[sampleSize][this.parameterValues.length][originalStressors.size()];
        ecocompEEMatrices = new double[sampleSize][this.parameterValues.length][originalEcocomps.size()];
        for(int r=0; r<sampleSize; r++)
        {
            System.out.println(prefix+"Processing trajectory " + (r+1) + " out of " + sampleSize);
            double[][] b = getOrientationMatrix();
            //System.out.println("    "+ prefix+"Created orientation matrix");
            ElementaryEffects eE = calculateElementaryEffects(b);
            //System.out.println("    "+ prefix+"Calculated elementary effects");
            regionEEMatrices[r] = eE.regionEEffects;
            stressorEEMatrices[r]=eE.stressorEEffects;
            ecocompEEMatrices[r]=eE.ecocompEEffects;
        }
    }
    
    public void calculateElementaryEffectStatistics(int sampleSize)
    {
        /*//[trajectory][parameter][region]
        regionEEMatrices = new double[sampleSize][this.parameterValues.length][GlobalResources.mappingProject.regions.getGrid().getUniqueDataValues().size()];
        stressorEEMatrices = new double[sampleSize][this.parameterValues.length][GlobalResources.mappingProject.stressors.size()];
        ecocompEEMatrices = new double[sampleSize][this.parameterValues.length][GlobalResources.mappingProject.ecocomps.size()];
        for(int r=0; r<sampleSize; r++)
        {
            System.out.println("Processing trajectory " + (r+1) + " out of " + sampleSize);
            double[][] b = getOrientationMatrix();
            ElementaryEffects eE = calculateElementaryEffects(b);
            regionEEMatrices[r] = eE.regionEEffects;
            stressorEEMatrices[r]=eE.stressorEEffects;
            ecocompEEMatrices[r]=eE.ecocompEEffects;
        }*/
        //calculate mu*'s
        //[parameter][region/stressor/ecocomp]
        regionMuStarMatrix = new double[this.parameterValues.length][originalRegions.getGrid().getUniqueDataValues().size()];
        regionMuMatrix  = new double[this.parameterValues.length][originalRegions.getGrid().getUniqueDataValues().size()];
        for(int p=0; p<this.parameterValues.length; p++)
        {    
            for(int region=0; region<originalRegions.getGrid().getUniqueDataValues().size(); region++)
            {
                double muStarSum=0;
                double muSum=0;
                for(int r=0; r<sampleSize; r++)  
                {   
                    muStarSum+=Math.abs(regionEEMatrices[r][p][region]);
                    muSum+=regionEEMatrices[r][p][region];
                }
                regionMuStarMatrix[p][region] = muStarSum/sampleSize;
                regionMuMatrix[p][region] = muSum/sampleSize;
            }
        }
        regionSigmaMatrix = new double[this.parameterValues.length][originalRegions.getGrid().getUniqueDataValues().size()];
        regionSigmaStarMatrix = new double[this.parameterValues.length][originalRegions.getGrid().getUniqueDataValues().size()];
        for(int p=0; p<this.parameterValues.length; p++)
        {    
            for(int region=0; region<originalRegions.getGrid().getUniqueDataValues().size(); region++)
            {
                double sqResidualSum=0;
                double sqAbsResidualSum=0;
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
        stressorMuStarMatrix = new double[this.parameterValues.length][originalStressors.size()];
        stressorMuMatrix  = new double[this.parameterValues.length][originalStressors.size()];
        for(int p=0; p<this.parameterValues.length; p++)
        {    
            for(int stressor=0; stressor<originalStressors.size(); stressor++)
            {
                double muStarSum=0;
                double muSum=0;
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
        stressorSigmaMatrix = new double[this.parameterValues.length][originalStressors.size()];
        stressorSigmaStarMatrix = new double[this.parameterValues.length][originalStressors.size()];
        for(int p=0; p<this.parameterValues.length; p++)
        {    
            for(int stressor=0; stressor<originalStressors.size(); stressor++)
            {
                double sqResidualSum=0;
                double sqAbsResidualSum=0;
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
        ecocompMuStarMatrix = new double[this.parameterValues.length][originalEcocomps.size()];
        ecocompMuMatrix  = new double[this.parameterValues.length][originalEcocomps.size()];
        for(int p=0; p<this.parameterValues.length; p++)
        {    
            for(int ecocomp=0; ecocomp<originalEcocomps.size(); ecocomp++)
            {
                double muStarSum=0;
                double muSum=0;
                for(int r=0; r<sampleSize; r++)  
                {   
                    muStarSum+=Math.abs(ecocompEEMatrices[r][p][ecocomp]);
                    muSum+=ecocompEEMatrices[r][p][ecocomp];
                }
                ecocompMuStarMatrix[p][ecocomp] = muStarSum/sampleSize;
                ecocompMuMatrix[p][ecocomp] = muSum/sampleSize;
            }
        }
        ecocompSigmaMatrix = new double[this.parameterValues.length][originalEcocomps.size()];
        ecocompSigmaStarMatrix = new double[this.parameterValues.length][originalEcocomps.size()];
        for(int p=0; p<this.parameterValues.length; p++)
        {    
            for(int ecocomp=0; ecocomp<originalEcocomps.size(); ecocomp++)
            {
                double sqResidualSum=0;
                double sqAbsResidualSum=0;
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
        //regions
        saveRegionData(regionMuMatrix, new File(outputFolder,"regions_mu.csv").getAbsolutePath());
        saveRegionData(regionMuStarMatrix, new File(outputFolder,"regions_mustar.csv").getAbsolutePath());
        saveRegionData(regionSigmaMatrix, new File(outputFolder,"regions_sigma.csv").getAbsolutePath());
        saveRegionData(regionSigmaStarMatrix, new File(outputFolder,"regions_sigmastar.csv").getAbsolutePath());
        
        //stressors
        saveStressorData(stressorMuMatrix, new File(outputFolder,"stressors_mu.csv").getAbsolutePath());
        saveStressorData(stressorMuStarMatrix, new File(outputFolder,"stressors_mustar.csv").getAbsolutePath());
        saveStressorData(stressorSigmaMatrix, new File(outputFolder,"stressors_sigma.csv").getAbsolutePath());
        saveStressorData(stressorSigmaStarMatrix, new File(outputFolder,"stressors_sigmastar.csv").getAbsolutePath());
        
        //ecocomps
        saveEcocompData(ecocompMuMatrix, new File(outputFolder,"ecocomps_mu.csv").getAbsolutePath());
        saveEcocompData(ecocompMuStarMatrix, new File(outputFolder,"ecocomps_mustar.csv").getAbsolutePath());
        saveEcocompData(ecocompSigmaMatrix, new File(outputFolder,"ecocomps_sigma.csv").getAbsolutePath());
        saveEcocompData(ecocompSigmaStarMatrix, new File(outputFolder,"ecocomps_sigmastar.csv").getAbsolutePath()); 
    }
    
    private void saveRegionData(double[][] data, String file)
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
    
    private void saveStressorData(double[][] data, String file)
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
    
    private void saveEcocompData(double[][] data, String file)
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
        //protected double[][][] regionEEMatrices=null;
        //protected double[][][] stressorEEMatrices=null;
        //protected double[][][] ecocompEEMatrices=null;
        double[][][] mergedRegionMatrix = new double[regionEEMatrices.length+ms2.regionEEMatrices.length][regionEEMatrices[0].length][regionEEMatrices[0][0].length];
        double[][][] mergedStressorMatrix = new double[stressorEEMatrices.length+ms2.stressorEEMatrices.length][stressorEEMatrices[0].length][stressorEEMatrices[0][0].length];
        double[][][] mergedEcocompMatrix  = new double[ecocompEEMatrices.length+ms2.ecocompEEMatrices.length][ecocompEEMatrices[0].length][ecocompEEMatrices[0][0].length];
    
        //add first set of results
        for(int r=0; r<regionEEMatrices.length; r++)
        {
            mergedRegionMatrix[r]=regionEEMatrices[r];
            mergedStressorMatrix[r]=stressorEEMatrices[r];
            mergedEcocompMatrix[r]=ecocompEEMatrices[r];
        }
        //add 2nd set of results
        for(int r=0; r<regionEEMatrices.length; r++)
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
