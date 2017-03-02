/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import javax.swing.SwingWorker;

/**
 *
 * @author andy
 */
public class EEMapWorker extends SwingWorker<MorrisSampler,Void>
{
    public boolean working = false;
    public boolean rankBased=true;
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
                ms.calculateEEMaps(rankBased);
                return ms;
    }
    
     @Override 
     protected void done()
     {
        working=false;
        try    
        {
           MorrisSampler ms = get();         
        }
        catch(Exception e)
        {

        }

      }

}