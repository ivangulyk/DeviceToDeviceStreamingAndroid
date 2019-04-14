package d2d.testing.net.threads.selectors;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import d2d.testing.MainActivity;
import d2d.testing.helpers.Logger;
import d2d.testing.net.threads.workers.ServerWorker;

public class UDPServerSelector extends AbstractSelector {
    private DatagramChannel mDatagramChannel;
    private int mPortUDP;
    private int mStatusUDP;

    public UDPServerSelector(MainActivity mainActivity, int port) throws IOException {
        super(mainActivity);
        mPortUDP = port;
        mWorker = new ServerWorker();
        mWorker.start();
    }

    @Override
    protected void onClientDisconnected(SelectableChannel socketChannel) {

    }

    @Override
    protected void initiateConnection() {
        try {
            mDatagramChannel = (DatagramChannel) DatagramChannel.open().configureBlocking(false);
            mDatagramChannel.socket().bind(new InetSocketAddress(PORT_UDP));

            mStatusUDP = STATUS_LISTENING;
            this.addChangeRequest(new ChangeRequest(mDatagramChannel, ChangeRequest.REGISTER, SelectionKey.OP_READ));
            Logger.d("ClientSelector: initiateConnection as server listening UDP on port " + InetAddress.getLocalHost().getHostAddress() + ":" + PORT_UDP);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addConnectionUDP(InetAddress address, int port){
        try {
            DatagramChannel datagramChannel =  (DatagramChannel) DatagramChannel.open().configureBlocking(false);
            datagramChannel.connect(new InetSocketAddress(address.getHostAddress(), port));

            mConnections.add(datagramChannel);
            Logger.d("ClientSelector: initiateConnection UDP client 'connected' to " + address.getHostAddress() + ":" + port);
        } catch (IOException e) {
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