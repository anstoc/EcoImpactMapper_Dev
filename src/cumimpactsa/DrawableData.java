/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cumimpactsa;

import java.awt.image.BufferedImage;

/**
 *
 * @author ast
 */
public interface DrawableData
{
    public String getName();
    public DataSourceInfo getSource();
    public int getType();
    public BufferedImage getImage(ImageCreator creator, boolean variation);
    public int getDrawingDataType();
}
