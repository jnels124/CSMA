import java.util.*;
import java.net.*;
import java.io.*;

public class SensingTask extends TimerTask {
    //DatagramPacket packetToSend;
    private final static int DESTINATION_PORT = 4567;
    DatagramSocket sendingSocket;
    Timer timer;
    int timeToTransmit;
    InetAddress serverAddress;

    public SensingTask (DatagramSocket skt, InetAddress serverAddress, int timeToTransmit) {
        //this.packetToSend = packet;
        this.sendingSocket = skt;
        this.timer = new Timer();
        this.timeToTransmit = timeToTransmit;
        this.serverAddress = serverAddress;
    }

    public void run () {
        try {
            System.out.println("NIC senses channel to see whether the channel is idle. Current time is " + System.nanoTime());
            String response = sendAndWait("IDLE", true);
        if (response.toUpperCase().equals("NO")) {
                //currTime =
            System.out.println("\nThe channel is busy right now. The current time is " + System.nanoTime());
            this.timer.schedule(new SensingTask(this.sendingSocket, this.serverAddress, this.timeToTransmit), 1000);
        }
        else {
            //System.out.println("First else in handle client ");
            sendAndWait("START", false);
            System.out.println("\nNIC starts transmitting a frame. Current time is " + System.nanoTime() + "\nThe leftover transmission time is " + this.timeToTransmit);
            this.timer.schedule(new CollisionTask(this.sendingSocket,
                                                  this.serverAddress,
                                                  this.timeToTransmit,
                                                  this.timeToTransmit), 1000);
        }
            //this.sendingSocket.send(this.packetToSend);
        } catch (Exception e) {
            System.out.println("Exception while performing sensing task " + e.getMessage());
        }
    }

    final private String sendAndWait(String message, boolean waitForResponse) throws IOException{
        byte outBuff [] = message.getBytes();
        DatagramPacket outgoing =
        new DatagramPacket(outBuff, outBuff.length, this.serverAddress, DESTINATION_PORT);
        this.sendingSocket.send(outgoing);
        if (waitForResponse) {
            byte inBuff [] = new byte[1024];
            DatagramPacket incoming =
            new DatagramPacket(inBuff, inBuff.length);
            this.sendingSocket.receive(incoming);
            return new String (incoming.getData(), 0, incoming.getLength());
        }
        return "";
    }
}