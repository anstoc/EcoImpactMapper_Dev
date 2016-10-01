/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
//import java.util.HashSet;
//import java.util.Set;

/**
 *
 * @author ast
 */
class Simulator 
{
    private ArrayList<SpatialDataLayer> stressors; 
    private ArrayList<SpatialDataLayer> ecocomps; 
    private SpatialDataLayer regions;
    private SensitivityScoreSet scores;
    private int mem;
    private int meanOrSum;
    private SpatialDataLayer result;
    
    public static final int MEM_ADDITIVE=0;
    public static final int MEM_DOMINANT=1;
    public static final int MEM_DIMINISHING=2;
    
    public static final int IMPACTS_SUM =0;
    public static final int IMPACTS_AVG =1;
    
    private ArrayList<StressorRankInfo> stressorInfos;
    private ArrayList<EcocompRankInfo> ecocompInfos;
    private ArrayList<RegionRankInfo> regionInfos;
    
    private String prefix="";
    
     //mem is multiple effects model
    //This constructor uses the supplied external, i.e. retained over multiple simulator runs, info lists.
    //The other version below uses internal variables.
    public Simulator(ArrayList<SpatialDataLayer> stressors, ArrayList<SpatialDataLayer> ecocomps, SpatialDataLayer regions, SensitivityScoreSet scores, int mem, int meanOrSum, ArrayList<StressorRankInfo> stressorInfos, ArrayList<EcocompRankInfo> ecocompInfos, ArrayList<RegionRankInfo> regionInfos, String prefix)
    {
        this.stressors=stressors;
        this.scores=scores;
        this.ecocomps=ecocomps;   
        this.mem=mem;
        this.meanOrSum=meanOrSum;
        this.regions=regions;
      
        this.stressorInfos=stressorInfos;
        this.ecocompInfos=ecocompInfos;
        this.regionInfos=regionInfos; 
        
        this.prefix=prefix;

        
        //initializeInfoObjects();
        for(int i=0;i<stressorInfos.size();i++) {stressorInfos.get(i).currentContribution=0; /*stressorInfos.get(i).active=true;*/}
        for(int i=0;i<ecocompInfos.size();i++) {ecocompInfos.get(i).currentContribution=0;}
        for(int i=0;i<regionInfos.size();i++) 
        {   
            regionInfos.get(i).currentMeanImpact=0; 
            regionInfos.get(i).currentTotalImpact=0;
            //regionInfos.get(i).currentStressorImpact=new double[GlobalResources.mappingProject.stressors.size()];
        }
        if(mem==Simulator.MEM_ADDITIVE) {calculateWithAdditiveMEM();}
        else if(mem==Simulator.MEM_DOMINANT) {calculateWithDominantMEM();}
        else if(mem==Simulator.MEM_DIMINISHING) {calculateWithDiminishingMEM();}
    }
    
    //mem is multiple effects model
    public Simulator(ArrayList<SpatialDataLayer> stressors, ArrayList<SpatialDataLayer> ecocomps, SpatialDataLayer regions, SensitivityScoreSet scores, int mem, int meanOrSum, String prefix)
    {

        this.stressors=stressors;
        this.scores=scores;
        this.ecocomps=ecocomps;   
        this.regions=regions;
        this.mem=mem;
        this.meanOrSum=meanOrSum;
      
        this.prefix=prefix;
        initializeInfoObjects();
        
        for(int i=0;i<stressorInfos.size();i++) {stressorInfos.get(i).currentContribution=0; /*stressorInfos.get(i).active=true;*/}
        for(int i=0;i<ecocompInfos.size();i++) {ecocompInfos.get(i).currentContribution=0;}
        for(int i=0;i<regionInfos.size();i++) {regionInfos.get(i).currentMeanImpact=0; regionInfos.get(i).currentTotalImpact=0;}
        

        if(mem==Simulator.MEM_ADDITIVE) {calculateWithAdditiveMEM();}
        else if(mem==Simulator.MEM_DOMINANT) {calculateWithDominantMEM();}
        else if(mem==Simulator.MEM_DIMINISHING) {calculateWithDiminishingMEM();}

    }
    
    private void calculateWithAdditiveMEM()
    {
    //calculate impact index
     //now create internal data grid
        
        float[][] data = GlobalResources.mappingProject.grid.getEmptyGrid();
        float[][] ecocompSum;
        if(meanOrSum==Simulator.IMPACTS_SUM)
        {   
            ecocompSum = new float[GlobalResources.mappingProject.grid.getDimensions().x][GlobalResources.mappingProject.grid.getDimensions().y];
        }
        else
        {
      
            DiversityIndex divIndex = new DiversityIndex("not_saved", ecocomps);  
            ecocompSum = divIndex.getGrid().getData();
        }

        float max=0;
        float min=9999;
        int activeImpactCount=0;
        //go through all impacts, get the stressor and ecocomp data and add to index
        for(int i=0; i<scores.size();i++)
        {

       
            ImpactInfo impact = scores.getInfo(i);
            if(impact.isActive())
            {

                activeImpactCount++;

                double score = impact.getSensitivityScore();
           
                float[][] ecocompData=impact.getEcocomp().getProcessedGrid().getData();
             
                float[][] stressorData=impact.getStressor().getProcessedGrid().getData();
              
                ResponseFunction r =impact.getResponseFunction();

                StressorRankInfo sInfo = getStressorInfoByName(impact.getStressor().getName());
         
                EcocompRankInfo eInfo = getEcocompInfoByName(impact.getEcocomp().getName());  
            
                for(int x=0;x<ecocompData.length;x++)
                {
                    for(int y=0; y<ecocompData[0].length; y++)
                    {   
                        if(stressorData[x][y]==GlobalResources.NODATAVALUE || ecocompData[x][y]==GlobalResources.NODATAVALUE || data[x][y]==GlobalResources.NODATAVALUE)
                        {
       
                            data[x][y]=GlobalResources.NODATAVALUE;
                        }
                        else
                        {
                        
                            float summand=(float) score*ecocompData[x][y]*r.getResponse(stressorData[x][y]);
                            if(meanOrSum == Simulator.IMPACTS_AVG)
                            {
                                summand=summand/ecocompSum[x][y];
                            /*    if(ecocompSum[x][y]!=1)
                                {
                                    System.out.println("Div index !=1");
                                }*/
                            }
                        
                            if(Double.isNaN(summand)) {summand=0;}
                            
                            sInfo.currentContribution += summand;                  
                            eInfo.currentContribution += summand;
                            if(sInfo.contributionMap!=null) {sInfo.contributionMap[x][y] += summand;}
                      
                           double code = regions.getGrid().getData()[x][y];
                            if(code!=GlobalResources.NODATAVALUE)
                            {
                                RegionRankInfo rInfo = getRegionInfoByCode(code);
                                rInfo.currentTotalImpact += summand;
                                //rInfo.currentStressorImpact[impact.getStressorIndex()]+=summand;
                            }
                
                            data[x][y]=data[x][y]+summand;    
                            impact.addToContribution(summand);
                            if(data[x][y]>max && data[x][y]!=GlobalResources.NODATAVALUE) {max=data[x][y];}
                            else if(data[x][y]<min && data[x][y]!=GlobalResources.NODATAVALUE) {min=data[x][y];}
              
                        }
                    }
                }
            }
        }
       DataGrid grid = new DataGrid(data,max,min,GlobalResources.NODATAVALUE);
       result = new SpatialDataLayer("Simulation result (add)"+new Date(),grid,GlobalResources.DATATYPE_SPATIAL,new DataSourceInfo());
    }
    
    public SpatialDataLayer getResult()
    {
      
        return result;
    }

    
    private void calculateWithDominantMEM() 
    {
        //own value grid for each ecocomp
        float[][] ecocompSum;
        if(meanOrSum==Simulator.IMPACTS_SUM)
        {
            ecocompSum = new float[GlobalResources.mappingProject.grid.getDimensions().x][GlobalResources.mappingProject.grid.getDimensions().y];
        }
        else
        {
            DiversityIndex divIndex = new DiversityIndex("not_saved", ecocomps);  //TODO if thr siulation doesn't change ecocomps, calculate only once as a static variable
            ecocompSum = divIndex.getGrid().getData();
        }
        float[][] values = GlobalResources.mappingProject.grid.getEmptyGrid();
        float max=0;
   
        //separate calculations for each ecocomp
        for(int e=0; e<ecocomps.size();e++)
        {
            //get impacts only for this ecocomp
            ArrayList<ImpactInfo> eImpacts = scores.getActiveImpactsForEcocomp(ecocomps.get(e).getName());
            float[][] ecocomp = ecocomps.get(e).getProcessedGrid().getData();
            //now go through the grid cell by cell... for each cell, find most important stressor, that's the summand, divide by # of ecocomps if need be, add to result
            for(int x=0; x<GlobalResources.mappingProject.grid.getDimensions().x;x++)
            {
                for(int y=0; y<GlobalResources.mappingProject.grid.getDimensions().y;y++)
                {
                    if(values[x][y]!=GlobalResources.NODATAVALUE && ecocomp[x][y]>0)  //only for present ecocomps
                    {
                        float largestSummand = -1;
                        int largestIndex=-1;
                        
                        for(int i=0; i<eImpacts.size(); i++)
                        {
                            ImpactInfo impact = eImpacts.get(i);
                            if(impact.isActive())
                            {
                                float summand = impact.getSensitivityScore() * impact.getResponseFunction().getResponse(impact.getStressor().getProcessedGrid().getData()[x][y]);
                                if(meanOrSum==Simulator.IMPACTS_AVG) {summand=summand/ecocompSum[x][y];}
                                if(Double.isNaN(summand)) {summand=0;}
                                
                                if(summand > largestSummand)
                                {
                                    largestSummand = summand;
                                    largestIndex = i;
                                }
                            }
                            
                        }
                        StressorRankInfo info = getStressorInfoByName(eImpacts.get(largestIndex).getStressor().getName());
                        info.currentContribution += largestSummand;
                        EcocompRankInfo eInfo = getEcocompInfoByName(eImpacts.get(largestIndex).getEcocomp().getName());
                        eInfo.currentContribution += largestSummand;
                        if(info.contributionMap!=null) {info.contributionMap[x][y] += largestSummand;}
                        double code = regions.getGrid().getData()[x][y];
                        if(code!=GlobalResources.NODATAVALUE)
                        {
                            RegionRankInfo rInfo = getRegionInfoByCode(code);
                            rInfo.currentTotalImpact += largestSummand;
                            //rInfo.currentStressorImpact[eImpacts.get(largestIndex).getStressorIndex()]+=largestSummand;
                        }
                        
                        values[x][y] = values[x][y] + largestSummand;
                        if(values[x][y]>max) {max = values[x][y];}
                        eImpacts.get(largestIndex).addToContribution(largestSummand);
                    }
                }
            }
        }
        DataGrid grid = new DataGrid(values,max,0,GlobalResources.NODATAVALUE);
        result = new SpatialDataLayer("Simulation Result (dom) " + new Date(),grid,GlobalResources.DATATYPE_SPATIAL,new DataSourceInfo());
    }

    private void calculateWithDiminishingMEM() 
    {   
         //set all ecocomp-stressor contributions to 0
        for(int i=0; i<scores.size();i++)
        {
            scores.getInfo(i).setContribution(0);
        }
        //own value grid for each ecocomp
        float[][] ecocompSum;
        if(meanOrSum == Simulator.IMPACTS_SUM)
        {
            ecocompSum = new float[GlobalResources.mappingProject.grid.getDimensions().x][GlobalResources.mappingProject.grid.getDimensions().y];
        }
        else
        {
            DiversityIndex divIndex = new DiversityIndex("not_saved", ecocomps);  
            ecocompSum = divIndex.getGrid().getData();
        }
        float[][] values = GlobalResources.mappingProject.grid.getEmptyGrid();
        float max=0;

        //separate calculations for each ecocomp
        for(int e=0; e<ecocomps.size();e++)
        {
            //GlobalResources.mappingProject.setProcessingProgressPercent((int) (100*e/ecocomps.size()));
            //get impacts only for this ecocomp
            ArrayList<ImpactInfo> eImpacts = scores.getActiveImpactsForEcocomp(ecocomps.get(e).getName());
            float[][] ecocomp = ecocomps.get(e).getProcessedGrid().getData();
            //now go through the grid cell by cell... for each cell, find most important stressor, that's the summand, divide by # of ecocomps if need be, add to result
            for(int x=0; x<GlobalResources.mappingProject.grid.getDimensions().x;x++)
            {
                for(int y=0; y<GlobalResources.mappingProject.grid.getDimensions().y;y++)
                {
                    if(values[x][y]!=GlobalResources.NODATAVALUE && ecocomp[x][y]>0)  //only for present ecocomps
                    {
                        ArrayList<Double> summands = new ArrayList<Double>();
                        int nonZeroImpacts=0;       
                        for(int i=0; i<eImpacts.size(); i++)
                        {
                            ImpactInfo impact = eImpacts.get(i);
                            double summand = impact.getSensitivityScore() * impact.getResponseFunction().getResponse(impact.getStressor().getProcessedGrid().getData()[x][y]);
                            if(!eImpacts.get(i).isActive()) summand = 0;
                            if(meanOrSum == Simulator.IMPACTS_AVG) {summand=summand/ecocompSum[x][y];}
                            if(Double.isNaN(summand)) {summand=0;}
                            if(summand>0) {nonZeroImpacts++;}
                            eImpacts.get(i).sortField=summand;
                        }
                        //sort impacts
                        Collections.sort(eImpacts,new ImpactComparator());
                        
                        //go through impacts, largest to smallest, weight according to number
                        for(int i=0;i<nonZeroImpacts;i++)
                        {
                            double weight = (nonZeroImpacts-i)/((double) nonZeroImpacts);
                            double weightedSummand=weight*eImpacts.get(i).sortField;
                            values[x][y]+=weightedSummand;
                            eImpacts.get(i).addToContribution(weightedSummand);
                            eImpacts.get(i).sortField=0;
                            StressorRankInfo info = getStressorInfoByName(eImpacts.get(i).getStressor().getName());
                            info.currentContribution += weightedSummand;
                            if(info.contributionMap!=null) {info.contributionMap[x][y] += weightedSummand;}
                            EcocompRankInfo eInfo = getEcocompInfoByName(eImpacts.get(i).getEcocomp().getName());
                            eInfo.currentContribution += weightedSummand;                                    
                            double code = regions.getGrid().getData()[x][y];    
                            if(code!=GlobalResources.NODATAVALUE)
                            {
                                RegionRankInfo rInfo = getRegionInfoByCode(code);
                                rInfo.currentTotalImpact += weightedSummand;
                                //rInfo.currentStressorImpact[eImpacts.get(i).getStressorIndex()]+=weightedSummand;
                            }
                            
                        }
                          if(values[x][y]>max) {max=values[x][y];}
                    }
                }
            }
            
        }
       DataGrid grid = new DataGrid(values,max,0,GlobalResources.NODATAVALUE);
       result = new SpatialDataLayer("Simulation Result (ant) " + new Date(),grid,GlobalResources.DATATYPE_SPATIAL,new DataSourceInfo());
    }

    //first item of returned list are region codes, second item are ranks
    public ArrayList<float[]> getRegionCodesAndRanks(float[][] regionCodes)
    {
        ArrayList<RegionRankInfo> regions = new ArrayList<RegionRankInfo>();
        float[][] impacts = getResult().getGrid().getData();
        for(int x=0; x<regionCodes.length; x++)
        {
            for(int y=0; y<regionCodes[0].length; y++)
            {
                if(regionCodes[x][y]!=GlobalResources.NODATAVALUE)
                {
                    float code =regionCodes[x][y];
                    //check if region already in list
                    RegionRankInfo info = null;
                    for(int i=0; i<regions.size(); i++)
                    {
                        if(regions.get(i).regionCode==code)
                        {
                            info=regions.get(i);
                        }
                    }
                    
                    //region not found: add
                    if(info==null)
                    {
                        info = new RegionRankInfo();
                        info.regionCode=code;
                        info.index=regions.size();
                        regions.add(info);
                    }
                    
                    //add impact to region
                    if(impacts[x][y]!=GlobalResources.NODATAVALUE)
                    {
                        info.currentTotalImpact+=impacts[x][y];
                        info.nrOfCells++;
                    }
                }
            }
        }
        
        //calculate mean impacts for regions
        for(int i=0; i<regions.size(); i++)
        {
            RegionRankInfo info = regions.get(i);
            info.currentMeanImpact = info.currentTotalImpact/info.nrOfCells;
        }
        
        //sort regions by impact
        Collections.sort(regions, new RegionComparator());
        float[] codes = new float[regions.size()];
        float[] regionRanks = new float[regions.size()];
        for(int i=0; i<regions.size();i++)
        {
            RegionRankInfo info = regions.get(i);
            regionRanks[info.index] = i+1;
            codes[info.index] = info.regionCode;
        }
        ArrayList<float[]> results = new ArrayList<float[]>();
        results.add(codes);
        results.add(regionRanks);
        return results;
    }

    
    public RegionRankInfo getRegionInfoByCode(double code) 
    {
        for(int i=0; i<regionInfos.size(); i++)
        {
            if(regionInfos.get(i).regionCode == code) {return regionInfos.get(i);}
        }
        return null;
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

    private EcocompRankInfo getEcocompInfoByName(String name) 
    {
         EcocompRankInfo info=null;
        if(ecocompInfos==null) {return null;}
        for(int i=0; i<ecocompInfos.size();i++)
        {
            if(ecocompInfos.get(i).name.equals(name)) {info=ecocompInfos.get(i);}
        }
        return info;
    }

  /*  //can be external references to static variables to keep infos across simulator instances
    public void setInfoObjects(ArrayList<StressorRankInfo> stressorInfos,
            ArrayList<EcocompRankInfo> ecocompInfos, ArrayList<RegionRankInfo> regionInfos)
    {
        this.stressorInfos=stressorInfos;
        this.ecocompInfos=ecocompInfos;
        this.regionInfos=regionInfos;
    }*/
    
    //must be called before each Morris run; returns region codes
    //MCSimulationManager has its own objects
    private void initializeInfoObjects() 
    {
        stressorInfos = new ArrayList<>();
        for(int i =0; i<stressors.size();i++)
        {
           StressorRankInfo newInfo = new StressorRankInfo();
           newInfo.name = stressors.get(i).getName();
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
        for(int i =0; i<ecocomps.size();i++)
        {
           EcocompRankInfo newInfo = new EcocompRankInfo();
           newInfo.name = ecocomps.get(i).getName();
           newInfo.cellSum = ecocomps.get(i).getGrid().getCellSum();
           ecocompInfos.add(newInfo);
        }
        ArrayList<Float> regionCodes = regions.grid.getUniqueDataValues();
   
        regionInfos = new ArrayList<RegionRankInfo>();
        float[][] regionData = regions.grid.getData();
        
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
            regionInfos.add(newInfo);
        } 
    }

   
    
    public ArrayList<EcocompRankInfo> getEcocompInfos()
    {
        return ecocompInfos;
    }
    public ArrayList<StressorRankInfo> getStressorInfos()
    {
        return stressorInfos;
    }
    public ArrayList<RegionRankInfo> getRegionInfos()
    {
        return regionInfos;
    }
    
    //returns stressor ranks in order that stressors appear in GlobalResources.mappingProject, with
    //no data value
    public float[] getStressorRanks()
    {
        
        float[] ranks = new float[stressors.size()];
        
        if(stressorInfos!=null)
        {
            //calculate ranks
            Collections.sort(stressorInfos, new StressorComparator());
            for(int s=0; s<stressorInfos.size(); s++)
            {
                stressorInfos.get(s).maxRank=s+1;
                stressorInfos.get(s).minRank=s+1;
            }
            int active=0;
            for(int i=0; i<stressors.size(); i++)
            {
                String name = stressors.get(i).getName();
                StressorRankInfo info = getStressorInfoByName(name);
                if(!info.active) {ranks[i]=GlobalResources.NODATAVALUE;}
                else {ranks[i] = info.maxRank;active++;}
            }
            for(int i=0; i<ranks.length; i++)
            {
                if(ranks[i]!=GlobalResources.NODATAVALUE) {ranks[i]=((float)(1.0*ranks[i])/active);}
            }
        }
        return ranks;
    }
    
        //returns ecocomp ranks in order that stressors appear in GlobalResources.mappingProject
    public float[] getEcocompRanks()
    {
        
        float[] ranks = new float[ecocomps.size()];
        
        if(ecocompInfos!=null)
        {
            //calculate ranks
            Collections.sort(ecocompInfos, new EcocompComparator());
            for(int s=0; s<ecocompInfos.size(); s++)
            {
                ecocompInfos.get(s).maxRank=s+1;
                ecocompInfos.get(s).minRank=s+1;
            }
            
            for(int i=0; i<ecocomps.size(); i++)
            {
                String name = ecocomps.get(i).getName();
                EcocompRankInfo info = getEcocompInfoByName(name);
                ranks[i] = info.maxRank;
            }
        }
        return ranks;
    }
            
    
}
