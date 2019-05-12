package d2d.testing.streaming;

import android.os.HandlerThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static java.util.UUID.randomUUID;

public class ServerSession {

    public final static String TAG = "ServerSession";

    private String mOrigin;
    private String mDestination;
    private int mTimeToLive = 64;
    public final String mSessionID;

    private TrackInfo mVideoTrackInfo;
    private TrackInfo mAudioTrackInfo;
    private String mSessionIDessionDescription;


    /**
     * Creates a streaming session that can be customized by adding tracks.
     */
    public ServerSession() {
        long uptime = System.currentTimeMillis();

        HandlerThread thread = new HandlerThread("d2d.testing.streaming.Session");
        thread.start();

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

    public void addVideoTrack(TrackInfo track) {
        mVideoTrackInfo = track;
    }

    public void addAudioTrack(TrackInfo track) {
        mAudioTrackInfo = track;
    }

    public TrackInfo getTrack(int id) {
        if (id==0)
            return mAudioTrackInfo;
        else
            return mVideoTrackInfo;
    }

    public void release(){

    }

    public String getTimeout() {
        return "60";
    }
}
