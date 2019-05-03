package d2d.testing.net.threads.workers;

import java.nio.channels.SelectableChannel;

import d2d.testing.helpers.Logger;
import d2d.testing.net.handlers.FileHandler;
import d2d.testing.net.handlers.StreamHandler;
import d2d.testing.net.packets.DataPacket;
import d2d.testing.net.threads.selectors.AbstractSelector;

//TODO Externalizar constantes... SSL??
//TODO COMPARTIR MISMOS WORKERS? Ya veremos
public class ClientWorker extends AbstractWorker {

    private StreamHandler mStream = null;

    @Override
    protected void processData(DataPacket dataPacket, AbstractSelector selector, SelectableChannel channel) {
        switch (dataPacket.getType())
        {
            case DataPacket.TYPE_MSG:
                selector.getMainActivity().updateMsg(new String(dataPacket.getBodyData()));
                Logger.d("ClientWorker received TYPE_MSG command");
                break;
            case DataPacket.STREAM_ON:
                selector.getMainActivity().updateStreamList(true, new String(dataPacket.getBodyData()));
                Logger.d("ClientWorker received STREAM_ON command");
                break;

            case DataPacket.STREAM_OFF:
                selector.getMainActivity().updateStreamList(false, new String(dataPacket.getBodyData()));
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

            case DataPacket.TYPE_VIDEO_STREAM:
                Logger.d("ClientWorker received TYPE_VIDEO_STREAM");
                if(mStream == null)
                {
                    mStream = new StreamHandler();
                }

                mStream.handle(dataPacket);
                break;

            default:
                Logger.e("ClientWorker received no TYPE_FILE");
                //ERROR NO HAY TIPO DE MENSAJE!!
        }
    }
}