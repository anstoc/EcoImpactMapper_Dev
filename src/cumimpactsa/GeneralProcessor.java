/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

/**
 *
 * @author ast
 */
public abstract class GeneralProcessor implements PreProcessor 
{
    
    protected String[] paramNames;
    protected double[] paramValues;
    protected String name="General";
    protected int workerNr=-1;

    @Override
    public String[] getParamNames() {
        return paramNames;
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public int getParamNr() {
        if(paramNames==null) {return 0;}
        else {return paramNames.length;}
    }

    @Override
    public double[] getParamValues() {
        return paramValues;
    }

    @Override
    public boolean setParamValue(String paramName, double value) {
        if(paramNames==null) {return false;}
        else
        {
            for(int i=0; i<paramNames.length;i++)
            {
                if(paramName.equals(paramNames[i]))
                {
                    paramValues[i]=value;
                    return true;
                }
            }
            
            return false;
        }
    }
    
    public PreProcessor clone()
    {
        return null;
    }
    
    public void setWorkerNr(int workerNr)
    {
        this.workerNr=workerNr;
    }
    
    @Override
    public String getNameAndLastParam()
    {
        if(getParamNr()>0) return getName() + "["+getParamValues()[getParamNr()-1]+"]";
        else return getName();
    }
    
}
