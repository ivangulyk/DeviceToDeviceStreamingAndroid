package d2d.testing.streaming;

public class TrackInfo {
    private int mRemoteRtpPort = 15220;
    private int mRemoteRtcpPort = 15221;
    private int mLocalRtpPort = 25220;
    private int mLocalRtcpPort = 25221;

    public TrackInfo() {
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