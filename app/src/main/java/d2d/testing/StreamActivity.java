package d2d.testing;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.io.IOException;

import d2d.testing.helpers.Logger;
import d2d.testing.net.threads.selectors.RTSPServerSelector;
import d2d.testing.streaming.Session;
import d2d.testing.streaming.SessionBuilder;
import d2d.testing.streaming.gl.SurfaceView;
import d2d.testing.streaming.rtsp.RtspServer;
import d2d.testing.streaming.video.VideoQuality;


public class StreamActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private final static String TAG = "MainActivity";

    private SurfaceView mSurfaceView;

    private RTSPServerSelector mRtspServerSelector;

    private Intent mIntent;

    public Session mSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_stream);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(RtspServer.KEY_PORT, String.valueOf(12345));
        editor.commit();

        mSurfaceView = findViewById(R.id.surface);

        // Configures the SessionBuilder
        mSesion = SessionBuilder.getInstance()
                .setSurfaceView(mSurfaceView)
                .setPreviewOrientation(90)
                .setContext(getApplicationContext())
                .setAudioEncoder(SessionBuilder.AUDIO_AAC)
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .build();

        mSurfaceView.getHolder().addCallback(this);

        // Starts the RTSP server
        mIntent = new Intent(this, RtspServer.class);
        this.startService(mIntent);
        /*
        Logger.d("running on create stream activity....");
        if(savedInstanceState == null) {
            Logger.d("no saved instace");
            try {
                mRtspServerSelector = new RTSPServerSelector(42020);
                new Thread(mRtspServerSelector).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        */
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSesion.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void onDestroy(){
        super.onDestroy();
        mSesion.stopPreview();
        mSesion.stop();
        this.stopService(mIntent);
    }
}
