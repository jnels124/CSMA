import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    private final static int DESTINATION_PORT = 4567;
    private final static String SIMSTART_MSG = "SIMSTART";
    private static final long START = System.nanoTime();
    private static BufferedReader sysIn =
    new BufferedReader(new InputStreamReader(System.in));
    private InetAddress serverAddress;
    private int initialDelay;
    private int timeToTransmit;
    private DatagramSocket dgSocket;
    private Timer timer;

    public Client (InetAddress serverAddress, int initialDelay,int timeToTransmit )  throws IOException{
        System.out.println("Created Client 2");
        this.serverAddress = serverAddress;
        this.initialDelay = initialDelay;
        this.timeToTransmit = timeToTransmit;
        System.out.println("server Address " + serverAddress);
        this.dgSocket = new DatagramSocket();
        this.timer = new Timer();
        handleClient();
    }

    final private void handleClient () throws IOException{
        System.out.println("Handle client called");
        //int totalCollisions = 0;
        String response;
        long currTime = 0;
        response = sendAndWait(SIMSTART_MSG, true);
        if (!"YESSS".equals(response.toUpperCase())) {
            System.out.println("There was an error. Expected YESSS and got " + response);
            System.exit(1);
        }
        this.timer.schedule(new SensingTask(this.dgSocket, this.serverAddress, this.timeToTransmit, 0), 1000*this.initialDelay);
            //System.out.println("Print response and current time here \nThe response is " + response);
    }

    final private String sendAndWait(String message, boolean waitForResponse) throws IOException{
        byte outBuff [] = message.getBytes();
        DatagramPacket outgoing =
        new DatagramPacket(outBuff, outBuff.length, this.serverAddress, DESTINATION_PORT);
        this.dgSocket.send(outgoing);
        if (waitForResponse) {
            byte inBuff [] = new byte[1024];
            DatagramPacket incoming =
            new DatagramPacket(inBuff, inBuff.length);
            this.dgSocket.receive(incoming);
            return new String (incoming.getData(), 0, incoming.getLength());
        }
        return "";
    }

    final public static String messageResponse(final String message) throws IOException{
        System.out.println(message);
        return sysIn.readLine();
    }


    public static void main (String [] args) {
        try {
            final int frameReady =
            Integer.parseInt(messageResponse("How many seconds until the frame is ready to be sent out?"));
            final int frameTransTime =
            Integer.parseInt(messageResponse("How long does it take the host to transmit the frame entirely?"));
            final String address = messageResponse ("Please enter the server address.");
            new Client (InetAddress.getByName(address), frameReady, frameTransTime);
            System.out.println("Created the Client");
        }
        catch (Exception e) {
            System.out.println("There was an exception on the client\n"  + e.getMessage());
        }
    }
}
