package d2d.testing.streaming;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SelectableChannel;
import java.util.Random;

import d2d.testing.net.threads.selectors.UDPServerSelector;

public class TrackInfo {
    private int mRemoteRtpPort;
    private int mRemoteRtcpPort;
    private int mLocalRtpPort;
    private int mLocalRtcpPort;

    private UDPServerSelector mRtcpUdpServer;
    private Thread mRtcpUdpServerThread;
    private UDPServerSelector mRtpUdpServer;
    private Thread mRtpUdpServerThread;

    public TrackInfo() {
        setLocalPorts(15000 * new Random().nextInt(1000));
        setRemotePorts(14000 * new Random().nextInt(1000));
    }

    public void startServer() throws IOException {
        mRtcpUdpServer = new UDPServerSelector(null, mLocalRtcpPort);
        mRtcpUdpServerThread = new Thread(mRtcpUdpServer);
        mRtcpUdpServerThread.start();
        mRtpUdpServer = new UDPServerSelector(null, mLocalRtpPort);
        mRtpUdpServerThread = new Thread(mRtpUdpServer);
        mRtpUdpServerThread.start();
    }

    public void stopServer() {
        mRtcpUdpServer.stop();
        mRtcpUdpServer = null;
        mRtcpUdpServerThread.interrupt();
        mRtcpUdpServerThread = null;

        mRtpUdpServer.stop();
        mRtpUdpServer = null;
        mRtpUdpServerThread.interrupt();
        mRtpUdpServerThread = null;
    }

    public void addEchoSession(InetAddress address, int port) {
        mRtcpUdpServer.addConnectionUDP(address, port);
        mRtpUdpServer.addConnectionUDP(address, port);
    }

    public void removeSession(SelectableChannel channel) {
        
    }

    public int[] getRemotePorts() {
        return new int[]{mRemoteRtpPort, mRemoteRtcpPort};
    }

    public void setRemotePorts(int dport) {
        if (dport % 2 == 1) {
            mRemoteRtpPort = dport-1;
            mRemoteRtcpPort = dport;
        } else {
            mRemoteRtpPort = dport;
            mRemoteRtcpPort = dport+1;
        }
    }
    public void setRemotePorts(int rtpPort, int rtcpPort) {
        mRemoteRtpPort = rtpPort;
        mRemoteRtcpPort = rtcpPort;
    }

    public int[] getLocalPorts() {
        return new int[]{mLocalRtpPort, mLocalRtcpPort};
    }

    public void setLocalPorts(int dport) {
        if (dport % 2 == 1) {
            mLocalRtpPort = dport-1;
            mLocalRtcpPort = dport;
        } else {
            mLocalRtpPort = dport;
            mLocalRtcpPort = dport+1;
        }
    }
    public void setLocalPorts(int rtpPort, int rtcpPort) {
        mLocalRtpPort = rtpPort;
        mLocalRtcpPort = rtcpPort;
    }
}