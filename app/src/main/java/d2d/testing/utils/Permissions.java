package d2d.testing.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import d2d.testing.MainActivity;

public class Permissions {
    public static final int REQUEST_COARSE_LOCATION_CODE = 101;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int MY_WRITE_EXTERNAL_STORAGE_CODE = 103;
    private static final int MY_AUDIO_REQUEST_CODE = 104;

    private MainActivity activity;
    private Context context;

    public Permissions(MainActivity activity, Context context){
        this.activity = activity;
        this.context = context;
    }
    public void memory()
    {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            this.activity.set_camera_has_perm(false);
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Toast.makeText(activity.getApplicationContext(), "WE NEED YOU TO ALLOW US TO USE YOUR CAMERA", Toast.LENGTH_SHORT).show();
                //ask later
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_WRITE_EXTERNAL_STORAGE_CODE);

            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_WRITE_EXTERNAL_STORAGE_CODE);
            }
        }
        else{
            this.activity.set_storage_has_perm(true);
        }
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
    public void audio()
    {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(context,Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            this.activity.set_audio_has_perm(false);
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.CAMERA)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Toast.makeText(activity.getApplicationContext(), "WE NEED YOU TO ALLOW US TO USE YOUR CAMERA", Toast.LENGTH_SHORT).show();
                //ask later
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, MY_AUDIO_REQUEST_CODE);

            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, MY_AUDIO_REQUEST_CODE);
            }
        }
        else{
            this.activity.set_audio_has_perm(true);
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
