package d2d.testing.streaming.video;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import d2d.testing.streaming.hw.EncoderDebugger;
import d2d.testing.streaming.hw.NV21Convertor;
import d2d.testing.streaming.rtp.AbstractPacketizer;
import d2d.testing.streaming.rtp.ByteBufferInputStream;
import d2d.testing.streaming.rtp.MediaCodecInputStream;
import d2d.testing.streaming.video.VideoQuality;
import d2d.testing.streaming.rtp.MediaCodecBufferReader;

public class VideoPacketizerDispatcher {

    private static String TAG = "VideoPacketizerDispatcher";
    private Thread mReaderThread;
    private static VideoPacketizerDispatcher mInstance;
    private Camera mCamera;

    private final int SAMPLING_RATE = 8000;
    private final VideoQuality mQuality;

    protected SharedPreferences mSettings = null;

    private final MediaCodec mMediaCodec;
    private final MediaCodecInputStream mMediaCodecInputStream;
    private final Map<AbstractPacketizer, InputStream> mPacketizersInputsMap = new HashMap<>();

    @SuppressLint("NewApi")
    private VideoPacketizerDispatcher(Camera camera, SharedPreferences settings, VideoQuality quality) throws IOException {

        this.mCamera = camera;
        this.mSettings = settings;
        this.mQuality = VideoQuality.determineClosestSupportedResolution(mCamera.getParameters(), quality);

        EncoderDebugger debugger = EncoderDebugger.debug(mSettings, mQuality.resX, mQuality.resY);
        final NV21Convertor convertor = debugger.getNV21Convertor();

        mMediaCodec = MediaCodec.createByCodecName(debugger.getEncoderName());
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", mQuality.resX, mQuality.resY);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, mQuality.bitrate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mQuality.framerate);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,debugger.getEncoderColorFormat());
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();

        Camera.PreviewCallback callback = new Camera.PreviewCallback() {
            long now = System.nanoTime()/1000, oldnow = now, i=0;
            ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                oldnow = now;
                now = System.nanoTime()/1000;
                if (i++>3) {
                    i = 0;
                    //Log.d(TAG,"Measured: "+1000000L/(now-oldnow)+" fps.");
                }
                try {
                    int bufferIndex = mMediaCodec.dequeueInputBuffer(500000);
                    if (bufferIndex>=0) {
                        inputBuffers[bufferIndex].clear();
                        if (data == null) Log.e(TAG,"Symptom of the \"Callback buffer was to small\" problem...");
                        else convertor.convert(data, inputBuffers[bufferIndex]);
                        mMediaCodec.queueInputBuffer(bufferIndex, 0, inputBuffers[bufferIndex].position(), now, 0);
                    } else {
                        Log.e(TAG,"No buffer available !");
                    }

                }
                catch(IllegalStateException error){}
                finally {
                    mCamera.addCallbackBuffer(data);
                }
            }
        };

        for (int i=0;i<10;i++) mCamera.addCallbackBuffer(new byte[convertor.getBufferSize()]);
        mCamera.setPreviewCallbackWithBuffer(callback);

        mMediaCodecInputStream = new MediaCodecInputStream(mMediaCodec);
        mReaderThread = new Thread(new MediaCodecBufferReader(64000, mMediaCodecInputStream, mPacketizersInputsMap));
        mReaderThread.start();

        Log.e(TAG, "Constructor finished");
    }

    public static boolean isRunning() {
        return mInstance != null;
    }

    public static VideoPacketizerDispatcher start(Camera camera, SharedPreferences settings, VideoQuality quality) throws IOException {
        if (mInstance == null) {
            mInstance = new VideoPacketizerDispatcher(camera, settings, quality);

            Log.e(TAG, "Thread started!");
        }
        return mInstance;
    }

    @SuppressLint("NewApi")
    public void internalStop() {
        Log.e(TAG,"Stopping dispatcher...");

        if (mReaderThread != null) {
            try {
                mMediaCodecInputStream.close();
            } catch (IOException ignore) {}
            mReaderThread.interrupt();
            try {
                mReaderThread.join();
            } catch (InterruptedException e) {}
            Log.e(TAG, "Reader Thread interrupted!");
            mReaderThread = null;
        }

        Log.e(TAG, "Disabling PreviewCallback!");
        mCamera.setPreviewCallbackWithBuffer(null);

        mMediaCodec.stop();
        mMediaCodec.release();

        mInstance = null;
    }

    public static void subscribe(Camera camera, SharedPreferences settings, AbstractPacketizer packetizer, VideoQuality quality) throws IOException {
        VideoPacketizerDispatcher.start(camera, settings, quality).addInternalPacketizer(packetizer);
    }

    public static void unsubscribe(AbstractPacketizer packetizer) {
        if (mInstance != null) {
            mInstance.removeInternalMediaCodec(packetizer);
        }
    }

    @SuppressLint("NewApi")
    private void addInternalPacketizer(AbstractPacketizer packetizer) {
        InputStream packetizerInput = new ByteBufferInputStream();
        packetizer.setInputStream(packetizerInput);

        mPacketizersInputsMap.put(packetizer, packetizerInput);
        packetizer.start();
        Log.e(TAG, "Added internal packetizer to inputStreamMap!");
    }

    @SuppressLint("NewApi")
    private void removeInternalMediaCodec(AbstractPacketizer packetizer) {
        mPacketizersInputsMap.remove(packetizer);
        Log.e(TAG, "Removed internal media codec from map!");
        if (mPacketizersInputsMap.isEmpty()) {
            Log.e(TAG, "No more elements in map lets finish this!");

            internalStop();
        }
    }
}
