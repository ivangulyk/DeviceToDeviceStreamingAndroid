package d2d.testing.net.threads.workers;

import android.os.Environment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import d2d.testing.helpers.Logger;
import d2d.testing.net.events.DataEvent;
import d2d.testing.net.threads.selectors.NioSelectorThread;

public class ServerWorker implements WorkerInterface {
    private final List queue = new LinkedList();
    private boolean mEnabled = true;

    @Override
    public void addData(NioSelectorThread selector, SocketChannel socket, byte[] data, int count) {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        synchronized(queue) {
            queue.add(new DataEvent(selector, socket, dataCopy));
            queue.notify();
        }
    }

    @Override
    public void run() {
        DataEvent dataEvent;
        final File f = new File(Environment.getExternalStorageDirectory() + "/"
                + "/wifip2pshared-" + System.currentTimeMillis()
                + ".jpg");
        while(mEnabled) {
            // Wait for data to become available
            synchronized(queue) {
                while(queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                dataEvent = (DataEvent) queue.remove(0);
            }
            File dirs = new File(f.getParent());
            if (!dirs.exists())
                dirs.mkdirs();
            try {
                f.createNewFile();
            } catch (IOException e) {
                Logger.e(e.getMessage());
            }
            Logger.d("copying files " + f.toString());
            try {
                copyFile(dataEvent.getData(), new FileOutputStream(f));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            /*
            // Return to sender
            Logger.d("ServerWorker received: " +  new String(dataEvent.getData()));
            dataEvent.getSelector().getMainActivity().updateMsg(new String(dataEvent.getData()));

            //echo to everyone
            dataEvent.getSelector().send(dataEvent.getData());
            */
        }
    }

    private boolean copyFile(byte[] file, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            int i = 0;
            while (i < file.length) {
                out.write(buf, 0, file[i]);
                i++;
            }
            out.close();
        } catch (IOException e) {
            Logger.d(e.toString());
            return false;
        }
        return true;
    }
}