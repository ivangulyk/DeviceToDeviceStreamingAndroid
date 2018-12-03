package d2d.testing.net.events;

import java.nio.channels.SocketChannel;

import d2d.testing.net.threads.ClientThread;
import d2d.testing.net.threads.SelectorInterface;
import d2d.testing.net.threads.ServerThread;

public class DataEvent {
    public final int TYPE_TEXT_MESSAGE = 101;
    public final int TYPE_TEXT_COMMANDX = 102;

    public ServerThread server;
    public ClientThread client;
    public SelectorInterface selectorInterface;
    public SocketChannel socket;
    public byte[] data;
    public int type;
    //TODO selector interfaz yt usamos lo mismos data events para server que para cliente?
    // realmente ya tenemos el socket y los datos con eso podemos mapear
    public DataEvent(SelectorInterface selectorInterface, SocketChannel socket, byte[] data) {
        this.selectorInterface = selectorInterface;
        this.socket = socket;
        this.data = data;
    }

    public DataEvent(ServerThread server, SocketChannel socket, byte[] data) {
        this.server = server;
        this.socket = socket;
        this.data = data;
    }

    public DataEvent(ClientThread client, SocketChannel socket, byte[] data) {
        this.client = client;
        this.socket = socket;
        this.data = data;
    }

    //CLASE PARA LOS DOS -- COMPARTIR MISMOS WORKERS?
    //se necesita

}
