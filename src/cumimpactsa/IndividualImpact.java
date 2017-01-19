/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

/**
 *
 * @author andy
 */
public class IndividualImpact 
{
    public int x;
    public int y;
    public ImpactInfo impactInfo;
    public float impact;
    
    public IndividualImpact(int x, int y, ImpactInfo impactInfo, float impact)
    {
        this.x=x;
        this.y=y;
        this.impact=impact;
        this.impactInfo=impactInfo;
    }
    
}
