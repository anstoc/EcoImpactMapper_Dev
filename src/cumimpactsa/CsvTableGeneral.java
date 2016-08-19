/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package cumimpactsa;
import java.util.ArrayList;
import java.io.*;
/**
 * @summary Class to read in and write CSV files. 
 * @author ast
 */
public class CsvTableGeneral
{
    private ArrayList<ArrayList> data = new ArrayList<ArrayList>(); //a list of columns, where each column is a list of strings
    private ArrayList<String> colNames = new ArrayList<String>();
    
    
    /**
     * @summary Adds a column. 
     * @param name Column name.
     */
    public void addColumn(String name)
    {

        colNames.add(name);
        ArrayList<String> newCol = new ArrayList<String>();
        if(data.size()!=0)
        {
            for(int i = 0; i<data.get(0).size(); i++)
            {
                newCol.add("");
            }
        }
        data.add(newCol);

    }
    
    public void addColumn(String name, ArrayList<String> columnData)
    {

        colNames.add(name);
        ArrayList<String> newCol = new ArrayList<String>();
        if(!data.isEmpty())
        {
            for(int i = 0; i<data.get(0).size(); i++)
            {
                newCol.add(columnData.get(i));
            }
        }
        data.add(newCol);

    }
    
    //adds all columns that don't exist yet 
    public void append(CsvTableGeneral table2)
    {
        for(int i=0; i<table2.colNames.size();i++)
        {
            String newName = table2.getColNames().get(i);
            if(!this.colNames.contains(newName))
            {
                addColumn(newName, table2.data.get(i));
            }
            
        }
    }
    

    
    /**
     * 
     * @return List of column names.
     */
    public ArrayList<String> getColNames()
    {
        return colNames;
    }
    
    /**
     * @summary Adds a row to the table.
     * @param values ArrayList of values, one for each existing column
     */
    public void addRow(ArrayList<String> values)
    {
        if(values.size() == colNames.size())
        {
            for(int i = 0; i < colNames.size(); i++)
            {
                data.get(i).add(values.get(i));
            }
        }
    }
    
    
    public void readFromFile(File file)
    {
       //read file line-by-line
            try
            {
                //open reading stream
                FileInputStream fstream = new FileInputStream(file.getAbsoluteFile());
                DataInputStream ds = new DataInputStream(fstream);
                BufferedReader in = new BufferedReader(new InputStreamReader(ds));
                
                //read header
                String line = in.readLine();
                data = new ArrayList<ArrayList>(); //a list of columns, where each column is a list of strings
                ArrayList<String> colNames = lineToValues(line);
                for(int i = 0; i < colNames.size(); i++)
                {
                    addColumn(colNames.get(i));
                }
                
                //run through all lines and parse
                
                while ((line = in.readLine()) != null)   
                {
                    ArrayList values = lineToValues(line);
                    addRow(values);
                }
                
                in.close();
                ds.close();
                fstream.close();
                          
                
            }
            catch(Exception e)
            {
                System.err.println("Error while reading" + file);
                System.err.println(e.getMessage().toString());
           }
    }
    
    public ArrayList<String> lineToValues(String line)
    {
        ArrayList<String> list = new ArrayList<String>();
        
        while(line.contains(","))
        {
            String value = line.substring(0, line.indexOf(","));
            list.add(value);
            line = line.substring(line.indexOf(",")+1);
        }
        
        //last column
        list.add(line);
        
        return list;
        
    }
    
    /**
     * @summary Returns a column of the table as list of strings (excluding the column name).
     * @param colname Name of the column
     * @return The values of the column as strings
     */
    public ArrayList<String> getColumn(String colname)
    {
        for(int i=0; i<colNames.size(); i++)
        {
            if(colNames.get(i).equals(colname))
            {
                return this.data.get(i);
            }
        }
        
        return null;
        
    }
    
    public void writeToFile(String filename)
    {
        try
        {
            File file = new File(filename);
 
            // if file doesnt exists, then create it
               if (!file.exists()) 
                {
                    
                    file.createNewFile();
                }
 
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		
                //write header
                String header = "";
                for(int i=0;i<colNames.size()-1; i++)
                {
                    header += colNames.get(i);
                    header += ",";
                }
                header+=colNames.get(colNames.size()-1);
                
                bw.write(header);
                bw.newLine();
                
                //write data line by line
                for(int row = 0; row < data.get(0).size();row++)
                {
                    String line = "";
                    for(int col=0; col<data.size()-1;col++)
                    {
                        String value = (String) data.get(col).get(row);
                        line += value;
                        line +=",";
                    }
                    line += data.get(data.size()-1).get(row);
                    
                    bw.write(line);
                    bw.newLine();
                    
                }
                
                bw.close();
 
	
 
	} 
        catch (IOException e) 
        {
		e.printStackTrace();
	}
    }
            
    
}
