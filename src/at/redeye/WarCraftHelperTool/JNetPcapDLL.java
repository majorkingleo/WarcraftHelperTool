/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.redeye.WarCraftHelperTool;

import at.redeye.FrameWork.base.Setup;
import at.redeye.FrameWork.base.dll_cache.DLLExtractor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author martin
 */
public class JNetPcapDLL implements DLLExtractor
{
    private static final Logger logger = Logger.getLogger(JNetPcapDLL.class);
    
    public static final String LIB_NAME_BASE = "jnetpcap_";
    public static final String PROPERTY_NAME = "JNETPCAP_HOME";

    @Override
    public String getPropertyNameForDllDir() {
        return PROPERTY_NAME;
    }

    @Override
    public void extractDlls() throws IOException
    {
        String envdir = System.getProperty(PROPERTY_NAME);

        for( String lib : getNames() )
        {
            InputStream source = this.getClass().getResourceAsStream("/at/redeye/WarCraftHelperTool/ext_resources/" + lib);

            if( source == null ) {
                logger.error("cannot load " + "/" + lib);
                continue;                
            }

            File tempFile = new File( envdir + "/" + lib );

            FileOutputStream fout = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int read = 0;
            while (read >= 0) {
                fout.write(buffer, 0, read);
                read = source.read(buffer);
            }
            fout.flush();
            fout.close();
            source.close();
        }
    }
    
    public static String getLibName()
    {
        return "jnetpcap_" + System.getProperty("os.arch") + ".dll";
    }

    @Override
    public List<String> getNames() {

        List<String> res = new ArrayList<String>();

       if (Setup.is_win_system())
        {
            String libname = "jnetpcap_" + System.getProperty("os.arch") + ".dll";

            res.add(libname);
        }

        return res;
    }
}
