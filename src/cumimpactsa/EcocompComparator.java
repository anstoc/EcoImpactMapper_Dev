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
public class EcocompComparator implements Comparator<EcocompRankInfo>
{
       @Override
        public int compare(EcocompRankInfo i1, EcocompRankInfo i2) 
        {
            return (new Double(i2.currentContribution/i2.cellSum)).compareTo(i1.currentContribution/i1.cellSum);
        }
}