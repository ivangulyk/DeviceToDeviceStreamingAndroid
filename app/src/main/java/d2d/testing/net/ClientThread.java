package d2d.testing.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;

public class ClientThread extends Thread {

    private static final int PORT = 958;

    private boolean enabled = false;

    private Socket mSocket;
    private InetAddress mInetAddress;
    private InetSocketAddress mInetSocketAddress;
    private SocketChannel crunchifyClient;
    private ArrayList pendingChanges;

    public ClientThread(Socket socket) {
        mSocket = socket;
    }

    public ClientThread(InetAddress address) throws IOException {
        mInetAddress = address;
        mInetSocketAddress = new InetSocketAddress(mInetSocketAddress.getAddress(),PORT);
        //mSocket = new Socket(mInetAddress, PORT);
        crunchifyClient = SocketChannel.open(mInetSocketAddress);
    }

    private Selector initSelector() throws IOException {
        // Create a new selector
        return SelectorProvider.provider().openSelector();
    }

    private SocketChannel initiateConnection() throws IOException {
        // Create a non-blocking socket channel
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        // Kick off connection establishment
        //socketChannel.connect(new InetSocketAddress(this.hostAddress, this.port));

        // Queue a channel registration since the caller is not the 
        // selecting thread. As part of the registration we'll register
        // an interest in connection events. These are raised when a channel
        // is ready to complete connection establishment.
        synchronized(this.pendingChanges) {
            this.pendingChanges.add(new ChangeRequest(socketChannel, ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
        }

        return socketChannel;
    }
    
    @Override
    public void run(){
        try {
            while (mSocket.isConnected())
            {
                mSocket.getInputStream();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

