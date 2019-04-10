package d2d.testing;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import d2d.testing.streaming.Session;
import d2d.testing.streaming.SessionBuilder;
import d2d.testing.streaming.gl.SurfaceView;
import d2d.testing.streaming.rtsp.RtspServer;
import d2d.testing.streaming.video.VideoQuality;

public class StreamActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    private SurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_stream);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mSurfaceView = findViewById(R.id.surface);

        // Sets the port of the RTSP server to 1234
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(RtspServer.KEY_PORT, String.valueOf(1234));
        editor.commit();

        // Configures the SessionBuilder
        SessionBuilder.getInstance()
                .setSurfaceView(mSurfaceView)
                .setPreviewOrientation(90)
                .setContext(getApplicationContext())
                .setAudioEncoder(SessionBuilder.AUDIO_AAC)
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .setVideoQuality(VideoQuality.parseQuality("5000-15-1920-1080"));

        // Starts the RTSP server
        this.startService(new Intent(this,RtspServer.class));
    }
}
