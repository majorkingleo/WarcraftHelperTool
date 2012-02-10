/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.WarCraftHelperTool;

import at.redeye.FrameWork.base.BaseDialog;
import at.redeye.FrameWork.base.BaseModuleLauncher;
import at.redeye.FrameWork.base.Root;
import at.redeye.FrameWork.base.bindtypes.DBInteger;
import at.redeye.FrameWork.base.tablemanipulator.TableManipulator;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.apache.log4j.PatternLayout;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;

/**
 *
 * @author moberza
 */
public class MainWin extends BaseDialog {
    
    TableManipulator tm;

    StringBuilder errbuf = new StringBuilder();
    List<PcapIf> alldevs = new ArrayList<PcapIf>();
    ArrayList<InterfaceStruct> interfaces;
    Vector<DeviceListener> listeners;
    
    public MainWin(Root root) {
        super(root,root.getAppTitle());
        initComponents();
        
        /*
        String path = System.getProperty("JNETPCAP_HOME") + "/" + JNetPcapDLL.getLibName();
        System.out.println(System.getProperty("java.library.path"));
        System.load(path);     
        //System.loadLibrary("jnetpcap_x86");        
        */
        
        TextAreaAppender appender = new TextAreaAppender();
        PatternLayout layout = new PatternLayout("%m%n"); //new PatternLayout("%d{ISO8601} %-5p (%F:%L): %m%n");
        appender.setLayout(layout);
        appender.setTextArea(jTextArea1);
        
        BaseModuleLauncher.logger.addAppender( appender  );
        
        tm = new TableManipulator(root,jTable1, new InterfaceStruct());
       
        tm.prepareTable();               
        
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                initDeviceList();
            }
        });
    }

    void initDeviceList()
    {
        interfaces = new ArrayList();
        listeners = new Vector();
        
        int r = Pcap.findAllDevs(alldevs, errbuf);  
        if (r == Pcap.NOT_OK || alldevs.isEmpty()) {  
            logger.error(String.format("Can't read list of devices, error is %s", errbuf  
                .toString()));  
            return;  
        }  
  
        logger.debug("Network devices found:");  
  
        int i = 0;  
        for (PcapIf device : alldevs) {  
            String description =  
                (device.getDescription() != null) ? device.getDescription()  
                    : "No description available";                          
            
            logger.debug(String.format("#%d: %s [%s]", i++, device.getName(), description));                        
            
            InterfaceStruct iface = new InterfaceStruct();
            iface.iface.loadFromString(DeviceListener.getName(device));                                   
            
            interfaces.add(iface);
            
            DeviceListener listener = new DeviceListener(device, this);
            listener.start();
            listeners.add(listener);
        }
        
        tm.addAll(interfaces);
        tm.autoResize();
    }
    
    @Override
    public void close() {
        for (DeviceListener listener : listeners) {
            listener.doStop();
        }

        for (DeviceListener listener : listeners) {
            try {
                listener.join();
            } catch (InterruptedException ex) {
            }
        }

        listeners = null;
        interfaces = null;

        super.close();
    }
    
    void sendToOther( Thread me, PcapPacket packet ) {
        for( int i = 0; i < listeners.size(); i++ ) {
            if( listeners.get(i) != me  ) {
               listeners.get(i).send(packet);                              
            } else {
               DBInteger in =  interfaces.get(i).recv;
               in.loadFromCopy(in.getValue()+1);
            }
        }
        
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                tm.updateUI();
            }        
        });
    }        
    
    void incSent( Thread me ) {
        for( int i = 0; i < listeners.size(); i++ ) {
            if( listeners.get(i) == me  ) {
               DBInteger in =  interfaces.get(i).sent;
               in.loadFromCopy(in.getValue()+1);
               break;
            }
        }
        
       java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                tm.updateUI();
            }        
        });        
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jSplitPane1.setLeftComponent(jScrollPane1);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        jSplitPane1.setRightComponent(jScrollPane2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
