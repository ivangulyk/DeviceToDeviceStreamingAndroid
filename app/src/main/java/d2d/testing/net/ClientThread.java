package d2d.testing.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ClientThread extends Thread {

    private static final int PORT = 958;

    private boolean enabled = false;

    private Socket mSocket;
    private InetAddress mInetAddress;
    private InetSocketAddress mInetSocketAddress;
    private SocketChannel crunchifyClient;

    public ClientThread(Socket socket) {
        mSocket = socket;
    }

    public ClientThread(InetAddress address) throws IOException {
        mInetAddress = address;
        mInetSocketAddress = new InetSocketAddress(mInetSocketAddress.getAddress(),PORT);
        //mSocket = new Socket(mInetAddress, PORT);
        crunchifyClient = SocketChannel.open(mInetSocketAddress);
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
