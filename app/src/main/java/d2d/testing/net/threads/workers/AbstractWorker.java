package d2d.testing.net.threads.workers;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import d2d.testing.net.packets.DataReceived;
import d2d.testing.net.threads.selectors.NioSelectorThread;

public abstract class AbstractWorker implements Runnable {
    protected final List<DataReceived> mDataReceivedQueue;
    protected final Map<SelectableChannel, List> mOpenPacketsMap;

    protected boolean mEnabled;

    protected abstract void processData(DataReceived dataReceived);

    protected AbstractWorker() {
        mDataReceivedQueue = new LinkedList<>();
        mOpenPacketsMap = new HashMap<>();
        mEnabled = true;
    }

    @Override
    public void run() {
        DataReceived dataReceived;

        while(mEnabled) {                       // Wait for data to become available
            synchronized(mDataReceivedQueue) {
                while(mDataReceivedQueue.isEmpty()) {
                    try {
                        mDataReceivedQueue.wait();
                    } catch (InterruptedException ignored) {}
                }
                dataReceived = mDataReceivedQueue.remove(0);
            }
            this.processData(dataReceived);     //Process data on child classes
        }
    }

    public void addData(NioSelectorThread selectorThread, SocketChannel socket, byte[] data, int count) {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        synchronized(mDataReceivedQueue) {
            mDataReceivedQueue.add(new DataReceived(selectorThread, socket, dataCopy));
            mDataReceivedQueue.notify();
        }
    }
}
