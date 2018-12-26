package d2d.testing.net.threads.workers;

import android.os.ParcelFileDescriptor;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import d2d.testing.net.WifiP2pHandler;
import d2d.testing.net.packets.DataPacket;

import static java.lang.Thread.sleep;

public class SendStreamWorker implements Runnable {
    private static final int BUFFER_SIZE = 8192;

    private ParcelFileDescriptor mReadPipeFD;
    private WifiP2pHandler mHandler;

    private Thread mThread;
    private boolean mEnabled;

    public void start(){
        mThread = new Thread(this);
        mThread.start();
        mEnabled = true;
    }

    public void stop(){
        mEnabled = false;
    }

    public SendStreamWorker(ParcelFileDescriptor fd, WifiP2pHandler handler) {
        mReadPipeFD = fd;
        mHandler = handler;
        mEnabled = false;
    }

    @Override
    public void run() {

        byte[] buffer = new byte[BUFFER_SIZE];
        int read;

        final FileInputStream reader = new FileInputStream(mReadPipeFD.getFileDescriptor());
        try {
            while (mEnabled) {

                if (reader.available()>0) {
                    read = reader.read(buffer);

                    if(read > 0)
                    {
                        //todo leemos los datos hacer algo con ellos
                        DataPacket packet = DataPacket.createStreamPacket(Arrays.copyOfRange(buffer,0,read));
                        mHandler.mController.send(packet);
                    }
                } else {
                    sleep(10);
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
