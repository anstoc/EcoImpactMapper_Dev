/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

/**
 *
 * @author ast
 */
public class RegionRankInfo 
{
    public float regionCode=-1;
    public int index=-1;
    public double currentTotalImpact;
    public int nrOfCells=0;
    public double currentMeanImpact;
    public int maxRank=-1;
    public int minRank=9999;
    public int inTop25p=0;
    public int inBottom25p=0;
    //public double[] currentStressorImpact=null;
    //public double[] stressorPercentSum=null;
}
