package d2d.testing.net.threads.workers;

import java.nio.channels.SelectableChannel;

import d2d.testing.utils.IOUtils;
import d2d.testing.utils.Logger;
import d2d.testing.net.utils.FileHandler;
import d2d.testing.net.packets.DataPacket;
import d2d.testing.net.threads.selectors.AbstractSelector;

public class ServerWorker extends AbstractWorker {

    @Override
    protected void processData(DataPacket dataPacket, AbstractSelector selector, SelectableChannel channel) {
        String ip = "", path = "", name = "";

        switch (dataPacket.getType())
        {
            case DataPacket.TYPE_MSG:
                Logger.d("ServerWorker received TYPE_MSG command");
                selector.getMainActivity().updateMsg(new String(dataPacket.getBodyData()));

                Logger.d("ServerWorker echoing the MSG");
                selector.send(dataPacket.getData());
                break;
            case DataPacket.STREAM_ON:
                parseStreamPacket(dataPacket, ip, path, name);

                selector.getMainActivity().updateStreamList(true, ip, name);
                Logger.d("ServerWorker received STREAM_ON command");

                selector.send(dataPacket.getData());
                Logger.d("ServerWorker echoing the STREAM_ON");
                break;

            case DataPacket.STREAM_OFF:
                parseStreamPacket(dataPacket, ip, path, name);

                selector.getMainActivity().updateStreamList(false, ip, name);
                Logger.d("ServerWorker received STREAM_OFF command");

                selector.send(dataPacket.getData());
                Logger.d("ServerWorker echoing the STREAM_OFF");
                break;
            case DataPacket.TYPE_FILE:
                selector.getMainActivity().getWiFiP2pPermissions().memory();
                if(selector.getMainActivity().get_storage_has_perm()) {
                    FileHandler.handle(dataPacket);
                } else {
                    //todo perdemos el archivo si no tenemos permisos
                    //TODO colas en permisos
                    Logger.d("ServerWorker received TYPE_FILE command but no permission we are losing the file fix");
                }
                break;

            default:
                Logger.e("ClientWorker received no TYPE_FILE");
                //ERROR NO HAY TIPO DE MENSAJE!!
        }
    }

    private void parseStreamPacket(DataPacket dataPacket, String ip, String path, String name) {
        byte[] bodyData = dataPacket.getBodyData();

        int ipLen = IOUtils.fromByteArray(bodyData);
        int pathLen = IOUtils.fromByteArray(IOUtils.copyMax(bodyData, 4+ipLen, 4));
        int nameLen = IOUtils.fromByteArray(IOUtils.copyMax(bodyData, 8+ipLen+pathLen, 4));

        ip.concat(new String(IOUtils.copyMax(bodyData, 4, ipLen)));
        path.concat(new String(IOUtils.copyMax(bodyData, 8 + ipLen, pathLen)));
        name.concat(new String(IOUtils.copyMax(bodyData, 12 + ipLen + pathLen, nameLen)));
    }
}