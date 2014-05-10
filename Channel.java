import java.io.*;
import java.net.*;
import java.util.*;

public class Channel {
    private static final int PORT = 4567;
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private double propDelay;
    private static BufferedReader sysIn = new BufferedReader(new InputStreamReader(System.in));
    private ArrayList<Host> connectedHosts;
    private int numHosts;
    private DatagramSocket dgSocket;

    public Channel(double propDelay, int numHosts) throws Exception {
        this.propDelay = propDelay;
        this.numHosts = numHosts;
        this.connectedHosts = new ArrayList<Host>();
        this.dgSocket = new DatagramSocket(PORT);
        handleConnections();
        eventLoop();
    }

    private void eventLoop () throws IOException {
        final long t_now = System.nanoTime();
        byte [] buffer = new byte [DEFAULT_BUFFER_SIZE];
        DatagramPacket currentPacket = new DatagramPacket(buffer, buffer.length);
        this.dgSocket.receive(currentPacket);
        String msg = new String (currentPacket.getData(), 0, currentPacket.getLength()).toUpperCase();
        Host temp = null;

        if(msg == null) return;
        switch(msg) {
        case "IDLE" :
            for (Host host : this.connectedHosts) {
                final long t_start = host.getT_Start();
                final long t_stop = host.getT_Stop();
                boolean yesOrNo =  (t_start<= 0);
                yesOrNo =
                    yesOrNo || (((t_start + 1000 * this.propDelay) >  t_now) &&
                                ((t_stop < 0) || ((t_stop + 1000 * this.propDelay) <= t_now)));
                yesOrNo =
                    yesOrNo || ((t_start <= t_stop) && ((t_stop + 1000 * this.propDelay) <= t_now));

                if (yesOrNo) {
                    buffer = "YES".getBytes();
                    this.dgSocket.send(new DatagramPacket(buffer, buffer.length,
                                                                host.getHostIP(), host.getPort()));
                } else {
                    buffer = "NO".getBytes();
                    this.dgSocket.send(new DatagramPacket(buffer, buffer.length,
                                                                host.getHostIP(), host.getPort()));
                }
            }
            eventLoop();

        case "COLIDE" :
            for (Host host : this.connectedHosts) {
                if (currentPacket.getAddress().equals(host.getHostIP())) {
                    long t_start = host.getT_Start();
                    long t_stop = host.getT_Stop();
                    boolean yesOrNo =  (t_start<= 0);
                    yesOrNo =
                        yesOrNo || (((t_start + 1000 * this.propDelay) >  t_now) &&
                                ((t_stop < 0) || ((t_stop + 1000 * this.propDelay) <= t_now)));
                    yesOrNo =
                        yesOrNo || ((t_start <= t_stop) && ((t_stop + 1000 * this.propDelay) <= t_now));

                    if (yesOrNo) {
                        buffer = "NO".getBytes();
                        this.dgSocket.send(new DatagramPacket(buffer, buffer.length,
                                                                host.getHostIP(), host.getPort()));
                    } else {
                        buffer = "YES".getBytes();
                        this.dgSocket.send(new DatagramPacket(buffer, buffer.length,
                                                                host.getHostIP(), host.getPort()));
                    }
                }
            }
            eventLoop();

        case "START" :
            for (Host host : this.connectedHosts) {
                if (host.getHostIP().equals(currentPacket.getAddress())) {
                    temp = host;
                    break;
                }
            }
            if (temp == null) {
                this.connectedHosts.add(new Host(currentPacket.getAddress(),
                                                 currentPacket.getPort(),
                                                 t_now,
                                                 -1));
            }
            eventLoop();
            //Just fall through to abort
        case "DONE" :

        case "ABORT":
            for (Host host : this.connectedHosts) {
                if (host.getHostIP().equals(currentPacket.getAddress())) {
                    temp = host;
                    break;
                }
            }
            if (temp == null) {
                this.connectedHosts.add(new Host(currentPacket.getAddress(),
                                                 currentPacket.getPort(),
                                                 -1,
                                                 t_now));
            }
            eventLoop();
        }
    }

    private void handleConnections () throws Exception {
        int simStartsSent = 0;
        DatagramPacket currentPacket;
        byte [] buffer = new byte [DEFAULT_BUFFER_SIZE];
        while (true) {
            if (simStartsSent == this.numHosts) break;
            System.out.println("In while");
            try {
                currentPacket = new DatagramPacket(buffer, buffer.length);
                this.dgSocket.receive(currentPacket);
                this.connectedHosts.add(new Host (currentPacket.getAddress(),
                                                  currentPacket.getPort(),
                                                  -1,
                                                  -1));
                System.out.println("The data is " + currentPacket.getData());
                simStartsSent++;
            } catch (Exception e) {
                System.out.println( "There was an error wating for SIMSTART\n" + e.getMessage());
            }
        }
        System.out.println("Sending yes");
        byte [] toClient = "YESSS".getBytes();
        for (Host host : this.connectedHosts) {
            currentPacket = new DatagramPacket(toClient, toClient.length, host.getHostIP(), host.getPort());
            this.dgSocket.send(currentPacket);
        }
    }

    public double getPropDelay () {
        return this.propDelay;
    }

    final public static String messageResponse(final String message) throws IOException{
        System.out.println(message);
        return sysIn.readLine();
    }

    public static void main (String [] args) {
        try {
            double delay =
            Double.parseDouble(messageResponse("What is the end to end propagation delay? (seconds)"));
            int numberHosts =
            Integer.parseInt(messageResponse("How many hosts would you like to use?"));
            new Channel(delay, numberHosts);
        }
        catch (IOException e) {
            System.out.println("I/O Error on server\n" + e.getMessage());
        }
        /*catch (SocketException e) {
            System.out.println("Socket Sockeption on client\n" + e.getMessage());
        }*/
        catch (Exception e) {

            System.out.println("General exception\n"  + e.getMessage());
        }
    }
}
