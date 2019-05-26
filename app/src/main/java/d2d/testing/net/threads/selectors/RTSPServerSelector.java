package d2d.testing.net.threads.selectors;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

import d2d.testing.utils.Logger;
import d2d.testing.net.threads.workers.RTSPServerWorker;

public class RTSPServerSelector extends ServerSelector {
    private ServerSocketChannel mServerSocketChannel;

    public RTSPServerSelector(int port) throws IOException {
        super(null);

        mPortTCP = port;
        mWorker = new RTSPServerWorker();
        mWorker.start();
    }

    @Override
    protected void initiateConnection() {
        try {
            mServerSocketChannel = (ServerSocketChannel) ServerSocketChannel.open().configureBlocking(false);
            mServerSocketChannel.socket().bind(new InetSocketAddress(mPortTCP));
            // Create a new non-blocking server socket channel and start listening
            mStatusTCP = STATUS_LISTENING;
            addChangeRequest(new ChangeRequest(mServerSocketChannel, ChangeRequest.REGISTER, SelectionKey.OP_ACCEPT));
            Logger.d("RTSPServerSelector: initiateConnection(): server listening TCP connections on port " + mPortTCP);
        } catch (IOException e) {
            Logger.d("RTSPServerSelector: initiateConnection(): failed to listen at port " + mPortTCP);
            e.printStackTrace();
        }
    }

    @Override
    protected void onClientDisconnected(SelectableChannel channel) {
        ((RTSPServerWorker) mWorker).onClientDisconnected(channel);
    }

    @Override
    public void send(byte[] data) {
        for (SelectableChannel socket : mConnections) {
            this.send(socket,data);
        }
    }

}