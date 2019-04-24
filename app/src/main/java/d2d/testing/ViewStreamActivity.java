package d2d.testing;


import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.IOException;

public class ViewStreamActivity extends AppCompatActivity {
    VideoView videoView;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //Create a VideoView widget in the layout file
        //use setContentView method to set content of the activity to the layout file which contains videoView
        this.setContentView(R.layout.stream_view);

        videoView = this.findViewById(R.id.video_view);

        //add controls to a MediaPlayer like play, pause.
        MediaController mc = new MediaController(this);
        videoView.setMediaController(mc);


        String ip = "192.168.49.1";
        String port = "12345";

        String path= "rtsp://" + ip + ":" + port;

        videoView.setVideoURI(Uri.parse(path));
        //
        videoView.setMediaController(new MediaController(this));
        //Set the focus
        videoView.requestFocus();
        videoView.start();
    }

    public void onDestroy(){
        super.onDestroy();
        if(videoView != null){
            videoView.stopPlayback();
            videoView.resume();
        }
    }
}
