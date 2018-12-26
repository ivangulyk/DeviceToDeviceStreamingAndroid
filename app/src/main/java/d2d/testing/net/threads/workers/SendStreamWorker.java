package d2d.testing.net.threads.workers;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import d2d.testing.net.packets.DataPacket;

import static java.lang.Thread.sleep;

public class SendStreamWorker implements Runnable {
    private static final int BUFFER_SIZE = 8192;

    private FileDescriptor mReadPipeFD;

    public SendStreamWorker(FileDescriptor fd) {
        mReadPipeFD = fd;
    }


    @Override
    public void run() {

        byte[] buffer = new byte[BUFFER_SIZE];
        int read = 0;

        final FileInputStream reader = new FileInputStream(mReadPipeFD);

        try {
            while (true) {

                if (reader.available()>0) {
                    read = reader.read(buffer);

                    if(read > 0)
                    {
                        //todo leemos los datos hacer algo con ellos
                        DataPacket packet = DataPacket.createStreamPacket(Arrays.copyOfRange(buffer,0,read));


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
