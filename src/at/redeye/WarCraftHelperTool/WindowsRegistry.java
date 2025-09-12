// From: https://stackoverflow.com/a/30019357/2917421

package at.redeye.WarCraftHelperTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.log4j.Logger;

public class WindowsRegistry
{
     private static final Logger logger = Logger.getLogger(WindowsRegistry.class);
    
    public static void importSilently(String regFilePath) throws IOException,
            InterruptedException
    {
        if (!new File(regFilePath).exists())
        {
            throw new FileNotFoundException();
        }

        Process importer = Runtime.getRuntime().exec("reg import " + regFilePath);

        importer.waitFor();
    }

    public static void overwriteValue(String keyPath, String keyName,
            String keyValue) throws IOException, InterruptedException
    {
        
        logger.debug(  "reg add \"" + keyPath + "\" /t REG_SZ /v \"" + keyName + "\" /d "
                        + keyValue + " /f" );
        
        Process overwriter = Runtime.getRuntime().exec(
                "reg add " + keyPath + " /t REG_SZ /v \"" + keyName + "\" /d "
                        + keyValue + " /f");

        overwriter.waitFor();
    }

    public static void overwriteBinValue(String keyPath, String keyName,
            String keyValue) throws IOException, InterruptedException
    {
        
        logger.debug(  "reg add \"" + keyPath + "\" /t REG_BINARY /v \"" + keyName + "\" /d "
                        + keyValue + " /f" );
        
        Process overwriter = Runtime.getRuntime().exec(
                "reg add \"" + keyPath + "\" /t REG_BINARY /v \"" + keyName + "\" /d "
                        + keyValue + " /f");

        overwriter.waitFor();
    }
    
    public static void overwriteDwordValue(String keyPath, String keyName,
            String keyValue) throws IOException, InterruptedException
    {
        
        logger.debug(  "reg add \"" + keyPath + "\" /t REG_DWORD /v \"" + keyName + "\" /d "
                        + keyValue + " /f" );
        
        Process overwriter = Runtime.getRuntime().exec(
                "reg add \"" + keyPath + "\" /t REG_DWORD /v \"" + keyName + "\" /d "
                        + keyValue + " /f");

        overwriter.waitFor();
    }    
    
    public static String getValue(String keyPath, String keyName)
            throws IOException, InterruptedException
    {
        Process keyReader = Runtime.getRuntime().exec(
                "reg query \"" + keyPath + "\" /v \"" + keyName + "\"");

        BufferedReader outputReader;
        String readLine;
        StringBuffer outputBuffer = new StringBuffer();

        outputReader = new BufferedReader(new InputStreamReader(
                keyReader.getInputStream()));

        while ((readLine = outputReader.readLine()) != null)
        {
            outputBuffer.append(readLine);
        }

        String[] outputComponents = outputBuffer.toString().split("    ");

        keyReader.waitFor();

        return outputComponents[outputComponents.length - 1];
    }
}