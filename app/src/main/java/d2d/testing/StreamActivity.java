package d2d.testing;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import java.io.IOException;

import d2d.testing.net.threads.selectors.ClientSelector;
import d2d.testing.net.threads.selectors.RTSPServerSelector;
import d2d.testing.streaming.Session;
import d2d.testing.streaming.SessionBuilder;
import d2d.testing.streaming.gl.SurfaceView;
import d2d.testing.streaming.rtsp.RtspServer;

public class StreamActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    private SurfaceView mSurfaceView;

    private RTSPServerSelector mRtspServerSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_stream);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mSurfaceView = findViewById(R.id.surface);

        // Configures the SessionBuilder
        Session s = SessionBuilder.getInstance()
                .setSurfaceView(mSurfaceView)
                .setPreviewOrientation(90)
                .setContext(getApplicationContext())
                .setAudioEncoder(SessionBuilder.AUDIO_AAC)
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .build();
        try {
            s.syncStart();
        } catch (IOException e) {
            e.printStackTrace();
        }
        s.syncStop();

        // Starts the RTSP server
        this.startService(new Intent(this,RtspServer.class));

        try {
            mRtspServerSelector = new RTSPServerSelector(1234);
            new Thread(mRtspServerSelector).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
