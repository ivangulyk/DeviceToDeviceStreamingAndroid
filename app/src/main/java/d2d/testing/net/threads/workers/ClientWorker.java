package d2d.testing.net.threads.workers;

import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import d2d.testing.helpers.Logger;
import d2d.testing.net.handlers.FileHandler;
import d2d.testing.net.packets.DataReceived;
import d2d.testing.net.packets.DataFormatter;
import d2d.testing.net.packets.DataPacket;
import d2d.testing.net.threads.selectors.NioSelectorThread;

public class ClientWorker implements Runnable, WorkerInterface {
    //TODO Externalizar constantes... SSL??
    //TODO COMPARTIR MISMOS WORKERS? Ya veremos

    private final List queue = new LinkedList();

    private DataPacket openPacket;

    private boolean mEnabled = true;

    @Override
    public void addData(NioSelectorThread selectorThread, SocketChannel socket, byte[] data, int count) {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        synchronized(queue) {
            queue.add(new DataReceived(selectorThread, socket, dataCopy));
            queue.notify();
        }
    }

    @Override
    public void run() {
        DataReceived dataReceived;

        while(mEnabled) {
            // Wait for data to become available
            synchronized(queue) {
                while(queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                dataReceived = (DataReceived) queue.remove(0);
            }

            this.processData(dataReceived);
        }
    }

    private void processData(DataReceived dataReceived)
    {
        List<DataPacket> openPackets = new LinkedList();

        Logger.d("ClientWorker received: " +  dataReceived.getData().length + " bytes");//new String(dataReceived.getData()));
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
                    dataReceived.getSelector().getMainActivity().updateMsg(new String(packet.getBodyData()));
                    Logger.d("ClientWorker received TYPE_MSG command");
                    break;

                case DataFormatter.TYPE_FILE:
                    dataReceived.getSelector().getMainActivity().getWiFiP2pPermissions().memory();
                    if(dataReceived.getSelector().getMainActivity().get_storage_has_perm()) {
                        new FileHandler().handle(packet);
                    } else {
                        Logger.d("ClientWorker received TYPE_FILE command but no permission");
                    }
                    break;
                default:
                    //ERROR NO HAY TIPO DE MENSAJE!!
            }
        }
    }
}