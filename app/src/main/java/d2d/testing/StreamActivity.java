package d2d.testing;

import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;

import d2d.testing.wifip2p.WifiP2pController;
import d2d.testing.net.packets.DataPacketBuilder;
import d2d.testing.net.threads.selectors.RTSPServerSelector;
import d2d.testing.streaming.video.Session;
import d2d.testing.streaming.video.SessionBuilder;
import d2d.testing.streaming.gl.SurfaceView;
import d2d.testing.streaming.rtsp.RtspClient;


public class StreamActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private final static String TAG = "MainActivity";

    private SurfaceView mSurfaceView;

    private RtspClient rtspClient;

    public Session mSesion;

    private FloatingActionButton recordButton;
    public boolean mRecording = false;

    private boolean groupOwner = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_stream);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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

        //SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        //editor.putString(RtspServer.KEY_PORT, String.valueOf(12345));
        //editor.commit();
        // Starts the RTSP server
        //mIntent = new Intent(this, RtspServer.class);
        //this.startService(mIntent);

        recordButton = findViewById(R.id.button_capture);
        recordButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark)));
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mRecording) {
                    startStreaming();
                } else {
                    stopStreaming();
                }
            }
        });

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

    public void startStreaming() {
        if(WifiP2pController.getInstance().isGroupOwner()) {
            try {
                RTSPServerSelector.getInstance().setAllowLiveStreaming(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            WifiP2pController.getInstance().send(DataPacketBuilder.buildStreamNotifier(true,"192.168.49.1:12345/","Group Owner stream"));
            Toast.makeText(this,"Retransmitting streaming from server to multiple devices simultaneously", Toast.LENGTH_SHORT).show();
        } else {
            rtspClient = new RtspClient();
            rtspClient.setSession(mSesion);
            rtspClient.setStreamPath("/customName");
            rtspClient.setServerAddress("192.168.49.1", 12345);
            rtspClient.startStream();
            Toast.makeText(this,"Retransmitting streaming to GO server for multihopping", Toast.LENGTH_SHORT).show();
        }

        recordButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_stop));
        mRecording = true;
    }

    private void stopStreaming() {
        if (WifiP2pController.getInstance().isGroupOwner()) {
            try {
                RTSPServerSelector.getInstance().setAllowLiveStreaming(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
            WifiP2pController.getInstance().send(DataPacketBuilder.buildStreamNotifier(false, "192.168.49.1:12345/",  "Group Owner stream"));
        } else if (rtspClient.isStreaming()) {
            rtspClient.stopStream();
        }

        recordButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_video_camera));
        mRecording = false;
        Toast.makeText(this,"Stopped retransmitting the streaming", Toast.LENGTH_SHORT).show();
    }

    public void onDestroy(){
        if(mRecording) {
            stopStreaming();
        }

        super.onDestroy();
        //mSesion.stop();
        //this.stopService(mIntent);
        mSesion.stopPreview();
    }
}
