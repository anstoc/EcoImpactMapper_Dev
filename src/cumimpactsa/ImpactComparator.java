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
public class ImpactComparator implements Comparator<ImpactInfo> 
{

        @Override
        public int compare(ImpactInfo i1, ImpactInfo i2) 
        {
            return (new Double(i2.sortField)).compareTo(i1.sortField);
        }
     }
    