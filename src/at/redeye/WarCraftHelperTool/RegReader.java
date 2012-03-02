/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.WarCraftHelperTool;

import com.ice.jni.registry.*;
import java.awt.Dimension;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;

/**
 *
 * @author moberza
 */
public class RegReader {   
        
    private static final Logger logger = Logger.getLogger(RegReader.class);
    
    public Dimension getWarcraftScreenSize()
    {
        try {
            RegistryKey key = getKey();                   
        
            RegistryValue width = key.getValue("reswidth");
            RegistryValue height = key.getValue("resheight");    
            
            ByteBuffer buf = ByteBuffer.wrap(width.getByteData());
            int iwidth = buf.getInt();
            
            buf = ByteBuffer.wrap(height.getByteData());
            int iheight = buf.getInt();    
            
            logger.debug("warcraft screen size is: " + iwidth + "x" + iheight);
            
            return new Dimension(iwidth, iheight);
        } catch (NoSuchKeyException ex) {
            logger.error(ex, ex);
            return null;
        } catch (RegistryException ex) {
            logger.error(ex, ex);
        }     

        return null;
    }
    
    private RegistryKey getKey() throws NoSuchKeyException, RegistryException
    {
        return Registry.HKEY_CURRENT_USER.openSubKey("Software\\Blizzard Entertainment\\Warcraft III\\Video");
    }
    
    private RegistryKey getKeyForWrite() throws NoSuchKeyException, RegistryException
    {
        return Registry.HKEY_CURRENT_USER.openSubKey("Software\\Blizzard Entertainment\\Warcraft III\\Video",
                RegistryKey.ACCESS_ALL);
    }    

    public void setWarcraftScreenSize(Dimension dim) throws NoSuchKeyException, RegistryException {
        RegistryKey key = getKeyForWrite();
        
        if( key == null ) {
            throw new RuntimeException("Bitte Warcraft einmal starten. Dann nocheinmal probieren.");
        }
        RegDWordValue width = new RegDWordValue( key,"reswidth");
        width.setData(dim.width);
        
        RegDWordValue height = new RegDWordValue( key,"resheight");
        height.setData(dim.height);
        
        key.setValue(width);
        key.setValue(height);
    }    
    
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

}
