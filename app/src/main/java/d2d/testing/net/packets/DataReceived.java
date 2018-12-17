package d2d.testing.net.packets;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

import d2d.testing.net.threads.selectors.NioSelectorThread;

public class DataReceived {
    private final NioSelectorThread mSelector;
    private final SelectableChannel mChannel;
    private final byte[] mData;

    public DataReceived(NioSelectorThread selector, SelectableChannel socket, byte[] data) {
        this.mSelector = selector;
        this.mChannel = socket;
        this.mData = data;
    }

    public NioSelectorThread getSelector() {
        return mSelector;
    }

    public SelectableChannel getSocket() {
        return mChannel;
    }

    public byte[] getData() {
        return mData;
    }
}
