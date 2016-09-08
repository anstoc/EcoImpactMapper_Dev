/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JTextArea;

/**
 *
 * @author ast
 */
public abstract class GlobalResources 
{
    public static MappingProject mappingProject=new MappingProject();
    public static StatusWindow statusWindow;
    
    public static boolean releaseVersion=true; //used to hide functions that are not fully implemented or tested in the public version
    
    public static final float NODATAVALUE=-9999; //keep negative
    public static final int DATATYPE_STRESSOR=1;
    public static final int DATATYPE_ECOCOMP=2; 
    public static final int DATATYPE_PROCESSINGCHAINS=3;
    public static final int DATATYPE_SPATIAL=4;
    public static final int DATATYPE_FREQUENCY=5;
    public static final int DATATYPE_REGIONS = 6;
    public static final int DATATYPE_AOIS = 7;
    public static String lastUsedFolder=System.getProperty("user.home");
    public static CsvTableFloat lastOpenedTable = null;
    public static String lastOpenedTableFile = "";
    
    public static float lowPassFilterDistance=25000;
    public static int nrOfThreads=1;

    protected static MainWindow mainWindow;
    
    public static String getDateTime()
    {
      return new SimpleDateFormat("yyyyMMdd HH:mm").format(Calendar.getInstance().getTime());
    }
    
    
}