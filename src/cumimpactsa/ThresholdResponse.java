/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

/**
 *
 * @author ast
 */
public class ThresholdResponse implements ResponseFunction 
{

    double L=1; //max
    double x0=0.5; //flexion (?) point
    double k=50; //steepness
    
    public void setX0(double x0)
    {
        this.x0=x0;
    }
    
    @Override
    public float getResponse(float stressorIntensity) 
    {

       
       return (float) (L/(1+Math.exp(-k*(stressorIntensity-x0))));
        
    }

    @Override
    public String getName() 
    {
        return "Threshold response";
    }
    
}
