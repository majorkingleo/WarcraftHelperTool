/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.WarCraftHelperTool;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.Logger;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.lan.Ethernet;
import org.jnetpcap.protocol.tcpip.Udp;

/**
 *
 * @author moberza
 */
public class DeviceListener extends Thread
{
    private static final Logger logger = Logger.getLogger(DeviceListener.class);
    PcapIf device;
    StringBuilder errbuf = new StringBuilder();
    PcapPacketHandler<String> jpacketHandler;
    boolean do_stop = false;
    Pcap pcap;
    ConcurrentLinkedQueue<PcapPacket> to_send = new ConcurrentLinkedQueue();
    MainWin mainwin;
    
    int last_sent = 0;
    
    public DeviceListener(PcapIf device, final MainWin mainwin) {
        super(getName(device));

        this.mainwin = mainwin;
        this.device = device;
        
        final Thread sender = this;

        jpacketHandler = new PcapPacketHandler<String>() {

            @Override
            public void nextPacket(PcapPacket packet, String user) {
               
                Ethernet eth = packet.getHeader( new Ethernet());
                
                byte mac_dest[] = eth.destination();
                
                // listen to broadcasts
                for( int i = 0; i < mac_dest.length; i++ )
                {
                    if( mac_dest[i] != -1 )
                        return;
                }
                
                Udp udp = packet.getHeader( new Udp());
                
                if( udp == null )
                    return;                
                
                if( udp.destination() != 6112 )
                    return;                                  
                             
         
                if( last_sent > 0 ) {
                    last_sent -= 1;
                    
                    if( last_sent < 0 ) {
                        last_sent = 0;                        
                    }
                    
                    return;
                }
                
                logger.debug(String.format("%s udp broadcst to port %d", user, udp.destination() ));
                
                mainwin.sendToOther(sender, packet);
                
                //logger.debug(String.format("%20s udp: %s %s", user, udp.toString(), eth.toString()));
                
                /*
                logger.debug(String.format("Received packet at %s caplen=%-4d len=%-4d %s\n",
                        new Date(packet.getCaptureHeader().timestampInMillis()),
                        packet.getCaptureHeader().caplen(), // Length actually captured  
                        packet.getCaptureHeader().wirelen(), // Original length   
                        user // User supplied object  
                        ));                         
                        */
                
            }
        };
    }
    
    @Override
    public void run()
    {
        int snaplen = 64 * 1024;           // Capture all packets, no trucation  
        int flags = Pcap.MODE_NON_PROMISCUOUS; // capture all packets  
        int timeout = 100;           // 10 seconds in millis  
        pcap = Pcap.openLive(device.getName(), snaplen, flags, timeout, errbuf);

        if (pcap == null) {
            logger.error("Error while opening device for capture: "
                    + errbuf.toString());
            return;
        } 
        
        while( !do_stop ) {
            pcap.loop(1, jpacketHandler, this.getName());
            
            PcapPacket send_packet =  to_send.poll();
            
            if( send_packet != null ) { 
                byte bytes[] = send_packet.getByteArray(0, send_packet.size());  
                last_sent++;
                if( pcap.sendPacket(bytes) == 0 ) {
                    mainwin.incSent(this);                    
                    logger.debug(String.format("%s Sent", getName()));
                } else {
                    logger.error(String.format("failed sending %s",send_packet.toString()));
                }               
            }
        }
        
        pcap.close();
    }
    
    void doStop()
    {
        do_stop = true;
        pcap.breakloop();        
    }
    
    public static String getName(PcapIf device ) {
        
        String descr = device.getDescription();
        descr += " " + device.getAddresses().get(0).getAddr().toString();
        
        return descr;
    }
    
    public void send(PcapPacket packet)
    {        
        to_send.add(packet);  
    }

}