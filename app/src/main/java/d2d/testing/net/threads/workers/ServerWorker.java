package d2d.testing.net.threads.workers;

import android.util.Log;

import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import d2d.testing.net.events.ServerDataEvent;
import d2d.testing.net.threads.selectors.ServerSelectorThread;

public class ServerWorker implements Runnable {
    private List queue = new LinkedList();

    public void addData(ServerSelectorThread server, SocketChannel socket, byte[] data, int count) {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        synchronized(queue) {
            queue.add(new ServerDataEvent(server, socket, dataCopy));
            queue.notify();
        }
    }

    public void run() {
        ServerDataEvent dataEvent;

        while(true) {
            // Wait for data to become available
            synchronized(queue) {
                while(queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                    }
                }
                dataEvent = (ServerDataEvent) queue.remove(0);
            }

            // Return to sender
            Log.d("ServerWorker","ServerWorker received: " + dataEvent.data);
            dataEvent.server.send(dataEvent.socket, dataEvent.data);
        }
    }
}