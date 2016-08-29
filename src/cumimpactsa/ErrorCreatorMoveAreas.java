/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @summary Creates presence areas (1) in random locations. The areas have the same spatial extent as in the original data.
 * @author ast
 */
public class ErrorCreatorMoveAreas extends GeneralProcessor
{
    
    private int[] cellsCreated; 
    private int createdCellNr=0;
    private int targetCellNr=0;
    private float targetValue=0;
    
    @Override
    public DataGrid process(DataGrid grid) 
    {
        int[] areaSizes=grid.getPresenceAreaSizes();
        float[] areaValues=grid.getPresenceAreaValues();
        //cellsCreated=new int[areaSizes.length];
        float[][] data=Helpers.deepArrayCopy(GlobalResources.mappingProject.grid.getEmptyGrid());
        
        int a=0;
        while(a<areaSizes.length)
        {
            createdCellNr=0;
            targetCellNr=areaSizes[a];
            targetValue=areaValues[a];
            //select a random starting location
            int seedX = (int) Math.floor(Math.random()*data.length);
            int seedY = (int) Math.floor(Math.random()*data[0].length);
            if(data[seedX][seedY]==0)
            {
                expandArea(data, seedX,seedY);
                a++;
            }
        }
        
        
        return new DataGrid(data,grid.getMax(),grid.getMin(),grid.getNoDataValue());
    }

    
    private void expandArea(float[][] data, int x, int y)
    {
        
        if(createdCellNr<targetCellNr && data[x][y]==0)
        {
            data[x][y]=targetValue; 
            createdCellNr++;
            //set neighborhood, making sure it doesn't exceed array bounds
            int startX=x;
            int endX=x;
            int startY=y;
            int endY=y;
            if(x>0) {startX=x-1;}
            if(x<data.length-1) {endX=x+1;}
            if(y>0) {startY=y-1;}
            if(y<data[0].length-1) {endY=y+1;}

            //make list of cells that can be expanded on
            ArrayList<Point2DInt> candidates=new ArrayList<Point2DInt>();
            for(int nx=startX;nx<=endX;nx++)
                for(int ny=startY;ny<=endY;ny++)
                {
                    if(data[nx][ny]==0)
                    {
                       candidates.add(new Point2DInt(nx,ny));
                    }
                 }
            //take one candidate - this one is sure to be expanded
            Collections.shuffle(candidates);
            if(candidates.size()>0)
            {
                int randIndex = (int) Math.floor(Math.random()*candidates.size());
                Point2DInt sureCandidate = candidates.get(randIndex);
                //go through all candidates and add them with 60% probability
                for(int c=0; c<candidates.size();c++)
                {
                    if(Math.random()<0.5)
                    {
                        expandArea(data,candidates.get(c).x,candidates.get(c).y);
                    }
                }
                expandArea(data,sureCandidate.x,sureCandidate.y);
            }
        }
    }
    
    @Override
    public String getName() 
    {
        return "Move areas to random locations";
    }

    @Override
    public PreProcessor clone() {
        return new ErrorCreatorMoveAreas();
    }
    
}
