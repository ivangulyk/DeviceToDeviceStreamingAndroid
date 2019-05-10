package d2d.testing.streaming;

import android.os.HandlerThread;

import d2d.testing.net.threads.selectors.RTSPServerSelector;
import d2d.testing.streaming.video.VideoStream;

import static java.util.UUID.randomUUID;

public class RebroadcastSession {

    public final static String TAG = "ServerSession";

    private String mOrigin;
    private String mDestination;
    private int mTimeToLive = 64;
    private long mTimestamp;
    public final String mSessionID;
    private TrackInfo mVideoTrackInfo;
    private TrackInfo mAudioTrackInfo;
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

    /** You probably don't need to use that directly, use the {@link SessionBuilder}.
     * todo funcion para guardar informacion del streaming de audio recibida en el announce para reenviarla
     * todo funcion para guardar informacion del streaming de audio del setup (puertos y demas)
    void addAudioTrack(AudioStream track) {
        removeAudioTrack();
        mAudioStream = track;
    }
     */

    /** You probably don't need to use that directly, use the {@link SessionBuilder}.
     * * todo funcion para guardar informacion del streaming de video recibida en el announce para reenviarla
     * todo funcion para guardar informacion del streaming de video del setup (puertos y demas)
    void addVideoTrack(VideoStream track) {
        removeVideoTrack();
        mVideoStream = track;
    }
     */

    /** Returns the underlying {@link AudioStream} used by the {@link Session}.
     * todo getters para la informacion recibida del announce
    public AudioStream getAudioTrack() {
        return mAudioStream;
    }
     */

    /** Returns the underlying {@link VideoStream} used by the {@link Session}.
    public VideoStream getVideoTrack() {
        return mVideoStream;
    }
     */

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

        mServerSession.getSessionDescription();
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
    public void start() {

        //....
    }
    /** Stops all existing streams. */
    public void stop() {


        //........
    }


    /** Deletes all existing tracks & release associated resources. */
    public void release() {

        //.........
    }

    /*
    public boolean trackExists(int id) {
        if (id==0)
            return mAudioStream!=null;
        else
            return mVideoStream!=null;
    }
    */

    public TrackInfo getTrack(int id) {
        if (id==0)
            return mAudioTrackInfo;
        else
            return mVideoTrackInfo;
    }

    public void setServerSession(ServerSession serverSession) {
        this.mServerSession = serverSession;
    }

    public ServerSession getServerSession() {
        return mServerSession;
    }
}
