/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.io.File;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author ast
 */
public class SensitivityScoreSet 
{
    private ArrayList<ImpactInfo> impacts = new ArrayList<ImpactInfo>();
    private String sourceFileName="";
    private float min=9999; //smallest score
    private float max=0; //highest score
    
    public void createFromFile(String filename)
    {
        sourceFileName=filename;
        CsvTableGeneral table = new CsvTableGeneral();
        table.readFromFile(new File(filename));
        
        
        //go through ecocomps(columns)
        for(int e=1; e<table.getColNames().size();e++)
        {
            String ecocompName = table.getColNames().get(e);
            //go through stressors (rows)
            for(int s=0; s<table.getColumn(ecocompName).size(); s++)
            {
                String stressorName = table.getColumn(table.getColNames().get(0)).get(s); //stressor name in first column
                float score = Float.parseFloat(table.getColumn(ecocompName).get(s));   //read out from table
                
                ImpactInfo impactInfo = new ImpactInfo(stressorName,ecocompName,score);
                if(score<min) {min=score;}
                if(score>max) {max=score;}
                
                //check if it's ok
                if(impactInfo.getEcocomp()==null)
                {
                    impacts = new ArrayList<ImpactInfo>();
                    JOptionPane.showMessageDialog(null, "Loading failed. Could not find ecosystem component: "+ecocompName);
                    return;
                }
                else if(impactInfo.getStressor()==null)
                {
                    impacts = new ArrayList<ImpactInfo>();
                    JOptionPane.showMessageDialog(null, "Loading failed. Could not find stressor: "+stressorName);
                    return;
                }
                else //everything ok
                {
                    impacts.add(impactInfo);
                }

            }
        }

    }
    
    
    public SensitivityScoreSet clone(ArrayList<SpatialDataLayer> stressors, ArrayList<SpatialDataLayer> ecocomps)
    {
        SensitivityScoreSet clone = new SensitivityScoreSet();
        
        for(int i=0; i<impacts.size();i++)
        {
            
            SpatialDataLayer newStressor=null;
            SpatialDataLayer newEcocomp=null;
            for(int j=0;j<stressors.size();j++)
            {
                if(stressors.get(j).getName().equals(impacts.get(i).getStressor().getName()))  
                {newStressor=stressors.get(j);}
            }
            for(int j=0;j<ecocomps.size();j++)
            {
                if(ecocomps.get(j).getName().equals(impacts.get(i).getEcocomp().getName()))  
                {newEcocomp=ecocomps.get(j);}
            }
            ImpactInfo newImpact=new ImpactInfo(newStressor,newEcocomp,impacts.get(i).getSensitivityScore());
            clone.impacts.add(newImpact);
       
        }
        clone.max=this.max;
        clone.min=this.min;
        return clone;
    }
    
    public float getMin() {return min;}
    
    public float getMax() {return max;}
    
    public String getSourceFileName()
    {
        return sourceFileName;
    }
    
    public void addSensitivityScore(ImpactInfo info)
    {
        impacts.add(info);
    }
    
    public void addSensitivityScore(String stressorName, String ecocompName, float score)
    {
        impacts.add(new ImpactInfo(stressorName, ecocompName, score));
    }
    
    public ArrayList<ImpactInfo> getAllScores()
    {
        return impacts;
    }
    
    public int size()
    {
        return impacts.size();
    }
    
    public ImpactInfo getInfo(int index)
    {
        if(index<impacts.size())
        {
            return impacts.get(index);
        }
        else {return null;}
    }
 
    public float getStressorAverageScore(String stressorName)
    {
        float sum=0;
        int count=0;
        for(int i=0; i<this.impacts.size();i++)
        {
            if(impacts.get(i).getStressor().getName().equals(stressorName))
            {
                sum+=impacts.get(i).getSensitivityScore();
                count++;
            }
        }
        
        return sum/count;
        
    }

    public CsvTableGeneral getContributionsAsTable()
    {
        CsvTableGeneral table = new CsvTableGeneral();
        table.addColumn("stressor");
        table.addColumn("ecocomp");
        table.addColumn("contribution");
        
        for(int i=0; i<this.impacts.size();i++)
        {
            ArrayList<String> row = new ArrayList<String>();
            row.add(impacts.get(i).getStressor().getName());
            row.add(impacts.get(i).getEcocomp().getName());
            row.add(Double.toString(impacts.get(i).getContribution()));
            table.addRow(row);
        }
        
        return table;
        
    }
    
    public ArrayList<ImpactInfo> getActiveImpactsForEcocomp(String name)
    {
        ArrayList<ImpactInfo> result = new ArrayList<ImpactInfo>();
        for(int i=0; i<impacts.size();i++)
        {
            if(impacts.get(i).getEcocomp().getName().equals(name) && impacts.get(i).isActive()) {result.add(impacts.get(i));}
        }
        return result;
    }

    float getEcocompAverageScore(String ecocompName)
    {
        float sum=0;
        int count=0;
        for(int i=0; i<this.impacts.size();i++)
        {
            if(impacts.get(i).getEcocomp().getName().equals(ecocompName))
            {
                sum+=impacts.get(i).getSensitivityScore();
                count++;
            }
        }
        return sum/count;
    }
    
}
