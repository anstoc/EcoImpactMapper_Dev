/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

/**
 * @summary Creates random lines with intensity values occurring in the original data, and so that the sum of all cells will be very similar to the original data
 * @author ast
 * 
 */
public class ErrorCreatorMoveLines extends GeneralProcessor 
{

    @Override
    public DataGrid process(DataGrid grid) 
    {
        float data[][]=Helpers.deepArrayCopy(GlobalResources.mappingProject.grid.getEmptyGrid());
        float newMax=grid.getMin();
        float newMin=0;
        if(grid.getMin()==0)
        {
            double targetSum=grid.getCellSum();
            
            double totalSum=0;
            while(totalSum<targetSum)
            {
                double remainingSum=targetSum-totalSum;
                double nextSum=Math.round(Math.random()*remainingSum); //length of the next line segment
                double createdSum=0;

                
                //random starting point - must not be noData
                int startX = (int) Math.floor(Math.random()*data.length);
                int startY = (int) Math.floor(Math.random()*data[0].length);
                
                if(data[startX][startY]!=grid.getNoDataValue())
                {
                    
                    //random direction
                    double dx=1-Math.random()*2;
                    double dy=1-Math.random()*2;
                
                    //random value
                    int randomIndex=(int) (Math.floor(Math.random()*(grid.getUniqueDataValues().size()-1)))+1;
                    double randomValue = grid.getUniqueDataValues().get(randomIndex);
                    
                    if(grid.isPresenceAbsence())
                    {
                        if(data[startX][startY]==0)
                        {
                            createdSum+=1;
                            data[startX][startY]=1;
                        }
                    }
                    else
                    {
                        createdSum+=randomValue;
                        data[startX][startY]+=randomValue;
                    }
                    if(data[startX][startY]>newMax) {newMax=data[startX][startY];}
                    if(data[startX][startY]<newMin) {newMin=data[startX][startY];}
                    
                    if(Math.abs(dx)>Math.abs(dy))
                    {
                        if(dx>0) dx=1; else dx=-1;
                        dy=dy*1/dx; 
                    }
                    else
                    {
                        dx=dx*1/dy;
                        if(dy>0) dy=1; else dy=-1;
                    }
                    
                    //weiter
                    boolean hitNoData=false;
                    double currentX=startX;
                    double currentY=startY;
                    while(createdSum<nextSum && !hitNoData)
                    {
                        if(Math.abs(dx)==1)
                        {
                            currentX+=dx;
                            currentY+=dy;
                        }
                        else if(Math.abs(dy)==1)
                        {
                            currentX+=dx;
                            currentY+=dy;
                        }
                        
                        //check if no data - in this case, we're done
                        if(currentX>=data.length || currentX<0 || currentY>=data[0].length || currentY<0 || data[(int) Math.round(currentX)][(int) Math.round(currentY)] == grid.getNoDataValue())
                        {
                            hitNoData=true;
                        }
                        else //create new point and go on with loop
                        {
                            if(grid.isPresenceAbsence())
                            {
                                if(data[(int) Math.round(currentX)][(int) Math.round(currentY)]==0)
                                {    
                                    createdSum+=1;
                                    data[(int) Math.round(currentX)][(int) Math.round(currentY)]=1;
                                }
                            }
                            else
                            {
                                createdSum+=randomValue;
                                data[(int) Math.round(currentX)][(int) Math.round(currentY)]+=randomValue;
                            }
                            if(data[(int) Math.round(currentX)][(int) Math.round(currentY)]>newMax) {newMax=data[(int) Math.round(currentX)][(int) Math.round(currentY)];}
                            if(data[(int) Math.round(currentX)][(int) Math.round(currentY)]<newMin) {newMin=data[(int) Math.round(currentX)][(int) Math.round(currentY)];}
                        }
                    }
                }
            totalSum+=createdSum;
            }

        }
        //todo: not presence-absence --> make lines with sampled unique values
        
        return new DataGrid(data,newMax,newMin,grid.getNoDataValue());

    }

    @Override
    public String getName() {
        return "Move lines to random locations";
    }

    @Override
    public PreProcessor clone() {
        return new ErrorCreatorMoveLines();
    }
    
}
