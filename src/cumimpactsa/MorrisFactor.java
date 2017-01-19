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
    private ArrayList<String> levelNames=new ArrayList<String>();
    private float[] possibleLevelCodes=null;
    private String[] possibleLevelNames=null;
    private boolean qualitative=true;
    private boolean forceNumericalSorting=false;
    private float min=0;
    private float max=1;
    private float minLevel=1;
    private float maxLevel=0;
    /*
    @summary Constructor. Takes the name and a list of possible options. If possibleOptionNames is null, will assume it's a quantitative factor taking any value between 0 and 1.
    */ 
    public MorrisFactor(String name, String[] possibleLevels, float[] possibleLevelCodes, float min, float max)
    {
        this.name=name;
        this.possibleLevelNames=possibleLevels;
        this.possibleLevelCodes=possibleLevelCodes;
        this.min=min;
        this.max=max;
        this.minLevel=Float.MAX_VALUE;
        this.maxLevel=Float.MIN_VALUE;
        if(possibleLevels==null) qualitative=false;
    }
    
    public String getName() {return name;}
    
    public String[] getPossibleLevels() {return possibleLevelNames;}
 
    public boolean isQualitative()
    {
        return qualitative;
    }
    
    public ArrayList<String> getLevelNames()
    {
        return levelNames;
    }
    
    public void addLevel(String levelName)
    {
        //make sure not yet contained
        boolean contained=false;
        if(qualitative)
        {
            for(int i=0; i<levelNames.size();i++)
            {
                if(levelNames.get(i).equals(levelName)) contained=true;
            }
        }
        //distinction necessary so that same numbers are recognized as being the same;
        else
        {
         try
            {
               float number=Float.parseFloat(levelName);
               float[] containedNumbers=this.getLevelCodes();
               for(int i=0; i<containedNumbers.length; i++)
               {
                   if(containedNumbers[i]==number) {contained=true;}
               }
               
            }
            catch(Exception e)
            {
                JOptionPane.showMessageDialog(null, "This factor level must be a number between "+min+" and "+max);
            }
        }
        
        //qualitative factor - make sure it's a valid option
        if(!contained && possibleLevelNames!=null)
        {
            boolean isPossible=false;
            for(int i=0; i<possibleLevelNames.length; i++)
            {
                if(possibleLevelNames[i].equals(levelName)) isPossible=true;
            }
            if(isPossible) levelNames.add(levelName);
            if(forceNumericalSorting) Collections.sort(levelNames,new FloatStringComparator());
            else Collections.sort(levelNames);
        }
        //quantitative - make sure it's a number between min and max
        else if(!contained)
        {
            try
            {
               float number=Float.parseFloat(levelName);
               if(number<min || number>max) throw new Exception();
               levelNames.add(levelName);
               Collections.sort(levelNames,new FloatStringComparator());
            }
            catch(Exception e)
            {
                JOptionPane.showMessageDialog(null, "This factor level must be a number between "+min+" and "+max);
            }
        }
        
        updateMinMaxLevel();
        
    }
    
    public String toString()
    {
        String text=name+": ";
        for(int i=0;i<levelNames.size();i++) 
        {
            text=text+levelNames.get(i);
            if(i<levelNames.size()-1) text=text+";";
        }
        return text;
    }
    
    public int getNrOfLevels()
    {
        return levelNames.size();
    }
    
    //returns the numbers actually used by the MorrisSampler
    public float[] getLevelCodes()
    {
        float[] levelCodes=new float[levelNames.size()];
     
        
        if(qualitative)
        {
            for(int i=0; i<levelCodes.length; i++)
            {
                String name=levelNames.get(i);
                int index=-1;
                for(int j=0; j<possibleLevelNames.length; j++)
                {
                    if(possibleLevelNames[j].equals(name)) index=j;
                }
                levelCodes[i]=possibleLevelCodes[index];
            }
        }
        else
        {
            for(int i=0; i<levelCodes.length; i++)
            {
                try
                {
                    float code=Float.parseFloat(levelNames.get(i));
                    levelCodes[i]=code;
                }
                catch(Exception e)
                {
                    JOptionPane.showMessageDialog(null, "This factor level must be a number between "+min+" and "+max);
                }
            }    
        }
        return levelCodes;
    }
    
    public static MorrisFactor[] getDefaultImplementedFactors()
    {
        MorrisFactor[] factors = new MorrisFactor[9];
        
            /*"0: Missing stressor data","1: Sensitivity weight errors","2: Linear stress decay","3: Ecological thresholds",
                                    "4: Reduced analysis resolution", "5: Improved stressor resolution","6: Impact model","7: Transformation","8: Multiple stressor effects model"};*/

        //factor 0: Missing stressor data
        factors[0]=new MorrisFactor("0: Missing stressor data", 
                        null, null, 0, 1);  
        factors[0].addLevel("0.0000"); factors[0].addLevel("0.1111");factors[0].addLevel("0.2222");factors[0].addLevel("0.3333"); //default options
        
        //factor 1: Sensitivity weight errors
        factors[1]=new MorrisFactor("1: Sensitivity weight errors", 
                        null, null, 0, 1); 
        factors[1].addLevel("0.0000"); factors[1].addLevel("0.1667");factors[1].addLevel("0.3333");factors[1].addLevel("0.5000");
        
        //factor 2: Linear stress decay
        factors[2]=new MorrisFactor("2: Linear stress decay", 
                        null, null, 0, Float.MAX_VALUE); 
        factors[2].addLevel("0");factors[2].addLevel("7000");factors[2].addLevel("14000");factors[2].addLevel("20000");
        
        //factor 3: Ecological thresholds
        factors[3]=new MorrisFactor("3: Ecological thresholds", 
                        null, null, 0, 1);
        factors[3].addLevel("0");factors[3].addLevel("0.3333");factors[3].addLevel("0.6667");factors[3].addLevel("1.0000");
        
        //factor "4: Reduced analysis resolution"
        factors[4]=new MorrisFactor("4: Half analysis resolution", 
                        new String[]{/*"1","2","4","6","8","10","12","14","16","18","20"*/"No","Yes"}, 
                        new float[]{/*1,2,4,6,8,10,12,14,16,18,20*/1,2}, -1, -1);
        //factors[4].forceNumericalSorting(true);
        factors[4].addLevel("No");factors[4].addLevel("Yes");
        
        
        //factor "5: Improved stressor resolution"
        factors[5]=new MorrisFactor("5: Improved stressor resolution", 
                        new String[]{"No","Yes"}, new float[]{0,1}, -1, -1);
        factors[5].addLevel("No");factors[5].addLevel("Yes");
        
        //factor "6: Impact model"
        factors[6]=new MorrisFactor("6: Impact model", 
                        new String[]{"Sum","Mean"}, new float[]{0,1}, -1, -1);
        factors[6].addLevel("Sum"); factors[6].addLevel("Mean");
        
        //factor "7: Transformation"
        factors[7]=new MorrisFactor("7: Transformation", 
                        new String[]{"Log[X+1]","CDF","Cut at 99-Percentile","None"}, new float[]{0,1,2,3}, -1, -1);
        factors[7].addLevel("Log[X+1]");factors[7].addLevel("CDF");factors[7].addLevel("Cut at 99-Percentile");
        
        //factor "8: Multiple stressor effects model"
        factors[8]=new MorrisFactor("8: Multiple stressor effects model", 
                        new String[]{"Additive","Dominant","Antagonistic"}, new float[]{0,1,2}, -1, -1);
        factors[8].addLevel("Additive");factors[8].addLevel("Dominant");factors[8].addLevel("Antagonistic");
        
        return factors;
    }
    
    //use in cases like resolution reduction factor that take selected integers; makes sure that sorting in the MorrisDialog will be numerical,
    //not alphabetical
    public void forceNumericalSorting(boolean b)
    {
        this.forceNumericalSorting=b;
    }
    
    public boolean isNumericalSortingForced()
    {
        return this.forceNumericalSorting;
    }

    int getNrOfPossibleLevels() 
    {
        return possibleLevelNames.length;
    }
    
    //does nothing if level given by name is not assigned
    public void removeLevel(String levelName)
    {
        if(levelNames.size()==1)
        {
            JOptionPane.showMessageDialog(null, "Each factor must have at least one level.");
            return;
        }    
            
        for(int i=0; i<levelNames.size();i++)
        {
            if(qualitative)
            {
                if(levelNames.get(i).equals(levelName))
                {
                    levelNames.remove(i);
                    return;
                }
            }
            else
            {
                float f1 = Float.parseFloat(levelNames.get(i));
                float f2 = Float.parseFloat(levelName);
                if(f1==f2) 
                {    
                    levelNames.remove(i);
                    updateMinMaxLevel();
                    return;
                }
            }
        }
    }
    
    public ArrayList<String>[] getCsvTableLines()
    {
        ArrayList<String>[] lines = (ArrayList<String>[]) new ArrayList[levelNames.size()];
        
        for(int i=0; i<levelNames.size();i++)
        {
            ArrayList<String> newline = new ArrayList<String>();
            newline.add(this.name);
            newline.add(this.levelNames.get(i));
            lines[i]=newline;
        }
        
        return lines;
    }
    
    public static void saveFactorsToCsv(String filename)
    {
        CsvTableGeneral table=new CsvTableGeneral();
        table.addColumn("Factor");
        table.addColumn("Level");
        for(int i=0;i<GlobalResources.mappingProject.morrisFactors.length;i++)
        {
            ArrayList<String>[] lines = GlobalResources.mappingProject.morrisFactors[i].getCsvTableLines();
            for(int j=0;j<lines.length;j++)
            {
                table.addRow(lines[j]);
            }
        }
        table.writeToFile(filename);
    }
    
    public static void setFactorLevelsFromTable(CsvTableGeneral table)
    {
        ArrayList<String> factors=table.getColumn("Factor");
        ArrayList<String> levels=table.getColumn("Level");
        boolean[] isReset = new boolean[GlobalResources.mappingProject.morrisFactors.length]; //first time a factor appears in the table, its levels have to be set to an empty array list
                                                                                              //for subsequent adding of listed factors from the table - but only the first time
        for(int i=0; i<factors.size(); i++)
        {
            //find the right factor in the mapping project
            MorrisFactor currentFactor=null;
            for(int j=0; j<GlobalResources.mappingProject.morrisFactors.length; j++)
            {
                if(GlobalResources.mappingProject.morrisFactors[j].name.equals(factors.get(i)))
                {
                    currentFactor=GlobalResources.mappingProject.morrisFactors[j];
                    if(!isReset[j])
                    {
                        currentFactor.levelNames=new ArrayList<String>();
                        isReset[j]=true;
                    }
                }
            }
            if(currentFactor!=null) currentFactor.addLevel(levels.get(i));  
        }    
        
    }    
    
    public static String[] getFactorNames()
    {
        String[] names = new String[GlobalResources.mappingProject.morrisFactors.length];
        for(int i=0; i<names.length; i++)
        {
            names[i]=GlobalResources.mappingProject.morrisFactors[i].getName();
        }
        return names;
    }

    float getRange() 
    {
        if(this.isQualitative() && !this.forceNumericalSorting)
        {
            return -1;
        }
        else
        {
            return maxLevel-minLevel;
        }
    }

    private void updateMinMaxLevel() 
    {
       if(isQualitative() && !forceNumericalSorting)
       {
           minLevel=0; maxLevel=0;
       }
       else
       {
           minLevel=max;
           maxLevel=min;
           for(int i=0; i<this.getLevelCodes().length; i++)
           {
               if(this.getLevelCodes()[i]<minLevel) {minLevel=getLevelCodes()[i];}
               if(this.getLevelCodes()[i]>maxLevel) {maxLevel=getLevelCodes()[i];}
           }
       }
    }
    
}
