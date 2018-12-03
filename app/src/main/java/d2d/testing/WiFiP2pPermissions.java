package d2d.testing;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

public class WiFiP2pPermissions {
    public static final int REQUEST_COARSE_LOCATION_CODE = 101;
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    private MainActivity activity;
    private Context context;

    public WiFiP2pPermissions(MainActivity activity,Context context){
        this.activity = activity;
        this.context = context;
    }

    public void camera()
    {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.CAMERA)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                Toast.makeText(activity.getApplicationContext(), "WE NEED PERMISSIONS BECAUSE YES.. BLAH BLAH BLAH JA JA JA", Toast.LENGTH_SHORT).show();
                //ask later
                /*
              new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkPermissions();
                    }
                }, 5 * 1000); // afterDelay will be executed after (secs*1000) milliseconds.
                */
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA},
                        MY_CAMERA_REQUEST_CODE);
            }
        }
    }
    public void location()
    {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                Toast.makeText(activity.getApplicationContext(), "WE NEED PERMISSIONS BECAUSE YES.. BLAH BLAH BLAH JA JA JA", Toast.LENGTH_SHORT).show();
                //ask later
                /*
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkPermissions();
                    }
                }, 5 * 1000); // afterDelay will be executed after (secs*1000) milliseconds.
                */
            } else {

                // We can request the permission.
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_COARSE_LOCATION_CODE);
            }
        }

    }
}
