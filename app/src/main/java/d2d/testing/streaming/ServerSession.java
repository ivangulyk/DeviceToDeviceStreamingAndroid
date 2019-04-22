package d2d.testing.streaming;

import android.os.HandlerThread;

import java.io.IOException;
import d2d.testing.streaming.video.VideoStream;

import static java.util.UUID.randomUUID;

public class ServerSession {

    public final static String TAG = "ServerSession";

    private String mOrigin;
    private String mDestination;
    private int mTimeToLive = 64;
    private long mTimestamp;
    public final String mSessionID;

    private TrackInfo mVideoTrackInfo;
    private TrackInfo mAudioTrackInfo;


    /**
     * Creates a streaming session that can be customized by adding tracks.
     */
    public ServerSession() {
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
        if (mDestination==null) {
            throw new IllegalStateException("setDestination() has not been called !");
        }
        sessionDescription.append("v=0\r\n");
        // TODO: Add IPV6 support
        sessionDescription.append("o=- "+mTimestamp+" "+mTimestamp+" IN IP4 "+mOrigin+"\r\n");
        sessionDescription.append("s=Unnamed\r\n");
        sessionDescription.append("i=N/A\r\n");
        sessionDescription.append("c=IN IP4 "+mDestination+"\r\n");
        // t=0 0 means the session is permanent (we don't know when it will stop)
        sessionDescription.append("t=0 0\r\n");
        sessionDescription.append("a=recvonly\r\n");
        // Prevents two different sessions from using the same peripheral at the same time
        /*
        todo reenviamos la informacion que hemos guardado antes
        if (mAudioStream != null) {
            sessionDescription.append(mAudioStream.getSessionDescription());
            sessionDescription.append("a=control:trackID="+0+"\r\n");
        }
        if (mVideoStream != null) {
            sessionDescription.append(mVideoStream.getSessionDescription());
            sessionDescription.append("a=control:trackID="+1+"\r\n");
        }
        */
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
    public void start() throws IOException {

        if(trackExists(0)) {
            mAudioTrackInfo.startServer();
        }
        if(trackExists(1)) {
            mVideoTrackInfo.startServer();
        }

        //....
    }
    /** Stops all existing streams. */
    public void stop() {
        if(trackExists(0)) {
            mAudioTrackInfo.stopServer();
        }
        if(trackExists(1)) {
            mVideoTrackInfo.stopServer();
        }
    }

    public boolean trackExists(int id) {
        if (id==0)
            return mAudioTrackInfo!=null;
        else
            return mVideoTrackInfo!=null;
    }


    public void addTrack(TrackInfo track, int id) {
        if (id==0)
            mAudioTrackInfo = track;
        else
            mVideoTrackInfo = track;
    }

    public TrackInfo getTrack(int id) {
        if (id==0)
            return mAudioTrackInfo;
        else
            return mVideoTrackInfo;
    }

    public void release(){

    }
}
