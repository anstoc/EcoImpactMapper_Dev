/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

/**
 * @summary Interface for data layer processors, e.g. log-transformation and rescaling.
 * @author ast
 */
public interface PreProcessor
{
   public DataGrid process(DataGrid grid);
   public String[] getParamNames();
   public int getParamNr();
   public double[] getParamValues();
   public boolean setParamValue(String paramName, double value);
   public String getName();
   public PreProcessor clone();
   public void setWorkerNr(int workerNr);
   public String getNameAndLastParam();
}
