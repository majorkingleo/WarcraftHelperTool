/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.WarCraftHelperTool;

import java.awt.Dimension;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 *
 * @author moberza
 */
public class RegReader {   
        
    private static final Logger logger = Logger.getLogger(RegReader.class);
    private static final String PATH = "HKEY_CURRENT_USER\\Software\\Blizzard Entertainment\\Warcraft III\\Video";
    
    public Dimension getWarcraftScreenSize()
    {                
        try {                       
            String swidth = WindowsRegistry.getValue( PATH, "reswidth");
            
            if( swidth == null || swidth.isEmpty() ) {
                return null;
            }
            
            if( swidth.startsWith("0x") ) {
                swidth = swidth.substring(2);
            }                        
            
            long iwidth = Long.parseLong(swidth, 16);
            
            String sheight = WindowsRegistry.getValue(PATH, "resheight");
            
            if( sheight == null || sheight.isEmpty() ) {
                return null;
            }            
            
            if( sheight.startsWith("0x") ) {
                sheight = sheight.substring(2);
            }
            long iheight = Long.parseLong(sheight, 16);    

            logger.debug("warcraft screen size is: " + iwidth + "x" + iheight);
            
            return new Dimension((int)iwidth, (int)iheight);        
        } catch (IOException | InterruptedException ex ) {
            logger.error(ex,ex);
            return null;
        }
    }
 
    public void setWarcraftScreenSize(Dimension dim) throws IOException, InterruptedException {

        WindowsRegistry.overwriteDwordValue(PATH, "reswidth", String.valueOf(dim.width));
        WindowsRegistry.overwriteDwordValue(PATH, "resheight", String.valueOf(dim.height));
    }    
    /*
    public static void main( String args[] ) {
        
        try {
            RegistryKey key = Registry.HKEY_CURRENT_USER.openSubKey("Software\\Blizzard Entertainment\\Warcraft III\\Video");
            RegistryValue width = key.getValue("reswidth");
            RegistryValue height = key.getValue("resheight");
            
            System.out.println(key.getFullName());
            System.out.println("width: " + width.getByteData());
            System.out.println("height: " + height.getByteData());
            
            ByteBuffer buf = ByteBuffer.wrap(width.getByteData());
            int iwidth = buf.getInt();
            
            buf = ByteBuffer.wrap(height.getByteData());
            int iheight = buf.getInt();            
            
            System.out.println("iwidth: " + iwidth);
            System.out.println("iheight: " + iheight);  
            
        } catch( Exception ex ) {
            System.out.println(ex.toString());
            ex.printStackTrace();
        }
    }
*/
}
