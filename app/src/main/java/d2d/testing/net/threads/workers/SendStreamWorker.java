package d2d.testing.net.threads.workers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import d2d.testing.helpers.Logger;
import d2d.testing.net.WifiP2pController;
import d2d.testing.net.packets.DataPacket;

import static java.lang.Thread.sleep;

public class SendStreamWorker implements Runnable {
    private static final int BUFFER_SIZE = 8192;

    private InputStream mInputStream;

    private Thread mThread;
    private boolean mEnabled;

    public void start(){
        mThread = new Thread(this);
        mThread.start();
        mEnabled = true;
        Logger.d("SendStreamWorker: starting...");
    }

    public void stop(){
        mEnabled = false;
    }

    public SendStreamWorker(InputStream is) {
        mInputStream = is;
        mEnabled = false;
    }

    @Override
    public void run() {

        byte[] buffer = new byte[BUFFER_SIZE];
        int read;

        try {
            while (mEnabled) {

                if (mInputStream.available()>0) {
                    read = mInputStream.read(buffer);

                    if(read > 0)   //todo leemos los datos hacer algo con ellos
                    {
                        Logger.d("SendStreamWorker: " + read + " bytes read from stream output");
                        DataPacket packet = DataPacket.createStreamPacket(Arrays.copyOfRange(buffer, 0, read));
                        WifiP2pController.getInstance().send(packet);
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
