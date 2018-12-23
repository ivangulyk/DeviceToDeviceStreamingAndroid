package d2d.testing.net.threads.workers;

import java.util.LinkedList;
import java.util.List;

import d2d.testing.helpers.Logger;
import d2d.testing.net.handlers.FileHandler;
import d2d.testing.net.packets.DataFormatter;
import d2d.testing.net.packets.DataPacket;
import d2d.testing.net.packets.DataReceived;

public class ServerWorker extends AbstractWorker {
    private DataPacket openPacket;

    @Override
    protected void processData(DataReceived dataReceived)
    {
        List<DataPacket> openPackets = new LinkedList();

        Logger.d("ServerWorker received: " + dataReceived.getData().length + " bytes");
        openPackets.addAll(DataFormatter.getPackets(openPacket, dataReceived.getData()));
        if(openPacket != null)
            openPacket = null;

        for (DataPacket packet : openPackets) {
            if(packet.isCompleted()){
                openPacket = packet;
                break;
            }

            switch (packet.getType())
            {
                case DataPacket.TYPE_MSG:
                    Logger.d("ServerWorker received TYPE_MSG command");
                    dataReceived.getSelector().getMainActivity().updateMsg(new String(packet.getBodyData()));
                    Logger.d("ServerWorker echoing the MSG");
                    dataReceived.getSelector().send(packet.getData());
                    break;

                case DataPacket.TYPE_FILE:
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