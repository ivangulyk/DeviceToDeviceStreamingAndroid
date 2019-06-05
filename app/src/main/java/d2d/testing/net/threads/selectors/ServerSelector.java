package d2d.testing.net.threads.selectors;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;

import d2d.testing.MainActivity;
import d2d.testing.gui.StreamDetail;
import d2d.testing.net.packets.DataPacketBuilder;
import d2d.testing.utils.Logger;
import d2d.testing.net.threads.workers.ServerWorker;

public class ServerSelector extends AbstractSelector {
    private ServerSocketChannel mServerSocketChannel;

    public ServerSelector(MainActivity mainActivity) throws IOException {
        super(mainActivity);

        mWorker = new ServerWorker();
        mWorker.start();
    }

    @Override
    protected void onClientDisconnected(SelectableChannel socketChannel) {

    }

    protected void onClientConnected(SelectableChannel socketChannel) {
        ArrayList<StreamDetail> list = getMainActivity().getStreamlist();

        for (StreamDetail item : list) {
            send(socketChannel,
                DataPacketBuilder.buildStreamNotifier(true, item.getIp(), item.getName()).getData()
            );
        }
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
        for (SelectableChannel socket : mConnections) {
            this.send(socket,data);
        }
    }
}