package d2d.testing.net;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ServerThread extends Thread {

    private static final int PORT = 958;

    private boolean enabled = false;

    private InetAddress mInetAddress;
    private InetSocketAddress  mInetSocketAddress;

    private Socket socket;
    private ServerSocketChannel mServerSocket;

    private SelectionKey mSelectKey;
    private Selector mSelector;

    // The buffer into which we'll read data when it's available
    private ByteBuffer mReadBuffer = ByteBuffer.allocate(8192);


    @RequiresApi(api = Build.VERSION_CODES.N)
    public ServerThread(InetAddress address) throws IOException {
        mInetAddress = address;
        mInetSocketAddress = new InetSocketAddress(address.getHostAddress(),PORT);
        mSelector = Selector.open();
        mServerSocket = ServerSocketChannel.open();
        mServerSocket.bind(mInetSocketAddress);
        mServerSocket.configureBlocking(false);
        mSelectKey = mServerSocket.register(mSelector, mServerSocket.validOps(), null);
    }

    public void stopServer(){
        //cerrar conexiones etetetete
        this.enabled = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void run(){
        try {
            while(enabled)
            {
                Log.d("SERVER","i'm a server and i'm waiting for new connection and buffer select...");
                // Selects a set of keys whose corresponding channels are ready for I/O operations
                mSelector.select();

                // token representing the registration of a SelectableChannel with a Selector
                Set<SelectionKey> selectionKeys = mSelector.selectedKeys();
                Iterator<SelectionKey> itKeys= selectionKeys.iterator();

                while (itKeys.hasNext()) {
                    SelectionKey myKey = itKeys.next();
                    itKeys.remove();

                    if (!myKey.isValid()) {
                        continue;
                    }

                    // Tests whether this key's channel is ready to accept a new socket connection
                    if (myKey.isAcceptable()) {
                        this.accept(myKey);
                    } else if (myKey.isReadable()) {

                        SocketChannel crunchifyClient = (SocketChannel) myKey.channel();
                        ByteBuffer crunchifyBuffer = ByteBuffer.allocate(256);
                        crunchifyClient.read(crunchifyBuffer);
                        String result = new String(crunchifyBuffer.array()).trim();

                        Log.d("SERVER","Message received: " + result);

                        if (result.equals("Crunchify")) {
                            crunchifyClient.close();
                            Log.d("SERVER","\nIt's time to close connection as we got last company name 'Crunchify'");
                            Log.d("SERVER","\nServer will keep running. Try running client again to establish new connection");
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void accept(SelectionKey key) throws IOException {
        // For an accept to be pending the channel must be a server socket channel.
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        // Accept the connection and make it non-blocking
        SocketChannel socketChannel = serverSocketChannel.accept();
        Socket socket = socketChannel.socket();
        socketChannel.configureBlocking(false);

        // Register the new SocketChannel with our Selector, indicating
        // we'd like to be notified when there's data waiting to be read
        socketChannel.register(this.mSelector, SelectionKey.OP_READ);
        Log.d("SERVER","Connection Accepted: " + socket.getLocalAddress() + "\n");

        // Tests whether this key's channel is ready for reading
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        // Clear out our read buffer so it's ready for new data
        this.mReadBuffer.clear();

        // Attempt to read off the channel
        int numRead;
        try {
            numRead = socketChannel.read(this.mReadBuffer);
        } catch (IOException e) {
            // The remote forcibly closed the connection, cancel
            // the selection key and close the channel.
            key.cancel();
            socketChannel.close();
            return;
        }

        if (numRead == -1) {
            // Remote entity shut the socket down cleanly. Do the
            // same from our end and cancel the channel.
            key.channel().close();
            key.cancel();
            return;
        }

        // Hand the data off to our worker thread
        //this.worker.processData(this, socketChannel, this.mReadBuffer.array(), numRead);
    }
}
