package d2d.testing.streaming;

import android.os.HandlerThread;

import java.util.Random;

import d2d.testing.streaming.video.VideoStream;

import static java.util.UUID.randomUUID;

public class RebroadcastSession {

    public final static String TAG = "ServerSession";

    private String mOrigin;
    private String mDestination;
    private int mTimeToLive = 64;
    private long mTimestamp;
    public final String mSessionID;
    private RebroadcastTrackInfo mVideoRebroadcastTrackInfo;
    private RebroadcastTrackInfo mAudioRebroadcastTrackInfo;
    private ServerSession mServerSession;

    /**
     * Creates a streaming session that can be customized by adding tracks.
     */
    public RebroadcastSession() {
        long uptime = System.currentTimeMillis();

        HandlerThread thread = new HandlerThread("d2d.testing.streaming.Session");
        thread.start();

        mTimestamp = (uptime/1000)<<32 & (((uptime-((uptime/1000)*1000))>>32)/1000); // NTP timestamp
        mOrigin = "127.0.0.1";
        mSessionID = randomUUID().toString();
    }

    /**
     * The origin address of the session.
     * It appears in the session description.
     * @param origin The origin address
     */
    public void setOrigin(String origin) {
        mOrigin = origin;
    }

    /**
     * The destination address for all the streams of the session. <br />
     * Changes will be taken into account the next time you start the session.
     * @param destination The destination address
     */
    public void setDestination(String destination) {
        mDestination =  destination;
    }

    /**
     * Returns a Session Description that can be stored in a file or sent to a client with RTSP.
     * @return The Session Description.
     * @throws IllegalStateException Thrown when {@link #setDestination(String)} has never been called.
     */
    public String getSessionDescription() {
        StringBuilder sessionDescription = new StringBuilder();
        sessionDescription.append("v=0\r\n");
        // TODO: Add IPV6 support
        sessionDescription.append("o=- "+mTimestamp+" "+mTimestamp+" IN IP4 "+mOrigin+"\r\n");
        sessionDescription.append("s=Unnamed\r\n");
        sessionDescription.append("i=N/A\r\n");
        sessionDescription.append("c=IN IP4 "+mDestination+"\r\n");
        // t=0 0 means the session is permanent (we don't know when it will stop)
        sessionDescription.append("t=0 0\r\n");
        sessionDescription.append("a=recvonly\r\n");

        if(serverTrackExists(0)) {
            sessionDescription.append(getServerTrack(0).getSessionDescription());
            sessionDescription.append("a=control:trackID="+0+"\r\n");
        }

        if(serverTrackExists(1)) {
            sessionDescription.append(getServerTrack(1).getSessionDescription());
            sessionDescription.append("a=control:trackID="+1+"\r\n");
        }
        return sessionDescription.toString();
    }

    public String getSessionID() {
        return mSessionID;
    }

    /** Returns the destination set with {@link #setDestination(String)}. */
    public String getDestination() {
        return mDestination;
    }


    /**
     * Asynchronously starts all streams of the session.
     **/
    public void start() {

        //....
    }
    /** Stops all existing streams. */
    public void stop() {


        //........
    }

    public boolean serverTrackExists(int id) {
        return mServerSession.trackExists(id);
    }

    public TrackInfo getServerTrack(int id) {
        return mServerSession.getTrack(id);
    }

    public ServerSession getServerSession() {
        return mServerSession;
    }

    public void setServerSession(ServerSession serverSession) {
        this.mServerSession = serverSession;
    }

    public RebroadcastTrackInfo getRebroadcastTrack(int trackId) {
        if (trackId==0)
            return mAudioRebroadcastTrackInfo;
        else
            return mVideoRebroadcastTrackInfo;
    }

    public void startTrack(int trackId) {
        if (serverTrackExists(trackId)){
            getServerTrack(trackId).addRtcpEchoSession(
                    getDestination(),
                    getRebroadcastTrack(trackId).getRemoteRctpPort()
            );

            getServerTrack(trackId).addRtpEchoSession(
                    getDestination(),
                    getRebroadcastTrack(trackId).getRemoteRtpPort()
            );
        }
    }

    public void addRebroadcastTrack(RebroadcastTrackInfo rebroadcastTrackInfo, int trackId) {
        if (trackId==0)

            mAudioRebroadcastTrackInfo = rebroadcastTrackInfo;
        else
            mVideoRebroadcastTrackInfo = rebroadcastTrackInfo;
    }

    public static class RebroadcastTrackInfo {
        private int mRemoteRtpPort;
        private int mRemoteRtcpPort;

        public RebroadcastTrackInfo() {
            setRemotePorts(18000 + new Random().nextInt(2000));
        }

        public int[] getRemotePorts() {
            return new int[]{mRemoteRtpPort, mRemoteRtcpPort};
        }

        public int getRemoteRtpPort() {
            return mRemoteRtpPort;
        }

        public int getRemoteRctpPort() {
            return mRemoteRtcpPort;
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
    }
}
