package d2d.testing.net.threads.selectors;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import d2d.testing.MainActivity;
import d2d.testing.helpers.Logger;
import d2d.testing.net.threads.workers.WorkerInterface;

import static java.lang.Thread.sleep;

public abstract class NioSelectorThread implements Runnable{
    protected static final int PORT_TCP = 3462;
    protected static final int PORT_UDP = 3463;

    protected static final int STATUS_DISCONNECTED = 0;
    protected static final int STATUS_LISTENING = 1;
    protected static final int STATUS_CONNECTING = 2;
    protected static final int STATUS_CONNECTED = 4;

    private final MainActivity mMainActivity;
    private final Selector mSelector;

    // A list of ChangeRequest instances and Data/socket map
    protected final List<SocketChannel> mConnections = new ArrayList<>();
    private final List<ChangeRequest> mPendingChangeRequests = new LinkedList<>();
    private final Map<SocketChannel, List> mPendingData = new HashMap<>();
    private final ByteBuffer mReadBuffer = ByteBuffer.allocate(8192);

    private DatagramChannel mDatagramChannel;
    protected InetAddress mInetAddress;

    private boolean mEnabled = true;
    protected int mStatusTCP = STATUS_DISCONNECTED;
    private int mStatusUDP = STATUS_DISCONNECTED;


    protected WorkerInterface mWorker; //TODO convert to array??

    public abstract void send(byte[] data);
    protected abstract void initiateConnection();

    public NioSelectorThread(MainActivity mainActivity) throws IOException {
        this(mainActivity,null);
    }

    public NioSelectorThread(MainActivity mainActivity, InetAddress inetAddress) throws IOException {
        this.mInetAddress   = inetAddress;
        this.mMainActivity  = mainActivity;
        this.mSelector      = SelectorProvider.provider().openSelector();
        //this.initiateConnectionUDP();
    }

    public MainActivity getMainActivity(){
        return this.mMainActivity;
    }

    public void stop(){
        this.mEnabled = false;
    }

    public void run(){
        try {
            while(mEnabled) {
                this.initiateConnection();

                while (mStatusTCP != STATUS_DISCONNECTED) {
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

                if(mEnabled)
                    sleep(5000); //nos hemos desconectado esperamos y volvemos a intentarlo
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            for (SelectionKey key : mSelector.keys()) {
                try {
                    key.channel().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                key.cancel();
            }
            try {
                mSelector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initiateConnectionUDP(){
        try {
            mDatagramChannel = (DatagramChannel) DatagramChannel.open().configureBlocking(false);
            mDatagramChannel.socket().bind(new InetSocketAddress(PORT_UDP));

            mStatusUDP = STATUS_LISTENING;
            this.addChangeRequest(new ChangeRequest(mDatagramChannel, ChangeRequest.REGISTER, SelectionKey.OP_READ));
            Logger.d("ClientSelector: initiateConnection as server listening UDP on port " + mInetAddress.getLocalHost().getHostAddress() + ":" + PORT_UDP);
        } catch (IOException e) {
            e.printStackTrace();
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
        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();//serverSocketChannel.accept();
        socketChannel.configureBlocking(false);// Accept the connection and make it non-blocking

        // Register the SocketChannel with our Selector, indicating to be notified for READING
        socketChannel.register(mSelector, SelectionKey.OP_READ);
        mConnections.add(socketChannel);
        Logger.d("NioSelectorThread: Connection Accepted from IP " + socketChannel.socket().getRemoteSocketAddress());
    }

    private void finishConnection(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        try {
            if(socketChannel.finishConnect()) { //Finish connecting.
                //todo negociar algo sobre la conexion?? donde ??
                this.mStatusTCP = STATUS_CONNECTED;
                key.interestOps(SelectionKey.OP_READ);  // Register an interest in reading till send
                Logger.d("NioSelectorThread: client (" + socketChannel.socket().getLocalAddress() + ") finished connecting...");
            }
        } catch (IOException e) {
            this.mStatusTCP = STATUS_DISCONNECTED;
            key.cancel();               // Cancel the channel's registration with our selector
            Logger.d("NioSelectorThread finishConnection: " + e.toString());
        }
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
            Logger.d("NioSelectorThread: client closed connection... IP: " + socketChannel.socket().getLocalAddress());
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

    /**
     * Process any pending key changes on our selector
     *
     * It processes mPendingChangeRequests list in a synchronized way
     *
     * @throws Exception
     */

    private void processChangeRequests() throws Exception {
        synchronized (mPendingChangeRequests) {
            for (ChangeRequest changeRequest : mPendingChangeRequests) {
                switch (changeRequest.getType()) {
                    case ChangeRequest.CHANGE_OPS:
                        changeRequest.getChannel().keyFor(mSelector).interestOps(changeRequest.getOps());
                        break;
                    case ChangeRequest.REGISTER:
                        changeRequest.getChannel().register(mSelector, changeRequest.getOps());
                        break;
                }
            }
            this.mPendingChangeRequests.clear();
        }
    }
}
