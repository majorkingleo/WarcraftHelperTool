/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.WarCraftHelperTool;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;


public class TextAreaAppender extends WriterAppender {
    
    private JTextArea jTextArea = null;

    /**
     * Set the target JTextArea for the logging information to appear.
     */
    public void setTextArea(JTextArea jTextArea) {
        this.jTextArea = jTextArea;
    }

    /**
     * Format and then append the loggingEvent to the stored JTextArea.
     */
    @Override
    public void append(LoggingEvent loggingEvent) {
        final String message = this.layout.format(loggingEvent);

        // Append formatted message to textarea using the Swing Thread.
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                jTextArea.append(message);
            }
        });
    }
}
