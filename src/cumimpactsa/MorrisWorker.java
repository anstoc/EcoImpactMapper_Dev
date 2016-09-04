/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 *
 * @author ast
 */
public class MorrisWorker extends SwingWorker<MorrisSampler,Void>
{
    public boolean working = false;
    //public boolean initialized=false;
    public int sampleSize = 0;
    public int workerNr=0;
    private MorrisSampler ms=null;
    
        
    public MorrisSampler getMorrisSampler()
    {
        if(!working) return ms;
        else return null;
    }
    
    @Override
    protected MorrisSampler doInBackground() throws Exception 
    {
                working=true;
                ms=new MorrisSampler();
                ms.setup();
                //initialized=true;
                ms.prefix="Thread "+workerNr+": ";
                ms.processTrajectories(sampleSize);  
                GlobalResources.statusWindow.println("Worker thread "+workerNr+ "is done. ");
                return ms;
    }
    
     @Override 
     protected void done()
     {
        GlobalResources.statusWindow.println(this.workerNr + ": Worker thread has reached done() method.");
        working=false;
        try    
        {
           MorrisSampler ms = get();
                     
        }
        catch(Exception e)
        {
             GlobalResources.statusWindow.println("Error retrieving thread results from elementary effects calculation.");
             GlobalResources.statusWindow.println(e);     
        }

      }

}
