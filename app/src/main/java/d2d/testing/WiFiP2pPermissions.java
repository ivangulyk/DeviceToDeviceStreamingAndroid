package d2d.testing;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

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
            this.activity.set_camera_has_perm(false);
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.CAMERA)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Toast.makeText(activity.getApplicationContext(), "WE NEED YOU TO ALLOW US TO USE YOUR CAMERA", Toast.LENGTH_SHORT).show();
                //ask later
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);

            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
            }
        }
        else{
            this.activity.set_camera_has_perm(true);
        }
    }
    public void location()
    {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            this.activity.set_location_has_perm(false);
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Toast.makeText(activity.getApplicationContext(), "WE NEED YOUR LOCATION FOR THIS FUNCTIONALITY", Toast.LENGTH_SHORT).show();
                //ask later
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION_CODE);

            } else {

                // We can request the permission.
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_COARSE_LOCATION_CODE);
            }
        }
        else{
            this.activity.set_location_has_perm(true);
        }
    }
}
