package d2d.testing.net.threads.workers;

import java.nio.channels.SelectableChannel;

import d2d.testing.utils.IOUtils;
import d2d.testing.utils.Logger;
import d2d.testing.net.utils.FileHandler;
import d2d.testing.net.packets.DataPacket;
import d2d.testing.net.threads.selectors.AbstractSelector;

//TODO Externalizar constantes... SSL??
//TODO COMPARTIR MISMOS WORKERS? Ya veremos
public class ClientWorker extends AbstractWorker {


    @Override
    protected void processData(DataPacket dataPacket, AbstractSelector selector, SelectableChannel channel) {
        String ip = "", name = "";
        byte[] bodyData;
        int ipLen, nameLen;

        switch (dataPacket.getType())
        {
            case DataPacket.TYPE_MSG:
                selector.getMainActivity().updateMsg(new String(dataPacket.getBodyData()));
                Logger.d("ClientWorker received TYPE_MSG command");
                break;
            case DataPacket.STREAM_ON:
                bodyData = dataPacket.getBodyData();

                ipLen = IOUtils.fromByteArray(bodyData);
                nameLen = IOUtils.fromByteArray(IOUtils.copyMax(bodyData, 4+ipLen, 4));

                ip = ip.concat(new String(IOUtils.copyMax(bodyData, 4, ipLen)));
                name = name.concat(new String(IOUtils.copyMax(bodyData, 8 + ipLen, nameLen)));

                selector.getMainActivity().updateStreamList(true, ip,name);
                Logger.d("ClientWorker received STREAM_ON command");
                break;

            case DataPacket.STREAM_OFF:
                bodyData = dataPacket.getBodyData();

                ipLen = IOUtils.fromByteArray(bodyData);
                nameLen = IOUtils.fromByteArray(IOUtils.copyMax(bodyData, 4+ipLen, 4));

                ip = ip.concat(new String(IOUtils.copyMax(bodyData, 4, ipLen)));
                name = name.concat(new String(IOUtils.copyMax(bodyData, 8 + ipLen, nameLen)));

                selector.getMainActivity().updateStreamList(false, ip, name);
                Logger.d("ClientWorker received STREAM_OFF command");
                break;
            case DataPacket.TYPE_FILE:
                selector.getMainActivity().getWiFiP2pPermissions().memory();
                if(selector.getMainActivity().get_storage_has_perm()) {
                    FileHandler.handle(dataPacket);
                } else {
                    //todo perdemos el archivo si no tenemos permisos
                    Logger.d("ClientWorker received TYPE_FILE command but no permission we are losing the file fix");
                }
                break;

            default:
                Logger.e("ClientWorker received no TYPE_FILE");
                //ERROR NO HAY TIPO DE MENSAJE!!
        }
    }
}