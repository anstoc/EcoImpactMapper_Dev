/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

/**
 *
 * @author ast
 */
public class ImpactInfo 
{
    private SpatialDataLayer stressor;
    private SpatialDataLayer ecocomp;
    private float sensitivityScore;
    private double contribution=0;
    private boolean active=true;     //used to switch stressors on and off   
    private ResponseFunction responseFunction=new LinearResponse();
    public double sortField = 0;
    private int stressorIndex = -1;
    
    public ImpactInfo(String stressorName, String ecocompName, float sensitivityScore)
    {
        this.stressor=GlobalResources.mappingProject.getStressorByName(stressorName);
        this.stressorIndex=GlobalResources.mappingProject.getStressorIndexByName(stressorName);
        this.ecocomp=GlobalResources.mappingProject.getEcocompByName(ecocompName);
        this.sensitivityScore=sensitivityScore;
      
    }
    
    public void setStressor(SpatialDataLayer stressor)
    {
        this.stressor=stressor;
        stressorIndex=GlobalResources.mappingProject.getStressorIndexByName(stressor.getName());
    }
    
    public void setEcocomp(SpatialDataLayer ecocomp)
    {
        this.ecocomp=ecocomp;
    }
    
    public void setResponseFunction(ResponseFunction r)
    {
        responseFunction=r;
    }
    
    public ResponseFunction getResponseFunction()
    {
        return responseFunction;
    }
    
    public ImpactInfo(SpatialDataLayer stressor, SpatialDataLayer ecocomp, float sensitivityScore)
    {
        this.stressor=stressor;
        this.ecocomp=ecocomp;
        this.sensitivityScore=sensitivityScore;
        this.stressorIndex=GlobalResources.mappingProject.getStressorIndexByName(stressor.getName());
      
    }
    
    public void changeSensitivtyScore(float newValue)
    {
        sensitivityScore=newValue;
    }
    
    public SpatialDataLayer getStressor()
    {
        return stressor;
    }
    
    public boolean isActive()
    {
        return active;
    }
    
    public void setActive(boolean active)
    {
        this.active=active;
    }
    
    public SpatialDataLayer getEcocomp()
    {
        return ecocomp;
    }
    
    public float getSensitivityScore()
    {
        return sensitivityScore;
    }
    
    public double getContribution()
    {
        return contribution;
    }

    void setContribution(double value) 
    {
        this.contribution=value;
    }
    
    void addToContribution(double value) 
    {
        this.contribution+=value;
    }

    public int getStressorIndex() 
    {
        return stressorIndex;
    }
    
    
}
