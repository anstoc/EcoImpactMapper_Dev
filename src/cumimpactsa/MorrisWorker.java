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
    public boolean initialized=false;
    public int sampleSize = 0;
    public int workerNr=0;
    //private MorrisSampler ms;
    
    
    @Override
    protected MorrisSampler doInBackground() throws Exception 
    {
                working=true;
                MorrisSampler ms = new MorrisSampler();
                ms.setup();
                initialized=true;
                ms.prefix="Thread "+workerNr+": ";
                ms.processTrajectories(sampleSize);  
                //ms.saveResults(outputFolder);
                working=false;
                return ms;
    }
    
     @Override 
     protected void done()
     {
        System.out.println("Background thread is done.");
        working=false;
        try    
        {
           MorrisSampler ms = get();
                     
        }
        catch(Throwable e)
        {
             System.out.println("!!!!!!!!!!!!!! Error retrieving results from multi-threaded elementary effects calculation.");
        }

      }

}
