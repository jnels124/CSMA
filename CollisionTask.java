import java.util.*;
import java.net.*;
import java.io.*;

public class CollisionTask extends TimerTask {
    private final static int DESTINATION_PORT = 4567;
    InetAddress serverAddress;
    DatagramSocket sendingSocket;
    Timer timer;
    int leftoverTransmissionDuration;
    int totalCollisions;
    int timeToTransmit;

    public CollisionTask (DatagramSocket skt, InetAddress serverAddress, 
                            int leftoverTransmissionDuration, int timeToTransmit, int numCollisions) {
        this.serverAddress = serverAddress;
        this.sendingSocket = skt;
        this.timer = new Timer();
        this.leftoverTransmissionDuration = leftoverTransmissionDuration;
        this.totalCollisions = numCollisions;
        this.timeToTransmit = timeToTransmit;
    }

    public void run () {
        try {
            String response;
            response = sendAndWait("COLIDE", true);
            /*if (delay <= 0) { //Makle sure jobs with negative delay aren't scheduled
                response =  "NO";
            }
            else {
                response = sendAndWait("COLIDE", delay, true);
            }*/
            System.out.println("\nNIC detects collision on channel. Current time is " + System.nanoTime() / 1000000000);
            if ("NO".equals(response.toUpperCase())) {
                if (this.leftoverTransmissionDuration == 0) {
                    sendAndWait("DONE", false);
                    System.out.println("\nDone with transmitting this frame!. The current time is " + System.nanoTime() / 1000000000);
                    System.exit(0);
                } 
                else if (this.leftoverTransmissionDuration > 0) {
                    this.leftoverTransmissionDuration = this.leftoverTransmissionDuration - Math.min(1, this.leftoverTransmissionDuration);
                    System.out.println("The NIC is still transmitting. The leftover transmission time is " + this.leftoverTransmissionDuration +
                                        " The local time is " + System.nanoTime() / 1000000000);
                    this.timer.schedule(new CollisionTask(this.sendingSocket, 
                                                          this.serverAddress,
                                                          this.leftoverTransmissionDuration, 
                                                          this.timeToTransmit,
                                                          this.totalCollisions), this.leftoverTransmissionDuration*1000);
                }
            }
            else {
                int backoff = getBackoff(++this.totalCollisions);
                System.out.println("NIC detects a collision and aborts transmitting the frame and will sense the channel for re-transmission " + backoff + " later. Local time is " + System.nanoTime() / 1000000000);
                sendAndWait("ABORT", false);
                //System.out.println("Calling sensing task from collision  "  + backoff);
                this.timer.schedule(new SensingTask(this.sendingSocket,
                                                    this.serverAddress,
                                                    this.timeToTransmit,
                                                    this.totalCollisions), 1000 * backoff);
            }
        } 
        catch (Exception e) {
           System.out.println("Exception while performing sensing task " + e.getMessage());
        }
    }
 
    final private int getBackoff(int high) {
        Random rand = new Random();
        ArrayList<Integer> possibleK = new ArrayList<Integer>();
        high = (int) Math.pow(2, high - 1);
        for (int i = 0; i <= high; i++)  {
            System.out.println(i + " was added to k's");
            possibleK.add(new Integer(i));
        }
        int k = possibleK.get(rand.nextInt(possibleK.size())) * this.timeToTransmit;
        System.out.println("The size is " + possibleK.size() + " k is " + k );
        return k;
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
