package d2d.testing.net.threads.workers;

import android.util.Log;

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

            // Return to sender
            Logger.d("ServerWorker received: " +  new String(dataEvent.getData()));
            dataEvent.getSelector().getMainActivity().updateMsg(new String(dataEvent.getData()));

            //echo to everyone
            dataEvent.getSelector().send(dataEvent.getData());
        }
    }
}