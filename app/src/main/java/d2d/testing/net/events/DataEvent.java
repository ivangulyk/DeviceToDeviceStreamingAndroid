package d2d.testing.net.events;

import java.nio.channels.SocketChannel;

import d2d.testing.net.threads.selectors.ClientSelector;
import d2d.testing.net.threads.selectors.NioSelectorThread;

public class DataEvent {
    public final int TYPE_TEXT_MESSAGE = 101;
    public final int TYPE_TEXT_COMMANDX = 102;

    public ClientSelector client;
    public NioSelectorThread selector;
    public SocketChannel socket;

    public byte[] data;
    public int type;
    //TODO selector interfaz yt usamos lo mismos data events para server que para cliente?

    public DataEvent(NioSelectorThread selector, SocketChannel socket, byte[] data) {
        this.selector = selector;
        this.socket = socket;
        this.data = data;
    }
}
