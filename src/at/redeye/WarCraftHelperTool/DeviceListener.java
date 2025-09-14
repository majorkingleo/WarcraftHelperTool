/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.redeye.WarCraftHelperTool;

import java.net.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.Logger;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.UdpPacket;
import org.pcap4j.packet.UnknownPacket;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.packet.namednumber.IpVersion;
import org.pcap4j.util.MacAddress;

/**
 *
 * @author moberza
 */

public class DeviceListener extends Thread
{
    private static final Logger logger = Logger.getLogger(DeviceListener.class);
    PcapNetworkInterface device;
    StringBuilder errbuf = new StringBuilder();
    PacketListener jpacketHandler;
    boolean do_stop = false;
    ConcurrentLinkedQueue<Packet> to_send = new ConcurrentLinkedQueue();
    MainWin mainwin;
    
    int last_sent = 0;
    int listenPort = 0;
    
    Inet4Address broadcast_address;
    Inet4Address local_address = null;
    private PcapHandle pcap;
    private PcapHandle handle4send;
    boolean is_valid = true;
    
    public DeviceListener(PcapNetworkInterface device, final MainWin mainwin) {
        super(getName(device));

        this.mainwin = mainwin;
        this.device = device;               
        
        
        if( device.isLoopBack() ) {
           is_valid = false;
           return;
       }
        
       listenPort = Integer.valueOf(mainwin.getRoot().getSetup().getLocalConfig(AppConfigDefinitions.ListenPort));
       
       var addresses = device.getAddresses();
       
       if( addresses.isEmpty() ) {
           is_valid = false;
           return;
       }      
       
       for( var addr : addresses ) {
                      
           if( addr.getAddress() instanceof Inet4Address ) {               
               local_address = (Inet4Address) addr.getAddress();
               
               byte netmask_bytes[] = addr.getNetmask().getAddress();
               int netmask = (int) unsignedIntToLong(netmask_bytes);                        

               byte ip_bytes[] = addresses.get(0).getAddress().getAddress();
               int ip = (int) unsignedIntToLong(ip_bytes);                        

               // apply netmask
               int broadcast_ip = ip | ( ~ netmask );

               // logger.debug(String.format("Netmask is: %x ip is: %x broadcast ip: %x", netmask, ip, broadcast_ip));            

               try {
                    broadcast_address = (Inet4Address)Inet4Address.getByName(intToIp(broadcast_ip));           
                    logger.debug("Broadcast Address: " + broadcast_address.toString() + " for device address " + addresses.get(0).getAddress().toString());               
               } catch( UnknownHostException ex ) {
                   // will not be reached... from an ip address as source
               }
               break;
           }
       }
       
       if( local_address == null ) {
           is_valid = false;
           return;
       }
        
       final Thread sender = this;

        jpacketHandler = new PacketListener() {
           
            @Override
            public void gotPacket(Packet packet) {    
                         
                IpV4Packet ipv4 = null;
                EthernetPacket eth = null;
                
                try {
                    eth = packet.get( EthernetPacket.class );
                    ipv4 = packet.get(IpV4Packet.class);                   

                } catch( IndexOutOfBoundsException ex ) {
                    logger.debug("unknown packet",ex);
                    return;
                }  
                
                if( eth == null || ipv4 == null )
                    return;                                
                
                byte mac_dest[] = eth.getHeader().getDstAddr().getAddress();
                
                // listen to broadcasts
                for( int i = 0; i < mac_dest.length; i++ )
                {
                    if( mac_dest[i] != -1 )
                        return;
                }
                
                byte ip_dest[] = ipv4.getHeader().getDstAddr().getAddress();
                
                // listen to broadcasts
                for( int i = 0; i < ip_dest.length; i++ )
                {
                    if( ip_dest[i] != -1 )
                        return;
                }                
                
                
                UdpPacket udp = packet.get( UdpPacket.class );
                
                if( udp == null )
                    return;                
                
                if( listenPort >= 0 ) {
                    if( udp.getHeader().getDstPort().valueAsInt() != listenPort )
                        return;
                }
                            

                //if( last_sent > 0 ) {
                //    last_sent -= 1;
                //    
                //    if( last_sent < 0 ) {
                //        last_sent = 0;                        
                //    }
                //    
                //    return;
                //}                               
                
                
                logger.debug(String.format("udp broadcst on port %d detected", udp.getHeader().getDstPort().valueAsInt() ));
                
                mainwin.sendToOther(sender, packet);
                
                //logger.debug(String.format("%20s udp: %s %s", user, udp.toString(), eth.toString()));
                
                
                //logger.debug(String.format("Received packet at %s caplen=%-4d len=%-4d %s\n",
                //        new Date(packet.getCaptureHeader().timestampInMillis()),
                //        packet.getCaptureHeader().caplen(), // Length actually captured  
                //        packet.getCaptureHeader().wirelen(), // Original length   
                //        user // User supplied object  
                //        ));                                                
                
            }         
        };                     
    }
    
    public static Long ipToInt(String addr) {

        String[] addrArray = addr.split("\\.");

        long num = 0;

        for (int i = 0; i < addrArray.length; i++) {
            int power = 3 - i;
            num += ((Integer.parseInt(addrArray[i]) % 256 * Math.pow(256, power)));
        }

        return num;
    }
    
    public static String intToIp(int i) {

        return ((i >> 24) & 0xFF) + "."
                + ((i >> 16) & 0xFF) + "."
                + ((i >> 8) & 0xFF) + "."
                + (i & 0xFF);
    }
    
    public static long unsignedIntToLong(byte[] b) {
        long l = 0;
        l |= b[0] & 0xFF;
        l <<= 8;
        l |= b[1] & 0xFF;
        l <<= 8;
        l |= b[2] & 0xFF;
        l <<= 8;
        l |= b[3] & 0xFF;
        return l;
    } 
    
    @Override
    public void run()
    {        
        int snaplen = 64 * 1024;           // Capture all packets, no truncation  
        PromiscuousMode flags = PromiscuousMode.NONPROMISCUOUS; // capture all packets  
        int timeout = 100;           // 10 seconds in millis  
        
        try {
            pcap = device.openLive(snaplen, flags, timeout);
        } catch (PcapNativeException ex) {
            logger.error("cannot open live capture",ex);
        }

        if (pcap == null) {
            logger.error("Error while opening device for capture: "
                    + errbuf.toString());
            return;
        } 
        
        try {
            handle4send = device.openLive(snaplen, flags, timeout);
        } catch (PcapNativeException ex) {
            logger.error("cannot open live capture",ex);
        }

        if (handle4send == null) {
            logger.error("Error while opening device for sending: "
                    + errbuf.toString());
            return;
        } 
        
        
        while( !do_stop ) {     
            
            try {
                pcap.loop(1, jpacketHandler);
            } catch (PcapNativeException | NotOpenException ex) {
                logger.error( "pcap.loop failed", ex );
            } catch(InterruptedException ex ) {
                // no logging here
            }
                
            if( do_stop )
                break;
            
            Packet send_packet =  to_send.poll();
            
            if( send_packet != null ) { 
                     
                if( device.getLinkLayerAddresses().isEmpty() ) {
                    continue;
                }
                
                try {
                    UdpPacket udp = send_packet.get( UdpPacket.class );
                    IpV4Packet ipv4 = send_packet.get( IpV4Packet.class );
                    IpV4Packet.IpV4Header header = ipv4.getHeader();
                    
                    IpV4Packet.Builder ipv4Builder = new IpV4Packet.Builder();
                    
                    //Inet4Address specific_address = (Inet4Address)(Inet4Address.getAllByName("25.35.134.238")[0]);
                    
                    ipv4Builder
                            .version(IpVersion.IPV4)
                            .tos(header.getTos())
                            .identification(header.getIdentification())
                            .protocol(header.getProtocol())
                            .srcAddr(local_address)
                            .dstAddr(broadcast_address)
                            //.dstAddr(specific_address)
                            .correctChecksumAtBuild(true)
                            .correctLengthAtBuild(true)
                            .ttl(header.getTtl())
                            .payloadBuilder(new UnknownPacket.Builder().rawData(udp.getRawData()));
                            
                    
                    
                    EthernetPacket.Builder etherBuilder = new EthernetPacket.Builder();
                    etherBuilder
                            .type(EtherType.IPV4)
                            .srcAddr(MacAddress.getByAddress(device.getLinkLayerAddresses().getFirst().getAddress()))
                            .dstAddr(MacAddress.ETHER_BROADCAST_ADDRESS)
                            .paddingAtBuild(true)                            
                            .payloadBuilder(ipv4Builder);
                    
                    Packet p = etherBuilder.build();
                    logger.debug(p);
                    
                    handle4send.sendPacket(p);
                            
                    last_sent++;
                    mainwin.incSent(this);
                    
                    /*
                    Ethernet ether = send_packet.getHeader(new Ethernet());
                    ether.source(device.getHardwareAddress());                    
                    Ip4 ipv4 = send_packet.getHeader(new Ip4());
                    ipv4.source(local_address.getAddress());                    
                    ipv4.destination(broadcast_address.getAddress());
                    ipv4.checksum(ipv4.calculateChecksum());
                    ether.checksum(ether.calculateChecksum());
                    
                    byte bytes[] = send_packet.getByteArray(0, send_packet.size());
                    
                    if( pcap.sendPacket(bytes) == 0 ) {
                        last_sent++;
                        mainwin.incSent(this);                    
                        // logger.debug(String.format("%s Sent", getName()));
                    } else {
                        logger.error(String.format("failed sending %s",send_packet.toString()));
                    }                        
                    */
                } catch (PcapNativeException | NotOpenException ex) {
                    logger.error(ex,ex);
                }                 
             }            
        }
        
        pcap.close();        
    }
    
    void doStop()
    {
        do_stop = true;
        try {
            pcap.breakLoop();
        } catch (NotOpenException ex) {
            logger.error(ex,ex);
        }
    }
    
    public static String getName(PcapNetworkInterface device ) {
        
        String descr = device.getDescription();
        
        var addresses = device.getAddresses();
        
        if( addresses != null && !addresses.isEmpty() ) {
            descr += " " + addresses.getFirst().toString();
        }
        
        return descr;
    }
    
    public void send(Packet packet)
    {        
        to_send.add(packet);  
        try {
            pcap.breakLoop();
        } catch (NotOpenException ex) {
            logger.error(ex,ex);
        }
    }
    
    boolean valid() {
        return is_valid;
    }

}