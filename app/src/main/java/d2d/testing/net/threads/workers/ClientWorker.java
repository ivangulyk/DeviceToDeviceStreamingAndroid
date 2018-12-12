package d2d.testing.net.threads.workers;

import android.os.Environment;
import android.provider.ContactsContract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import d2d.testing.helpers.Logger;
import d2d.testing.net.events.DataEvent;
import d2d.testing.net.threads.selectors.NioSelectorThread;

public class ClientWorker implements Runnable, WorkerInterface {
    //TODO Externalizar constantes... SSL??
    //TODO COMPARTIR MISMOS WORKERS? Ya veremos
    private final byte[] PREFIX_CONST = {0x11,0x12,0x11,0x14};
    private byte[] TYPE_MSG = {0x15,0x00};
    private byte[] TYPE_MSG2 = {0x15,0x01};
    private byte[] TYPE_3 = {0x15,0x02};
    private final List queue = new LinkedList();

    private List<DataPacket> openPackets = new LinkedList();

    private boolean mEnabled = true;

    @Override
    public void addData(NioSelectorThread selectorThread, SocketChannel socket, byte[] data, int count) {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        synchronized(queue) {
            queue.add(new DataEvent(selectorThread, socket, dataCopy));
            queue.notify();
        }
    }

    @Override
    public void run() {
        DataEvent dataEvent;

        while(mEnabled) {
            // Wait for data to become available
            synchronized(queue) {
                while(queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                dataEvent = (DataEvent) queue.remove(0);
            }

            this.processData(dataEvent);
        }
    }

    private void processData(DataEvent dataEvent)
    {
        if(openPackets.isEmpty())
        {
            Logger.d("ClientWorker received: " + new String(dataEvent.getData()));

            //TODO mandar longitud de mensaje + hash?

            openPackets.addAll(DataFormat.getPackets(dataEvent.getData()));
        }
        else
        {
            DataPacket p = openPackets.remove(0);
            openPackets.addAll(DataFormat.resumePacket(p));
            //TODO implementar cola ha llegado un mensaje pero no esta completo
        }

        for (DataPacket packet : openPackets) {
            if(!packet.isCompleted())
                break;
            switch (packet.getType())
            {
                case DataFormat.TYPE_MSG:
                    dataEvent.getSelector().getMainActivity().updateMsg(new String(dataEvent.getData()));
                    Logger.d("ClientWorker received TYPE_MSG command");
                    break;
                case DataFormat.TYPE_IMAGE:

                    Logger.d("ClientWorker received TYPE_IMAGE command");
                    break;
                case DataFormat.TYPE_FILE:
                    handleFile(packet);

                    Logger.d("ClientWorker received TYPE_FILE command");
                    break;
                default:
                    //ERROR NO HAY TIPO DE MENSAJE!!
            }
        }
    }

    private void handleFile(DataPacket packet){
        final File f = new File(Environment.getExternalStorageDirectory() + "/"
                + "/wifip2pshared-" + System.currentTimeMillis()
                + ".jpg");
        File dirs = new File(f.getParent());
        if (!dirs.exists())
            dirs.mkdirs();
        try {
            f.createNewFile();
        } catch (IOException e) {
            Logger.e(e.getMessage());
        }
        Logger.d("copying files " + f.toString());
        try {
            copyFile(packet.getPacketData(), new FileOutputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean copyFile(byte[] file, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            int i = 0;
            while (i < file.length) {
                out.write(buf, 0, file[i]);
                i++;
            }
            out.close();
        } catch (IOException e) {
            Logger.d(e.toString());
            return false;
        }
        return true;
    }
}