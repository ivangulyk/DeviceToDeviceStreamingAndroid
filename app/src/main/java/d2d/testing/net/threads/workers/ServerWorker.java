package d2d.testing.net.threads.workers;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import d2d.testing.helpers.Logger;
import d2d.testing.net.handlers.FileHandler;
import d2d.testing.net.packets.DataFormatter;
import d2d.testing.net.packets.DataPacket;
import d2d.testing.net.packets.DataReceived;
import d2d.testing.net.threads.selectors.NioSelectorThread;

public class ServerWorker implements WorkerInterface {
    private final List<DataReceived> mDataReceivedQueue = new LinkedList();
    private final Map<SelectableChannel, List> mOpenPacketsMap = new HashMap<>();
    private boolean mEnabled = true;
    private DataPacket openPacket;

    @Override
    public void addData(NioSelectorThread selector, SocketChannel socket, byte[] data, int count) {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        synchronized(mDataReceivedQueue) {
            mDataReceivedQueue.add(new DataReceived(selector, socket, dataCopy));
            mDataReceivedQueue.notify();
        }
    }

    @Override
    public void run() {
        DataReceived dataReceived;

        while(mEnabled) {    // Wait for data to become available
            synchronized(mDataReceivedQueue) {
                while(mDataReceivedQueue.isEmpty()) {
                    try {
                        mDataReceivedQueue.wait();
                    } catch (InterruptedException ignored) {}
                }
                dataReceived = mDataReceivedQueue.remove(0);
            }
            this.processData(dataReceived);
        }
    }

    private void processData(DataReceived dataReceived)
    {
        List<DataPacket> openPackets = new LinkedList();

        Logger.d("ServerWorker received: " + dataReceived.getData().length + " bytes");
        openPackets.addAll(DataFormatter.getPackets(openPacket, dataReceived.getData()));
        if(openPacket != null)
            openPacket = null;

        for (DataPacket packet : openPackets) {
            if(packet.notCompleted()){
                openPacket = packet;
                break;
            }

            switch (packet.getType())
            {
                case DataFormatter.TYPE_MSG:
                    Logger.d("ServerWorker received TYPE_MSG command");
                    dataReceived.getSelector().getMainActivity().updateMsg(new String(packet.getBodyData()));
                    Logger.d("ServerWorker echoing the MSG");
                    dataReceived.getSelector().send(packet.getData());
                    break;

                case DataFormatter.TYPE_FILE:
                    dataReceived.getSelector().getMainActivity().getWiFiP2pPermissions().memory();
                    if(dataReceived.getSelector().getMainActivity().get_storage_has_perm()) {
                        new FileHandler().handle(packet);
                        Logger.d("ServerWorker received TYPE_FILE command");
                    }
                    break;
                default:
                    //ERROR NO HAY TIPO DE MENSAJE!!
            }
        }
    }
}