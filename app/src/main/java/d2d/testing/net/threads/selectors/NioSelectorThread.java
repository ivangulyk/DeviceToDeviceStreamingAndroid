package d2d.testing.net.threads.selectors;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import d2d.testing.net.events.ChangeRequest;
import d2d.testing.net.threads.workers.ServerWorker;

public abstract class NioSelectorThread implements Runnable{
    //TODO
    protected static final int PORT = 3462;

    private boolean listening = true;

    protected ServerSocketChannel mServerSocket;
    protected Selector mSelector;
    protected ServerWorker mWorker; //TODO convert to array??

    // A list of ChangeRequest instances and Data/socket map
    private final List mConnections = new ArrayList<SocketChannel>();
    protected final List mPendingChangeRequests = new LinkedList();
    protected final Map mPendingData = new HashMap();
    private ByteBuffer mReadBuffer = ByteBuffer.allocate(8192);

    protected Selector initSelector() {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public NioSelectorThread() {
        this.mWorker = new ServerWorker();
        new Thread(mWorker).start();
        mSelector = this.initSelector();
    }

    public void stop(){
        this.listening = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void run(){
        try {
            while(listening)
            {
                this.processChangeRequests();

                //Log.d("ServerSelector","ServerSelector: i'm a server and i'm waiting for selection keys...");
                mSelector.select();

                Iterator<SelectionKey> itKeys = mSelector.selectedKeys().iterator();
                while (itKeys.hasNext()) {
                    SelectionKey myKey = itKeys.next();
                    itKeys.remove();

                    if (!myKey.isValid()) {
                        continue;
                    }

                    if (myKey.isAcceptable()) {
                        this.accept(myKey);
                    } else if (myKey.isReadable()) {
                        this.read(myKey);
                    } else if (myKey.isWritable()) {
                        this.write(myKey);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (SelectionKey key : mSelector.keys()) {
            try {
                key.channel().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            key.cancel();
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        // Accept the connection and make it non-blocking
        SocketChannel socketChannel = serverSocketChannel.accept();
        Socket socket = socketChannel.socket();
        socketChannel.configureBlocking(false);

        // Register the SocketChannel with our Selector, indicating to be notified for READING
        socketChannel.register(this.mSelector, SelectionKey.OP_READ);
        mConnections.add(socketChannel);

        Log.d("SERVER","Connection Accepted: " + socket.getLocalAddress() + "\n");
    }

    private void read(SelectionKey key) throws IOException {
        int numRead;
        SocketChannel socketChannel = (SocketChannel) key.channel();
        mReadBuffer.clear();   //Clear out our read buffer

        try {
            numRead = socketChannel.read(mReadBuffer); // Attempt to read off the channel
        } catch (IOException e) {
            key.cancel();       // Forced to close the connection, cancel key and close the channel.
            socketChannel.close();
            mConnections.remove(socketChannel);
            return;
        }

        if (numRead == -1) {
            key.cancel();       // Remote entity shut the socket down cleanly. Do the same
            socketChannel.close();
            mConnections.remove(socketChannel);
            Log.d("ServerSelector","ServerSelector: client closed connection...");
            return;
        }

        // Hand the data off to our worker thread
        this.mWorker.addData(this, socketChannel, mReadBuffer.array(), numRead);
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        synchronized (mPendingData) {
            List queue = (List) mPendingData.get(socketChannel);

            while (!queue.isEmpty()) {                  // Write until there's not more data ...
                ByteBuffer buf = (ByteBuffer) queue.get(0);
                socketChannel.write(buf);
                if (buf.remaining() > 0) {              // ... or the socket's buffer fills up
                    break;
                }
                queue.remove(0);
            }

            if (queue.isEmpty()) {
                key.interestOps(SelectionKey.OP_READ);  // We wrote away all data, switch back to READING
            }
        }
    }

    public void send(SocketChannel socket, byte[] data) {
        synchronized(mPendingChangeRequests) {
            this.mPendingChangeRequests.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
            synchronized (mPendingData) {  // And queue the data we want written
                List queue = (List) mPendingData.get(socket);
                if (queue == null) {
                    queue = new ArrayList();
                    mPendingData.put(socket, queue);
                }
                queue.add(ByteBuffer.wrap(data));
            }
        }

        // Finally, wake up our selecting thread so it can make the required changes
        this.mSelector.wakeup();
    }

    public void send(byte[] data) {
    }

    private void processChangeRequests() throws Exception {
        // Process any pending changes
        synchronized (mPendingChangeRequests) {
            Iterator changes = mPendingChangeRequests.iterator();
            while (changes.hasNext()) {
                ChangeRequest change = (ChangeRequest) changes.next();
                switch (change.type) {
                    case ChangeRequest.CHANGEOPS:
                        SelectionKey key = change.socket.keyFor(mSelector);
                        key.interestOps(change.ops);
                        break;
                    case ChangeRequest.REGISTER:
                        change.socket.register(mSelector, change.ops);
                        break;
                }
            }
            this.mPendingChangeRequests.clear();
        }
    }
}
