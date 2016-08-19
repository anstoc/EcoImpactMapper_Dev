/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.util.ArrayList;

/**
 *
 * @author ast
 */
public class OrderedUniqueDoubleList 
{
    private ArrayList<Double> list = new ArrayList<Double>();
    
    public void add(Double x)
    {
        if(list.size()==0) {list.add(x);}
        //else if(list.size()==1 && x>list.get(0)) {list.add(x);}
        //else if(list.size()==1 && x<list.get(0)) {list.add(0,x);}
        else if(x<list.get(0)) {list.add(0,x);}
        else if(x>list.get(list.size()-1)) {list.add(x);}
        else if(list.size()!=1)
        {
            int i=1;
            while(i<list.size() && list.get(i)<=x) 
            {
                i++;
            }
            if(!list.get(i-1).equals(x))
            {
                //System.out.println("Adding "+x+" at position "+i+" of "+list.size());
                list.add(i,x);
            } 
        }
    }
    
    public int size()
    {
        return list.size();
    }
    
    public Double get(int i) 
    {
        return list.get(i);
    }
    
    public ArrayList<Double> getList()
    {
        return list;
    }
    
}
