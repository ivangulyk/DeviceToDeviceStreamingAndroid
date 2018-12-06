package d2d.testing.net.threads.workers;

import android.util.Log;

import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import d2d.testing.net.events.DataEvent;
import d2d.testing.net.threads.selectors.NioSelectorThread;

public class ClientWorker implements Runnable, WorkerInterface {
    //TODO Externalizar constantes... SSL??
    //TODO COMPARTIR MISMOS WORKERS? Ya veremos
    private byte[] PREFIX_CONST = {0x11,0x12,0x11,0x14};
    private byte[] TYPE_MSG = {0x15,0x00};
    private byte[] TYPE_MSG2 = {0x15,0x01};
    private byte[] TYPE_3 = {0x15,0x02};
    private List queue = new LinkedList();

    private List openMessage = new LinkedList();

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
        if(openMessage.isEmpty())
        {
            Log.d("ClientWorker","ClientWorker received: " + new String(dataEvent.data));

            dataEvent.client.getMainActivity().updateMsg(new String(dataEvent.data));

            if(Arrays.equals(Arrays.copyOfRange(dataEvent.data, 0, 3),PREFIX_CONST))            //UN PREFIX DE BYTES PARA DAR COMIENZO AL MENSAJE - OPCIONAL?
            {
                //TODO mandar longitud de mensaje + hash?

                if(Arrays.equals(Arrays.copyOfRange(dataEvent.data, 4, 5),TYPE_MSG))        //CADA TIPO DE MENSAJE QUE PODEMOS ENVIAR
                {
                    Log.d("ClientWorker","ClientWorker received TYPE_MSG command");
                }
                else if(Arrays.equals(Arrays.copyOfRange(dataEvent.data, 4, 5),TYPE_MSG2))
                {
                    Log.d("ClientWorker","ClientWorker received TYPE_MSG2 command");
                }
                else if(Arrays.equals(Arrays.copyOfRange(dataEvent.data, 4, 5),TYPE_3))
                {
                    Log.d("ClientWorker","ClientWorker received TYPE_3 command");
                }
            }
            else
            {
                //TODO err?? no hay colas y no hay prefijo ver que se ha enviado y buscar prefijo mas adelante?
            }
        }
        else
        {
            //TODO implementar cola ha llegado un mensaje pero no esta completo
        }
    }
}