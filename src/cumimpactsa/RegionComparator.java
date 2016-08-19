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
public class RegionComparator implements Comparator<RegionRankInfo> 
{

        @Override
        public int compare(RegionRankInfo i1, RegionRankInfo i2) 
        {
            return (new Double(i2.currentMeanImpact)).compareTo(i1.currentMeanImpact);
        }
}


    