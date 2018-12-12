package d2d.testing.net.threads.workers;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import d2d.testing.helpers.Logger;
import d2d.testing.net.packets.DataReceived;
import d2d.testing.net.packets.DataFormat;
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

        Logger.d("ClientWorker received: " + new String(dataReceived.getData()));
        openPackets.addAll(DataFormat.getPackets(openPacket, dataReceived.getData()));
        if(openPacket != null)
            openPacket = null;

        for (DataPacket packet : openPackets) {
            if(!packet.isCompleted()){
                openPacket = packet;
                break;
            }

            switch (packet.getType())
            {
                case DataFormat.TYPE_MSG:
                    dataReceived.getSelector().getMainActivity().updateMsg(new String(packet.getPacketData()));
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
                + "/wifip2pshared-" + System.currentTimeMillis() + ".jpg");
        File dirs = new File(f.getParent());

        if (!dirs.exists() && !dirs.mkdirs()) {
            Logger.e("FileHandler: No se encuentran ni han podido crear lo directorios");
            return;
        }

        try {
            if(!f.createNewFile())
            {
                Logger.e("FileHandler: No se ha podido crear el archivo");
                return;
            }

            Logger.d("FileHandler: copying files " + f.toString());
            copyFile(packet.getPacketData(), new FileOutputStream(f));
            Logger.d("FileHandler: file creadted succesfully");
        }
        catch (FileNotFoundException e) {
            Logger.d(e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Logger.d(e.toString());
            e.printStackTrace();
        }
    }

    private boolean copyFile(byte[] file, OutputStream out) {
        try {
            out.write(file, 0, file.length);
            out.close();
        } catch (IOException e) {
            Logger.d(e.toString());
            return false;
        }
        return true;
    }
}