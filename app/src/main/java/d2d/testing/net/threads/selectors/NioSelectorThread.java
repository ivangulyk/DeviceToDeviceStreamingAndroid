package d2d.testing.net.threads.selectors;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
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

import d2d.testing.MainActivity;
import d2d.testing.net.events.ChangeRequest;
import d2d.testing.net.threads.workers.WorkerInterface;

import static java.lang.Thread.sleep;

public abstract class NioSelectorThread implements Runnable{
    //TODO
    protected static final int PORT = 3462;

    protected static final int STATUS_DISCONNECTED = 0;
    protected static final int STATUS_LISTENING = 1;
    protected static final int STATUS_CONNECTING = 2;
    protected static final int STATUS_CONNECTED = 4;

    private final MainActivity mMainActivity;
    protected InetAddress mInetAddress;

    protected boolean mEnabled = true;
    protected int mStatus = STATUS_DISCONNECTED;

    protected Selector mSelector;
    protected WorkerInterface mWorker; //TODO convert to array??

    // A list of ChangeRequest instances and Data/socket map
    protected final List<SocketChannel> mConnections = new ArrayList<>();
    protected final List<ChangeRequest> mPendingChangeRequests = new LinkedList<>();
    protected final Map<SocketChannel, List> mPendingData = new HashMap<>();
    private ByteBuffer mReadBuffer = ByteBuffer.allocate(8192);

    public abstract void send(byte[] data);
    protected abstract void initiateConnection();

    @RequiresApi(api = Build.VERSION_CODES.N)
    public NioSelectorThread(MainActivity mainActivity) throws IOException {
        this(mainActivity,null);
    }

    public NioSelectorThread(MainActivity mainActivity, InetAddress inetAddress) throws IOException {
        this.mInetAddress   = inetAddress;
        this.mMainActivity  = mainActivity;
        this.mSelector      = SelectorProvider.provider().openSelector();
        this.initiateConnection();
        //WORKER MOVIDO A CLIENT/SELECTOR THREAD.. MAS FLEXIBLE SE PUEDE DEVOLVER AQUI EN UN FUTURO ALOMEJOR

    }

    public MainActivity getMainActivity(){
        return this.mMainActivity;
    }

    public void stop(){
        this.mEnabled = false;
    }

    public void run(){
        try {
            while(mEnabled)
            {
                if(mStatus == STATUS_DISCONNECTED)
                {
                    this.initiateConnection();
                    sleep(5000);
                }

                this.processChangeRequests();

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
                    } else if (myKey.isConnectable()) {
                        this.finishConnection(myKey);
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

    protected void send(SocketChannel socket, byte[] data) {
        synchronized(mPendingChangeRequests) {
            this.mPendingChangeRequests.add(new ChangeRequest(socket, ChangeRequest.CHANGE_OPS, SelectionKey.OP_WRITE));

            synchronized (mPendingData) {  // And queue the data we want written
                List queue = mPendingData.get(socket);
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

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        // Accept the connection and make it non-blocking
        SocketChannel socketChannel = serverSocketChannel.accept();
        Socket socket = socketChannel.socket();
        socketChannel.configureBlocking(false);

        // Register the SocketChannel with our Selector, indicating to be notified for READING
        socketChannel.register(this.mSelector, SelectionKey.OP_READ);
        mConnections.add(socketChannel);
        this.mStatus = this.mStatus | STATUS_CONNECTED;
        Log.d("SERVER","Connection Accepted: " + socket.getLocalAddress() + "\n");
    }

    private void finishConnection(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        try {
            socketChannel.finishConnect(); //Finish connecting.
            //negociar algo sobre la conexion?? donde ??
        } catch (IOException e) {
            System.out.println(e);
            key.cancel();               // Cancel the channel's registration with our selector
            return;
        }
        this.mStatus = STATUS_CONNECTED;
        key.interestOps(SelectionKey.OP_READ);  // Register an interest in reading till send
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
            List queue = mPendingData.get(socketChannel);
            if(queue == null)
            {
                queue = new ArrayList();
                mPendingData.put(socketChannel, queue);
                //TODO error no deberia haber ocurrido lo metemos en logs o algo!
                return;
            }

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

    public void addChangeRequest(ChangeRequest changeRequest) {
        synchronized(this.mPendingChangeRequests) {         // Queue a channel registration
            this.mPendingChangeRequests.add(changeRequest);
        }
    }

    private void processChangeRequests() throws Exception {
        // Process any pending changes
        synchronized (mPendingChangeRequests) {
            for (ChangeRequest changeRequest : mPendingChangeRequests) {
                switch (changeRequest.type) {
                    case ChangeRequest.CHANGE_OPS:
                        SelectionKey key = changeRequest.socket.keyFor(mSelector);
                        key.interestOps(changeRequest.ops);
                        break;
                    case ChangeRequest.REGISTER:
                        changeRequest.socket.register(mSelector, changeRequest.ops);
                        break;
                }
            }
            this.mPendingChangeRequests.clear();
        }
    }
}
