package d2d.testing.net.events;

import java.nio.channels.SocketChannel;

import d2d.testing.net.threads.selectors.NioSelectorThread;

public class DataEvent {
    private final NioSelectorThread selector;
    private final SocketChannel socket;

    private final byte[] data;

    public DataEvent(NioSelectorThread selector, SocketChannel socket, byte[] data) {
        this.selector = selector;
        this.socket = socket;
        this.data = data;
    }

    public NioSelectorThread getSelector() {
        return selector;
    }

    public SocketChannel getSocket() {
        return socket;
    }

    public byte[] getData() {
        return data;
    }
}
