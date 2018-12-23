package d2d.testing.net.threads.workers;

import java.nio.channels.SelectableChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import d2d.testing.helpers.Logger;
import d2d.testing.net.handlers.FileHandler;
import d2d.testing.net.packets.DataReceived;
import d2d.testing.net.packets.DataPacket;
import d2d.testing.net.packets.OpenPacket;

//TODO Externalizar constantes... SSL??
//TODO COMPARTIR MISMOS WORKERS? Ya veremos
public class ClientWorker extends AbstractWorker {
    private OpenPacket openPacket;

    @Override
    protected void processData(DataReceived dataReceived)
    {
        List<DataPacket> openPackets = new LinkedList();

        Logger.d("ClientWorker received: " +  dataReceived.getData().length + " bytes");//new String(dataReceived.getData()));
        openPackets.addAll(DataParser.getPackets(openPacket, dataReceived.getData()));
        if(openPacket != null)
            openPacket = null;

        for (DataPacket packet : openPackets) {
            if(!packet.isCompleted()){
                openPacket = packet;
                break;
            }

            switch (packet.getType())
            {
                case DataPacket.TYPE_MSG:
                    dataReceived.getSelector().getMainActivity().updateMsg(new String(packet.getBodyData()));
                    Logger.d("ClientWorker received TYPE_MSG command");
                    break;

                case DataPacket.TYPE_FILE:
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