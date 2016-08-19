/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.util.Comparator;

/**
 * @summary Compares two cells by their x and y coordinates.
 * @author ast
 */
public class CellComparatorXY implements Comparator<CellRankInfo>
{
       @Override
        public int compare(CellRankInfo i1, CellRankInfo i2) 
        {
            if(i1.x!=i2.x)
            {
                return (new Integer(i2.x)).compareTo(i1.x);
            }
            else
            {
                return (new Integer(i2.y)).compareTo(i1.y);
            }
        }
}        

