/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.util.Comparator;

/**
 *
 * @author andy
 */
public class FloatStringComparator implements Comparator<String>
{
    @Override
    public int compare(String o1, String o2) 
    {
        try
        {
            Float nr1 = Float.parseFloat(o1);
            Float nr2 = Float.parseFloat(o2);
            if(nr1>nr2) return 1;
            else if(nr1<nr2) return -1;
            else return 0;
        }
        catch(Exception e)
        {
            return o1.compareTo(o2);
        }
    } 
}
