package d2d.testing.net.threads;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import d2d.testing.net.events.ChangeRequest;

public class ServerThread implements Runnable {

    private static final int PORT = 3462;

    private boolean enabled = true;

    private ServerSocketChannel mServerSocket;

    private Selector mSelector;
    private ServerWorker mWorker; //TODO convert to array??

    // A list of ChangeRequest instances and Data/socket map
    private final List mPendingChangeRequests = new LinkedList();
    private final Map mPendingData = new HashMap();

    // The buffer into which we'll read data when it's available
    private ByteBuffer mReadBuffer = ByteBuffer.allocate(8192);


    @RequiresApi(api = Build.VERSION_CODES.N)
    public ServerThread() throws IOException {
        this.mWorker = new ServerWorker();
        new Thread(mWorker).start();
        mSelector = this.initSelector();
    }

    private Selector initSelector() throws IOException {

        Selector socketSelector = SelectorProvider.provider().openSelector();   // Create a new selector
        this.mServerSocket = ServerSocketChannel.open();                        // Create a new non-blocking server socket channel
        mServerSocket.configureBlocking(false);
        mServerSocket.socket().bind(new InetSocketAddress(PORT));               // Bind the server socket
        mServerSocket.register(socketSelector, SelectionKey.OP_ACCEPT);         // Register the server socket channel
        return socketSelector;
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
                this.processChangeRequests();

                Log.d("SERVER","i'm a server and i'm waiting for new connection and buffer select...");
                // Selects a set of keys whose corresponding channels are ready for I/O operations
                mSelector.select();

                // iterator
                Iterator<SelectionKey> itKeys = mSelector.selectedKeys().iterator();
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
                        this.read(myKey);
                    } else if (myKey.isWritable()) {
                        this.write(myKey);
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
        this.mWorker.addData(this, socketChannel, this.mReadBuffer.array(), numRead);
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        synchronized (this.mPendingData) {
            List queue = (List) this.mPendingData.get(socketChannel);

            // Write until there's not more data ...
            while (!queue.isEmpty()) {
                ByteBuffer buf = (ByteBuffer) queue.get(0);
                socketChannel.write(buf);
                if (buf.remaining() > 0) {
                    // ... or the socket's buffer fills up
                    break;
                }
                queue.remove(0);
            }

            if (queue.isEmpty()) {
                // We wrote away all data, so we're no longer interested
                // in writing on this socket. Switch back to waiting for
                // data.
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    public void send(SocketChannel socket, byte[] data) {
        synchronized (this.mPendingChangeRequests) {
            // Indicate we want the interest ops set changed
            this.mPendingChangeRequests.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

            // And queue the data we want written
            synchronized (this.mPendingData) {
                List queue = (List) this.mPendingData.get(socket);
                if (queue == null) {
                    queue = new ArrayList();
                    this.mPendingData.put(socket, queue);
                }
                queue.add(ByteBuffer.wrap(data));
            }
        }

        // Finally, wake up our selecting thread so it can make the required changes
        this.mSelector.wakeup();
    }

    private void processChangeRequests()
    {
        // Process any pending changes
        synchronized(this.mPendingChangeRequests) {
            Iterator changes = this.mPendingChangeRequests.iterator();
            while (changes.hasNext()) {
                ChangeRequest change = (ChangeRequest) changes.next();
                switch(change.type) {
                    case ChangeRequest.CHANGEOPS:
                        SelectionKey key = change.socket.keyFor(this.mSelector);
                        key.interestOps(change.ops);
                }
            }
            this.mPendingChangeRequests.clear();
        }
    }
}
