/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

/**
 *
 * @author ast
 */
public class LinearResponse implements ResponseFunction
{
    @Override
    public float getResponse(float stressorIntensity) 
    {
        return stressorIntensity;
    }

    @Override
    public String getName() {
        return "Linear response";
    }
    
}
