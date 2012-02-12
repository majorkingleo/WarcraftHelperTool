/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.WarCraftHelperTool;

import at.redeye.FrameWork.base.BaseAppConfigDefinitions;
import at.redeye.FrameWork.base.prm.PrmDefaultChecksInterface;
import at.redeye.FrameWork.base.prm.bindtypes.DBConfig;
import at.redeye.FrameWork.base.prm.impl.GlobalConfigDefinitions;
import at.redeye.FrameWork.base.prm.impl.LocalConfigDefinitions;
import at.redeye.FrameWork.base.prm.impl.PrmDefaultCheckSuite;

/**
 *
 * @author martin
 */
public class AppConfigDefinitions extends BaseAppConfigDefinitions {

    public static DBConfig ListenPort = new DBConfig("ListenPort","6112","UDP Port der gebroadcastet werden soll", new PrmDefaultCheckSuite(PrmDefaultChecksInterface.PRM_IS_LONG));    
   
    public static void registerDefinitions() {

        BaseRegisterDefinitions();

        addLocal(ListenPort);
    }    
}
