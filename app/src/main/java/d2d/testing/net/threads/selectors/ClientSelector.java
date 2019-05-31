package d2d.testing.net.threads.selectors;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import d2d.testing.MainActivity;
import d2d.testing.utils.Logger;
import d2d.testing.net.threads.workers.ClientWorker;

public class ClientSelector extends AbstractSelector {
    private SocketChannel mSocketChannel;

    public ClientSelector(InetAddress address, MainActivity mainActivity) throws IOException {
        super(mainActivity,address);

        mWorker = new ClientWorker();
        mWorker.start();
    }

    @Override
    protected void initiateConnection() {
        try {
            mSocketChannel = (SocketChannel) SocketChannel.open().configureBlocking(false);
            mSocketChannel.connect(new InetSocketAddress(mInetAddress.getHostAddress(), mPortTCP));
            // Create a non-blocking socket channel and connect to GroupOwner
            mStatusTCP = STATUS_CONNECTING;
            this.addChangeRequest(new ChangeRequest(mSocketChannel, ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
            Logger.d("ClientSelector: initiateConnection() connecting to " + mInetAddress.getHostAddress() + ":" + PORT_TCP);
        } catch (IOException e) {
            Logger.d("ClientSelector: initiateConnection(): failed to connect to " + mInetAddress.getHostAddress() + ":" + PORT_TCP);
            e.printStackTrace();
        }
    }

    @Override
    protected void onClientDisconnected(SelectableChannel socketChannel) {
        mStatusTCP = STATUS_DISCONNECTED;
    }

    @Override
    public void send(byte[] data) {
        this.send(mSocketChannel, data);
    }
}

