package cumimpactsa;

import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JOptionPane;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author andy
 */
public class MorrisFactor 
{
    private String name = new String();
    private ArrayList<String> optionNames=new ArrayList<String>();
    private float[] possibleOptionCodes=null;
    private String[] possibleOptionNames=null;
    private boolean qualitative=true;
    float min=0;
    float max=1;
    /*
    @summary Constructor. Takes the name and a list of possible options. If possibleOptionNames is null, will assume it's a quantitative factor taking any value between 0 and 1.
    */ 
    public MorrisFactor(String name, String[] possibleOptions, float[] possibleOptionCodes, float min, float max)
    {
        this.name=name;
        this.possibleOptionNames=possibleOptions;
        this.min=min;
        this.max=max;
        if(possibleOptions==null) qualitative=false;
    }
    
    public String getName() {return name;}
    
    public String[] getPossibleOptions() {return possibleOptionNames;}
 
    public boolean isQualitative()
    {
        return qualitative;
    }
    
    public ArrayList<String> getOptionNames()
    {
        return optionNames;
    }
    
    public void addOption(String optionName)
    {
        //make sure not yet contained
        boolean contained=false;
        for(int i=0; i<optionNames.size();i++)
        {
            if(optionNames.get(i).equals(optionName)) contained=true;
        }
        
        //qualitative factor - make sure it's a valid option
        if(!contained && possibleOptionNames!=null)
        {
            boolean isPossible=false;
            for(int i=0; i<possibleOptionNames.length; i++)
            {
                if(possibleOptionNames[i].equals(optionName)) isPossible=true;
            }
            if(isPossible) optionNames.add(optionName);
            Collections.sort(optionNames);
        }
        //quantitative - make sure it's a number between min and max
        else if(!contained)
        {
            try
            {
               float number=Float.parseFloat(optionName);
               if(number<min || number>max) throw new Exception();
               optionNames.add(optionName);
               Collections.sort(optionNames);
            }
            catch(Exception e)
            {
                JOptionPane.showMessageDialog(null, "This factor must be a number between "+min+" and "+max);
            }
        }
    }
    
    public String toString()
    {
        String text=name+": ";
        for(int i=0;i<optionNames.size();i++) 
        {
            text=text+optionNames.get(i);
            if(i<optionNames.size()-1) text=text+";";
        }
        return text;
    }
    
    public int getNrOfOptions()
    {
        return optionNames.size();
    }
    
    //returns the numbers actually used by the MorrisSampler
    public float[] getOptionCodes()
    {
        float[] optionCodes=new float[optionNames.size()];
     
        
        if(qualitative)
        {
            for(int i=0; i<optionCodes.length; i++)
            {
                String name=optionNames.get(i);
                int index=-1;
                for(int j=0; j<possibleOptionNames.length; j++)
                {
                    if(possibleOptionNames[j].equals(name)) index=j;
                }
                optionCodes[i]=possibleOptionCodes[index];
            }
        }
        else
        {
            for(int i=0; i<optionCodes.length; i++)
            {
                try
                {
                    float code=Float.parseFloat(optionNames.get(i));
                }
                catch(Exception e)
                {
                    JOptionPane.showMessageDialog(null, "This factor must be a number between "+min+" and "+max);
                }
            }    
        }
        return optionCodes;
    }
    
    public static MorrisFactor[] getDefaultImplementedFactors()
    {
        MorrisFactor[] factors = new MorrisFactor[9];
        
            /*"0: Missing stressor data","1: Sensitivity weight errors","2: Linear stress decay","3: Ecological thresholds",
                                    "4: Reduced analysis resolution", "5: Improved stressor resolution","6: Impact model","7: Transformation","8: Multiple stressor effects model"};*/

        //factor 0: Missing stressor data
        factors[0]=new MorrisFactor("0: Missing stressor data", 
                        null, null, 0, 1);  
        factors[0].addOption("0.0000"); factors[0].addOption("0.1111");factors[0].addOption("0.2222");factors[0].addOption("0.3333"); //default options
        
        //factor 1: Sensitivity weight errors
        factors[1]=new MorrisFactor("1: Sensitivity weight errors", 
                        null, null, 0, 1); 
        factors[1].addOption("0.0000"); factors[1].addOption("0.1667");factors[1].addOption("0.3333");factors[1].addOption("0.5000");
        
        //factor 2: Linear stress decay
        factors[2]=new MorrisFactor("2: Linear stress decay", 
                        null, null, 0, Float.MAX_VALUE); 
        factors[2].addOption("0");factors[2].addOption("7000");factors[2].addOption("14000");factors[2].addOption("20000");
        
        //factor 3: Ecological thresholds
        factors[3]=new MorrisFactor("3: Ecological thresholds", 
                        null, null, 0, 1);
        factors[3].addOption("0");factors[3].addOption("0.3333");factors[3].addOption("0.6667");factors[3].addOption("1.0000");
        
        //factor "4: Reduced analysis resolution"
        factors[4]=new MorrisFactor("4: Reduced analysis resolution", 
                        new String[]{"1","2","4","6","8","10","12","14","16","18","20"}, new float[]{1,2,4,6,8,10,12,14,16,18,20}, -1, -1);
        factors[4].addOption("1");factors[4].addOption("2");
        
        //factor "5: Improved stressor resolution"
        factors[5]=new MorrisFactor("5: Improved stressor resolution", 
                        new String[]{"No","Yes"}, new float[]{0,1}, -1, -1);
        factors[5].addOption("No");factors[5].addOption("Yes");
        
        //factor "6: Impact model"
        factors[6]=new MorrisFactor("6: Impact model", 
                        new String[]{"Sum","Mean"}, new float[]{0,1}, -1, -1);
        factors[6].addOption("Sum"); factors[6].addOption("Mean");
        
        //factor "7: Transformation"
        factors[7]=new MorrisFactor("7: Transformation", 
                        new String[]{"Log[X+1]","CDF","Cut at 99-Percentile","None"}, new float[]{0,1,2,3}, -1, -1);
        factors[7].addOption("Log[X+1]");factors[7].addOption("CDF");factors[7].addOption("Cut at 99-Percentile");
        
        //factor "8: Multiple stressor effects model"
        factors[8]=new MorrisFactor("8: Multiple stressor effects model", 
                        new String[]{"Additive","Dominant","Antagonistic"}, new float[]{0,1,2}, -1, -1);
        factors[8].addOption("Additive");factors[8].addOption("Dominant");factors[8].addOption("Antagonistic");
        
        return factors;
    }
    
}
