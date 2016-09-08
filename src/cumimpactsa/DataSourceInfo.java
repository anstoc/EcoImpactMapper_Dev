/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

/**
 *
 * @author ast
 */
public class DataSourceInfo {
    public String sourceFile;
    public String xField;
    public String yField;
    public String valueField;
    
    public String toString()
    {
        String s="    Source: "+sourceFile+"\n"
                +"    X field: "+xField+"\n"
                +"    Y field: "+yField+"\n"
                +"    Value field: "+valueField;
        return s;
    }
    
}
