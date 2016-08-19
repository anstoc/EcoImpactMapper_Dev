/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

/**
 *
 * @author ast
 */
public class StressorRankInfo 
{
    public String name;
    public float minRank=9999;
    public float maxRank=-1;
    public float inMostImportant25p=0;       
    public float inLeastImportant25p=0;   
    public float included=0;   //how often has this stressor been included in the analysis?
    public double currentContribution=0;
    public boolean active=true;
    public float[][] contributionMap=null;
}
