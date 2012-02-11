/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.WarCraftHelperTool;

import at.redeye.FrameWork.base.BaseModuleLauncher;
import at.redeye.FrameWork.base.FrameWorkConfigDefinitions;
import at.redeye.FrameWork.base.LocalRoot;

/**
 *
 * @author moberza
 */
public class Main extends BaseModuleLauncher
{
    Main( String args[] )
    {
        super(args);
        
        root  = new LocalRoot("W3HT", "WarCraftHelperTool", false, false);
        
        root.setBaseLanguage("de");
        root.setDefaultLanguage("en");               
    }

    public void run() {
        /*
        root.addDllExtractorToCache(new JNetPcapDLL());
        root.updateDllCache();       
        */       
        
        AppConfigDefinitions.registerDefinitions();
        FrameWorkConfigDefinitions.registerDefinitions();

        FrameWorkConfigDefinitions.LookAndFeel.value.loadFromString("Nimbus");
        setLookAndFeel(root);

        MainWin mainwin = new MainWin(root);

        mainwin.setVisible(true);
    }
    
    @Override
    public String getVersion() {
        return Version.getVersion();
    }    
    
    public static void main( String args[]  )
    {
        Main main = new Main( args );
        main.run();
    }
}
