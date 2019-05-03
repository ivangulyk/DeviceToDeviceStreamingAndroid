package d2d.testing;

import android.util.Log;
import org.videolan.libvlc.MediaPlayer;
import java.lang.ref.WeakReference;

class MyPlayerListener implements MediaPlayer.EventListener {

    private static String TAG = "PlayerListener";
    private WeakReference<ViewStreamActivity> mOwner;


    public MyPlayerListener(ViewStreamActivity owner) {
        mOwner = new WeakReference<ViewStreamActivity>(owner);
    }

    @Override
    public void onEvent(MediaPlayer.Event event) {
        ViewStreamActivity player = mOwner.get();

        switch(event.type) {
            case MediaPlayer.Event.EndReached:
                Log.d(TAG, "MediaPlayerEndReached");
                player.releasePlayer();
                break;
            case MediaPlayer.Event.Playing:
            case MediaPlayer.Event.Paused:
            case MediaPlayer.Event.Stopped:
            default:
                break;
        }
    }
}
