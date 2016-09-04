/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.util.concurrent.locks.LockSupport;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 *
 * @author ast
 */
public class MCSimulationWorker extends SwingWorker<MCSimulationManager,Void>
{
    public boolean working = false;
    public boolean initialized=false;
    public int sampleSize = 0;
    public int workerNr=0;
    //private MorrisSampler ms;
    private MCSimulationManager mcm=null;
    
    
    public void setSimulationManager(MCSimulationManager mcm)
    {
        this.mcm=mcm;
    }
    
    public MCSimulationManager getSimulationManager()
    {
        if(!working) {return mcm;}
        else {return null;}
    }
    
    @Override
    protected MCSimulationManager doInBackground() throws Exception 
    {
                working=true;
                //msm.setup();
                initialized=true;
                mcm.prefix="Thread "+workerNr+": ";
                mcm.simulationRuns=sampleSize;
                mcm.runMCSimulation();  
                //working=false;
                GlobalResources.statusWindow.println("Worker thread "+workerNr+ "is done. ");
                return mcm;
    }
    
     @Override 
     protected void done()
     {
        GlobalResources.statusWindow.println(this.workerNr + ": Worker thread has reached done() method.");
        working=false;
        try    
        {
           MCSimulationManager mcm = get();            
        }
        catch(Exception e)
        {
             GlobalResources.statusWindow.println("Error retrieving thread results from Monte Carlo Simulation.");
             GlobalResources.statusWindow.println(e);             
        }

      }
}
