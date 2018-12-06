package d2d.testing.net.threads.workers;

import java.nio.channels.SocketChannel;

import d2d.testing.net.threads.selectors.NioSelectorThread;

public interface WorkerInterface extends Runnable {
    void addData(NioSelectorThread selector, SocketChannel socket, byte[] data, int count);
}
