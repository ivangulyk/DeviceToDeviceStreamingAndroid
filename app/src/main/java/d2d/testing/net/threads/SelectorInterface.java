package d2d.testing.net.threads;

import java.nio.channels.SocketChannel;

public interface SelectorInterface {
    //TODO
    void send(SocketChannel socket, byte[] data);
}
