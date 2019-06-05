package d2d.testing.gui;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import  d2d.testing.utils.Logger;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private Camera mCamera = null;

    public CameraPreview(Context context) {
        super(context);
        this.getHolder().addCallback(this);
    }

    public void setCamera(Camera camera)
    {
        Logger.d("CameraPreview: set camera");
        try {
            mCamera = camera;
            mCamera.setPreviewDisplay(this.getHolder());
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Logger.d("CameraPreview: surface created");
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
        } catch (IOException e) {
            Logger.e("Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Logger.d("CameraPreview: surface destroyed");
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Logger.d("CameraPreview: surface changed");
        if (this.getHolder().getSurface() == null)
            return;     // preview surface does not exist

        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        // stop preview before making changes
        try {
            //mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(this.getHolder());
            //mCamera.startPreview();

        } catch (Exception e){
            Logger.e( "Error starting camera preview: " + e.getMessage());
        }
    }
}