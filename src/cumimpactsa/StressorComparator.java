/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.util.Comparator;

/**
 *
 * @author ast
 */
public class StressorComparator implements Comparator<StressorRankInfo>
{
       @Override
        public int compare(StressorRankInfo i1, StressorRankInfo i2) 
        {
            return (new Double(i2.currentContribution)).compareTo(i1.currentContribution);
        }
}
