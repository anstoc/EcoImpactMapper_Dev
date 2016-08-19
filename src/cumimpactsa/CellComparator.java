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

public class CellComparator implements Comparator<CellRankInfo>
{
       @Override
        public int compare(CellRankInfo i1, CellRankInfo i2) 
        {
            return (new Float(i2.currentImpact)).compareTo(i1.currentImpact);
        }
}    

