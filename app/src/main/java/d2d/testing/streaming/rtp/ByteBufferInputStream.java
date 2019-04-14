package d2d.testing.streaming.rtp;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ByteBufferInputStream extends BufferInfoInputStream {

    private static final String TAG = "ByteBufferInputStream";

    LinkedList<ByteBufferInfo> mByteBufferInfos = new LinkedList<>();
    Map<ByteBuffer, MediaCodec.BufferInfo> mByteBuffersMap = new HashMap<>();
    ByteBuffer mByteBuffer = null;
    private boolean mClosed = false;

    public int read() throws IOException {
        return 0;
    }

    public int read(byte[] bytes, int off, int len) throws IOException {
        int min = 0;

        try {
            while (!Thread.interrupted() && !mClosed && mByteBufferInfos.isEmpty() && mByteBuffer != null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (mClosed)
                throw new IOException("This InputStream was closed");

            synchronized (mByteBufferInfos) {
                if(mByteBuffer != null) {
                    Log.v(TAG,"byteBuffer not null..!");
                    if(mByteBuffer.hasRemaining()) {
                        Log.v(TAG,"byteBuffer has remaining... reading...!");
                        min = Math.min(len, mByteBuffer.remaining());
                        mByteBuffer.get(bytes, off, min);
                    }
                    if(!mByteBuffer.hasRemaining()) {
                        Log.v(TAG,"byteBuffer doesnt have remaining... nullifying...!");
                        mByteBuffer = null;
                    }
                } else if(!mByteBufferInfos.isEmpty()) {
                    Log.v(TAG,"mByteBufferInfos not empty..!");

                    ByteBufferInfo byteBufferInfo = mByteBufferInfos.removeFirst();
                    mByteBuffer = byteBufferInfo.getByteBuffer();
                    mBufferInfo = byteBufferInfo.getBufferInfo();
                    if(mByteBuffer.hasRemaining()) {
                        Log.v(TAG,"byteBuffer has remaining... reading...!");
                        min = Math.min(len, mByteBuffer.remaining());
                        mByteBuffer.get(bytes, off, min);
                    }
                    if(!mByteBuffer.hasRemaining()) {
                        Log.v(TAG,"byteBuffer doesnt have remaining... nullifying...!");
                        mByteBuffer = null;
                    }
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return min;
    }

    public int available() {
        return mByteBuffer.remaining();
    }

    public void addBufferInput(byte[] buffer, long presentationTime) {
        synchronized (mByteBufferInfos) {
            mByteBufferInfos.add(new ByteBufferInfo(ByteBuffer.wrap(buffer), presentationTime));

            //mByteBuffersMap.put(byteBuffer, bufferInfo);
            //mByteBuffer = byteBuffer;
            //mBufferInfo = bufferInfo;
        }
    }

    @Override
    public MediaCodec.BufferInfo getLastBufferInfo() {
        synchronized (mByteBufferInfos) {
            return mBufferInfo;
        }
    }

    public class ByteBufferInfo {
        @SuppressLint("NewApi")
        MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer mByteBuffer;

        @SuppressLint("NewApi")
        public ByteBufferInfo(ByteBuffer mByteBuffer, long presentationTime) {
            this.mBufferInfo.presentationTimeUs = presentationTime;
            this.mByteBuffer = mByteBuffer;
        }

        public MediaCodec.BufferInfo getBufferInfo() {
            return mBufferInfo;
        }

        public ByteBuffer getByteBuffer() {
            return mByteBuffer;
        }
    }
}