package d2d.testing.streaming.rtp;

import android.media.MediaCodec;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class ByteBufferInputStream extends BufferInfoInputStream {

    Map<ByteBuffer, MediaCodec.BufferInfo> mByteBuffersMap = new HashMap<>();
    ByteBuffer mByteBuffer;
    private boolean mClosed = false;

    public ByteBufferInputStream() {
        this.mByteBuffer = ByteBuffer.allocateDirect(0);
    }

    public int read() throws IOException {
        return 0;
    }

    public int read(byte[] bytes, int off, int len) throws IOException {
        int min = 0;

        try {
            while (!Thread.interrupted() && !mClosed && !byteBufferAvailable()) {
                try {
                    this.wait(0,10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (mClosed)
                throw new IOException("This InputStream was closed");

            synchronized (this) {
                if(mByteBuffer.hasRemaining()) {
                    min = Math.min(len, mByteBuffer.remaining());
                    mByteBuffer.get(bytes, off, len);
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

    public void addBufferInput(byte[] buffer) {
        synchronized (this) {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(mByteBuffer.remaining() + buffer.length);
            byteBuffer.put(mByteBuffer);
            byteBuffer.put(buffer);
            //mBufferInfo = bufferInfo;
        }
    }

    private boolean byteBufferAvailable() {
        synchronized (this) {
            return mByteBuffer.remaining() > 0;
        }
    }
}