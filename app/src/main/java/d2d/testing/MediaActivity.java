package d2d.testing;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import d2d.testing.helpers.Logger;

public class MediaActivity extends AppCompatActivity {

    private MediaPlayer mMediaPlayer;

    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        Intent intent = getIntent();
        Uri myUri = intent.getParcelableExtra("key-uri");

        videoView = findViewById(R.id.videoView);
        final MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);

        videoView.setMediaController(mediaController);

        videoView.setVideoURI(myUri);
        videoView.requestFocus();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                        videoView.setMediaController(mediaController);
                        mediaController.setAnchorView(videoView);

                    }
                });
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Toast.makeText(getApplicationContext(), "Video over", Toast.LENGTH_SHORT).show();
                mp.release();
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Logger.d("What " + what + " extra " + extra);
                return false;
            }
        });
    }


    private void prepareMediaPlayer(Uri uri){
        //Uri ; // initialize Uri here
        MediaPlayer mediaPlayer = new MediaPlayer();
        //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //mediaPlayer.start();
    }

    private void releaseMediaPlayer()
    {
        mMediaPlayer.release();
        mMediaPlayer = null;
    }
}
