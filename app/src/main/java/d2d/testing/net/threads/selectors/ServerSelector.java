package d2d.testing.net.threads.selectors;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import d2d.testing.MainActivity;
import d2d.testing.helpers.Logger;
import d2d.testing.net.threads.workers.ServerWorker;

public class ServerSelector extends AbstractSelector {
    private ServerSocketChannel mServerSocketChannel;

    public ServerSelector(MainActivity mainActivity) throws IOException {
        super(mainActivity);

        mWorker = new ServerWorker();
        mWorker.start();
    }

    @Override
    protected void onClientDisconnected(SocketChannel socketChannel) {

    }

    @Override
    protected void initiateConnection() {
        try {
            mServerSocketChannel = (ServerSocketChannel) ServerSocketChannel.open().configureBlocking(false);
            mServerSocketChannel.socket().bind(new InetSocketAddress(mPortTCP));
            // Create a new non-blocking server socket channel  and start listening
            mStatusTCP = STATUS_LISTENING;
            addChangeRequest(new ChangeRequest(mServerSocketChannel, ChangeRequest.REGISTER, SelectionKey.OP_ACCEPT));
            Logger.d("ServerSelector: initiateConnection(): server listening TCP connections on port " + mPortTCP);
        } catch (IOException e) {
            Logger.d("ServerSelector: initiateConnection(): failed to listen at port " + mPortTCP);
            e.printStackTrace();
        }
    }

    @Override
    public void send(byte[] data) {
        for (SocketChannel socket : mConnections) {
            this.send(socket,data);
        }
    }
}