package d2d.testing.net.threads.workers;

import android.util.Log;

import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import d2d.testing.net.events.DataEvent;
import d2d.testing.net.threads.selectors.NioSelectorThread;

public class ServerWorker implements Runnable {
    private List queue = new LinkedList();

    public void addData(NioSelectorThread selector, SocketChannel socket, byte[] data, int count) {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        synchronized(queue) {
            queue.add(new DataEvent(selector, socket, dataCopy));
            queue.notify();
        }
    }

    public void run() {
        DataEvent dataEvent;

        while(true) {
            // Wait for data to become available
            synchronized(queue) {
                while(queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                    }
                }
                dataEvent = (DataEvent) queue.remove(0);
            }

            // Return to sender
            Log.d("ServerWorker","ServerWorker received: " +  new String(dataEvent.data));
            dataEvent.selector.send(dataEvent.socket, dataEvent.data);
        }
    }
}