package d2d.testing;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import d2d.testing.helpers.Logger;
import d2d.testing.net.WifiP2pController;
import d2d.testing.net.threads.workers.SendStreamWorker;

import static d2d.testing.net.helpers.IOUtils.getOutputMediaFile;

public class CameraActivity extends AppCompatActivity {

    private static final String[] FLASH_OPTIONS = {
            Camera.Parameters.FLASH_MODE_AUTO,
            Camera.Parameters.FLASH_MODE_OFF,
            Camera.Parameters.FLASH_MODE_ON,
    };

    private static final int[] FLASH_ICONS = {
            R.drawable.ic_flash_auto,
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_on,
    };

    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private SendStreamWorker mStreamWorker;
    private CameraOrientationEventListener mOrientationListener;

    private boolean mVideoMode = false;
    private boolean mRecording = false;
    private int mCurrentFlash;
    private int mCurrentCamera;
    private String mCurrentFocus;

    private List<String> availableFocusModes;
    private List<String> availableFlashModes;

    private FloatingActionButton btnSwitchCamera;
    private FloatingActionButton btnCapture;
    private FloatingActionButton btnSwitchVideo;

    private Toolbar cameraToolbar;
    private MenuItem btnSwitchFlash;

    private WifiP2pController mController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d("CameraActivity: onCreate() starting");

        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //se puede mover al xml? mirar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        this.setContentView(R.layout.camera);
        this.initialWork();

        mOrientationListener = new CameraOrientationEventListener(this);

        if (mOrientationListener.canDetectOrientation()) {
            mOrientationListener.enable();
        }

        mCurrentCamera = Camera.CameraInfo.CAMERA_FACING_BACK; //RECORDAR LA CAMARA FLASH ETC...?
        mCurrentFlash = 0;
        mCurrentFocus = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;

        mPreview = new CameraPreview(this); // Create our Preview view and set it as the content of our activity.
        ((FrameLayout)findViewById(R.id.camera_preview)).addView(mPreview);
    }

    public void initialWork() {

        btnSwitchCamera = findViewById(R.id.button_switch_camera);
        btnCapture = findViewById(R.id.button_capture);
        btnSwitchVideo = findViewById(R.id.button_switch_video);
        cameraToolbar = findViewById(R.id.camera_toolbar);

        setSupportActionBar(cameraToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    public void updateCameraParameters() {
        Camera.Parameters params = mCamera.getParameters();

        availableFocusModes = params.getSupportedFocusModes();
        availableFlashModes = params.getSupportedFlashModes();

        if (availableFlashModes == null) {
            //todo no flash mode hide button etc...
        } else {
            //todo set flash buttons..
            if (!availableFlashModes.contains(FLASH_OPTIONS[mCurrentFlash])) {
                if (availableFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                    mCurrentFlash = 0;
                }
            }
            params.setFlashMode(FLASH_OPTIONS[mCurrentFlash]);
        }

        //todo set focus buttons..???
        if (!availableFocusModes.contains(mCurrentFocus)) { //IF NOT AVAILABLE CURRENT FOCUS WE SET DEFAULT FOCUS
            if (!mVideoMode && availableFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
                mCurrentFocus = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
            else if (mVideoMode && availableFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
                mCurrentFocus = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
            else if (availableFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
                mCurrentFocus = Camera.Parameters.FOCUS_MODE_AUTO;
        }
        params.setFocusMode(mCurrentFocus);

        mCamera.setParameters(params);
    }

    public void setButtonsRotation(int rotation) {
        btnSwitchCamera.setRotation(rotation);
        btnCapture.setRotation(rotation);
        btnSwitchVideo.setRotation(rotation);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoMode && mRecording) {
            //todo stop recording?
        }
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d("CameraActivity: onCreate() starting");

        try {
            mCamera = Camera.open(mCurrentCamera);
        } catch (Exception e) {
            Logger.e("CameraActivity: Error opening the camera");
        }

        updateCameraParameters();
        mPreview.setCamera(mCamera);
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.camera_menu, menu);
        btnSwitchFlash = menu.findItem(R.id.switch_flash);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_flash:

                if (mCamera != null) {
                    mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
                    item.setTitle(FLASH_OPTIONS[mCurrentFlash]);
                    item.setIcon(FLASH_ICONS[mCurrentFlash]);

                    Camera.Parameters params = mCamera.getParameters();

                    List<String> flashModes = params.getSupportedFlashModes();
                    if (flashModes.contains(FLASH_OPTIONS[mCurrentFlash])) {
                        params.setFlashMode(FLASH_OPTIONS[mCurrentFlash]);
                    }
                    // set Camera parameters
                    mCamera.setParameters(params);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
        }
    }

    public void onSwitchCamera (View view){
        if(Camera.getNumberOfCameras() < 2)
            return;  //ONLY ONE CAMERA

        mCamera.stopPreview();
        releaseCamera();

        if (mCurrentCamera == Camera.CameraInfo.CAMERA_FACING_BACK)
            mCurrentCamera = Camera.CameraInfo.CAMERA_FACING_FRONT;
        else
            mCurrentCamera = Camera.CameraInfo.CAMERA_FACING_BACK;

        mCamera = Camera.open(mCurrentCamera);
        updateCameraParameters();
        mPreview.setCamera(mCamera);
    }

    private Camera.Parameters setDefaultFlashMode(Camera.Parameters params, List<String> flashModes) {
        params.setFlashMode(FLASH_OPTIONS[mCurrentFlash]);
        return params;
    }

    public void onCapture(View view){
        if(!mVideoMode){
            mCamera.takePicture(null, null, mPictureCallback);
        }else {
            if(mRecording){
                try {
                    mMediaRecorder.stop();
                    releaseMediaRecorder();
                } catch (Exception e) {
                    Logger.d("Exception stopping MediaRecorder: " + e.toString());
                }
                mStreamWorker.stop();

                //btnSwitchMode.setEnabled(false);
                btnCapture.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark)));
                btnCapture.setImageDrawable(null);
                mRecording = false;
            }else{
                try {
                    prepareVideoRecorder();
                    mMediaRecorder.start();
                } catch (Exception e) {
                    Logger.d("Exception starting MediaRecorder: " + e.toString());
                }
                mStreamWorker.start();

                //btnSwitchMode.setEnabled(true);
                btnCapture.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorCameraButton)));
                btnCapture.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_stop));
                mRecording = true;
            }
        }
    }

    public void onSwitchMode(View view){
        if(mVideoMode)
        {
            btnCapture.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorCameraButton)));
            btnCapture.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_camera));
            btnSwitchVideo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_video_camera));
            btnSwitchCamera.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_switch_camera));
            mVideoMode = false;
        } else {
            btnCapture.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark)));
            btnCapture.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_video_camera));
            btnSwitchVideo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_camera));
            btnSwitchCamera.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_switch_camera));
            mVideoMode = true;
        }
    }

    private boolean prepareVideoRecorder(){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        int audio_bitrate;
        int video_bitrate;
        int video_framerate;
        int video_size;
        int video_width = 1920;
        int video_height = 1080;

        audio_bitrate =  Integer.parseInt(settings.getString("audio_bitrate", "128")) * 1024;
        video_bitrate =  Integer.parseInt(settings.getString("video_bitrate", "4500")) * 1024;
        video_framerate =  Integer.parseInt(settings.getString("video_framerate", "30"));
        video_size = Integer.parseInt(settings.getString("video_size", "0"));

        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // set TS
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //mMediaRecorder.setAudioChannels(1);
        //mMediaRecorder.setAudioSamplingRate(44100);
        //mMediaRecorder.setAudioEncodingBitRate(audio_bitrate);

        //mMediaRecorder.setVideoSize(video_width, video_height);
        //mMediaRecorder.setVideoFrameRate(video_framerate);
        //mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //mMediaRecorder.setVideoEncodingBitRate(video_bitrate);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        //mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        //todo output a socket?

        //get a handle to your read and write fd objects.
        //mStreamWorker = new SendStreamWorker(new ParcelFileDescriptor.AutoCloseInputStream(fdPair[0]));
        //new ParcelFileDescriptor.AutoCloseOutputStream(fdPair[1]);
        //mMediaRecorder.setOutputFile(fdPair[1].getFileDescriptor());

        try {
            File file = getOutputMediaFile("video.mp4");
            FileOutputStream fileOutput = new FileOutputStream(file);
            FileInputStream fileInput = new FileInputStream(file);
            mMediaRecorder.setOutputFile(fileOutput.getFD());
            mStreamWorker = new SendStreamWorker(fileInput);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Logger.d("IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Logger.d("IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                camera.reconnect();
                mPreview.setCamera(camera);
            } catch (IOException e) {
                e.printStackTrace();
            }

            File pictureFile = getOutputMediaFile("imagen_camara.jpg");
            if (pictureFile == null){
                Logger.d("Error creating media file, check storage permissions");
                return;
            }

            try {
                //todo podemos enviarlo directamente

                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Logger.d("File not found: " + e.getMessage());
            } catch (IOException e) {
                Logger.d("Error accessing file: " + e.getMessage());
            }
        }
    };

    private class CameraOrientationEventListener extends OrientationEventListener {

        public CameraOrientationEventListener(Context context) {
            super(context);
        }

        public CameraOrientationEventListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if(orientation > 45 && orientation < 135) {
                setButtonsRotation(270);
            } else if (orientation > 135 && orientation < 225) {
                setButtonsRotation(180);
            } else if (orientation > 225 && orientation < 315) {
                setButtonsRotation(90);
            } else { //asumed if (orientation > 315 && orientation < 45) {
                setButtonsRotation(0);
            }
        }
    };
}
