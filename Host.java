import java.net.*;
public class Host {
    private InetAddress hostIP;
    private int port;
    private long t_start;
    private long t_stop;

    public Host (InetAddress hostIP, int port, long t_start, long t_stop) {
        this.hostIP = hostIP;
        this.port = port;
        this.t_start = t_start;
        this.t_stop = t_stop;
    }

    public InetAddress getHostIP () {
        return this.hostIP;
    }

    public void setPort (int newPort) {
        this.port = newPort;
    }

    public int getPort () {
        return this.port;
    }

    public void setT_Start (long newStart) {
        this.t_start = newStart;
    }

    public long getT_Start () {
        return this.t_start;
    }

    public void setT_Stop (long newStop) {
        this.t_start = newStop;
    }

    public long getT_Stop () {
        return this.t_start;
    }
}
