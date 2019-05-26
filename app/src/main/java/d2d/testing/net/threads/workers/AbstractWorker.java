package d2d.testing.net.threads.workers;

import java.nio.channels.SelectableChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import d2d.testing.utils.Logger;
import d2d.testing.utils.IOUtils;
import d2d.testing.net.packets.DataPacket;
import d2d.testing.net.packets.DataReceived;
import d2d.testing.net.threads.selectors.AbstractSelector;

public abstract class AbstractWorker implements Runnable {
    private final List<DataReceived> mDataReceivedQueue;
    protected final Map<SelectableChannel, DataPacket> mOpenPacketsMap;

    private Thread mThread;
    private boolean mEnabled;

    protected void processData(DataPacket dataPacket, AbstractSelector selector, SelectableChannel channel) {

    }

    protected AbstractWorker() {
        mDataReceivedQueue = new LinkedList<>();
        mOpenPacketsMap = new HashMap<>();
        mEnabled = true;
    }

    public void start(){
        mThread = new Thread(this);
        mThread.start();
        mEnabled = true;
    }

    public void stop(){
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
            this.parsePackets(dataReceived);
        }
    }

    public void addData(AbstractSelector selectorThread, SelectableChannel socket, byte[] data, int count) {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        synchronized(mDataReceivedQueue) {
            mDataReceivedQueue.add(new DataReceived(selectorThread, socket, dataCopy));
            mDataReceivedQueue.notify();
        }
    }


    protected void parsePackets(DataReceived dataReceived) {
        DataPacket openPacket = mOpenPacketsMap.get(dataReceived.getSocket());
        int cont = 0;

        if(openPacket == null)
            openPacket = new DataPacket();

        while(cont < dataReceived.getData().length)
        {
            byte[] packetData = IOUtils.copyMax(dataReceived.getData(), cont, openPacket.getRemainingLength());
            //Logger.d("AbstractWorker written " + packetData.length + " into open packet");
            cont += packetData.length;
            openPacket.addData(packetData);

            openPacket.parsePacket();

            if(openPacket.isCompleted())
            {
                Logger.d("AbstractWorker packet completed");
                this.processData(openPacket, dataReceived.getSelector(), dataReceived.getSocket());     //Process data on child classes
                openPacket = new DataPacket();
            } else if (openPacket.isInvalid()) {
                Logger.d("AbstractWorker packet invalid");
                openPacket = new DataPacket();
            }
        }

        mOpenPacketsMap.put(dataReceived.getSocket(), openPacket);
    }
}
