package d2d.testing.streaming.audio;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class AudioDispatcher implements Runnable {

    private static String TAG = "AudioDispatcher";
    private static Thread mThread;
    private static AudioDispatcher mInstance;

    private final int mSamplingRate;

    private final int mBufferSize;
    private final ByteBuffer mInternalBuffer;

    private final AudioRecord mAudioRecord;
    private final Map<MediaCodec, ByteBuffer[]> mMediaCodecsBuffersMap;

    @SuppressLint("NewApi")
    private AudioDispatcher(int samplingRate, MediaCodec mediaCodec) {
        this.mSamplingRate = samplingRate;
        this.mBufferSize = AudioRecord.getMinBufferSize(mSamplingRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)*2;
        this.mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, mSamplingRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, mBufferSize);

        if(this.mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            //errrrrrrrrrrrrrrrrrroooooooooooor
            Log.e(TAG,"An error occurred with the AudioRecord API while initialization!");
        }

        this.mInternalBuffer = ByteBuffer.allocateDirect(mBufferSize);
        this.mMediaCodecsBuffersMap = new HashMap<>();
        this.mMediaCodecsBuffersMap.put(mediaCodec, mediaCodec.getInputBuffers());

        mAudioRecord.startRecording();
        if(mAudioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            Log.e(TAG,"An error occurred with the AudioRecord API while starting recording!");
        }

        Log.e(TAG,"Constructor finished");
    }

    public static boolean isRunning() {
        return mInstance != null;
    }

    public static void start(int samplingRate, MediaCodec mediaCodec) {
        if(mInstance == null) {
            mInstance = new AudioDispatcher(samplingRate, mediaCodec);
            mThread = new Thread(mInstance);
            mThread.start();
            Log.e(TAG,"Thread started!");
        }
    }

    public static void addMediaCodec(MediaCodec mediaCodec) {
        if(mInstance != null) {
            mInstance.addInternalMediaCodec(mediaCodec);
        }
    }

    @SuppressLint("NewApi")
    private void addInternalMediaCodec(MediaCodec mediaCodec) {
        synchronized (mMediaCodecsBuffersMap) {
            mMediaCodecsBuffersMap.put(mediaCodec, mediaCodec.getInputBuffers());
            Log.e(TAG,"Added internal media codec to map!");
        }
    }

    @SuppressLint("NewApi")
    private Map<MediaCodec, Integer> getMediaCodecBufferIndexesMap() {
        Map<MediaCodec, Integer> retMap = new HashMap<>();

        for(MediaCodec codec : mMediaCodecsBuffersMap.keySet()) {
            int bufferIndex = codec.dequeueInputBuffer(10000);
            if (bufferIndex>=0) {
                mMediaCodecsBuffersMap.get(codec)[bufferIndex].clear();
                retMap.put(codec, bufferIndex);
            }
        }

        return retMap;
    }

    @SuppressLint("NewApi")
    @Override
    public void run() {
        int len = 0;

        try {
            while (!Thread.interrupted()) {
                Map<MediaCodec, Integer> mediaCodecBufferIndexMap = getMediaCodecBufferIndexesMap();

                //SI TENEMOS BUFFERS DE UNO O MAS CODECS LEEMOS DE LO CONTRARIO PASAMOS
                if(!mediaCodecBufferIndexMap.isEmpty()) {
                    //Log.e(TAG,"Media Codecs buffers map not empty: " + mediaCodecBufferIndexMap.size());
                    len = mAudioRecord.read(mInternalBuffer, mBufferSize);
                    if (len ==  AudioRecord.ERROR_INVALID_OPERATION || len == AudioRecord.ERROR_BAD_VALUE) {
                        Log.e(TAG,"An error occured with the AudioRecord API !");
                    } else {
                        for(Map.Entry<MediaCodec, Integer> entry : mediaCodecBufferIndexMap.entrySet()) {
                            //copiamos nuestro buffer a los buffer del media codec
                            ByteBuffer codecBuffer = mMediaCodecsBuffersMap.get(entry.getKey())[entry.getValue()];
                            codecBuffer.clear();
                            codecBuffer.put(mInternalBuffer);
                            entry.getKey().queueInputBuffer(entry.getValue(), 0, len, System.nanoTime()/1000, 0);
                            mInternalBuffer.position(0);
                        }
                    }
                    mInternalBuffer.clear();
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}
