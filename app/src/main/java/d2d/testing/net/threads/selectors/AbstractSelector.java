package d2d.testing.net.threads.selectors;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
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
import d2d.testing.helpers.Logger;
import d2d.testing.net.threads.workers.AbstractWorker;

import static java.lang.Thread.sleep;

public abstract class AbstractSelector implements Runnable{
    private static final int BUFFER_SIZE = 8192;

    protected static final int PORT_TCP = 3462;
    protected static final int PORT_UDP = 3463;

    protected static final int STATUS_DISCONNECTED = 0;
    protected static final int STATUS_LISTENING = 1;
    protected static final int STATUS_CONNECTING = 2;
    protected static final int STATUS_CONNECTED = 4;

    private final MainActivity mMainActivity;
    private final Selector mSelector;

    // A list of ChangeRequest instances and Data/socket map
    protected final List<SelectableChannel> mConnections = new ArrayList<>();
    private final List<ChangeRequest> mPendingChangeRequests = new LinkedList<>();
    private final Map<SelectableChannel, List> mPendingData = new HashMap<>();
    private final ByteBuffer mReadBuffer = ByteBuffer.allocate(BUFFER_SIZE);

    protected InetAddress mInetAddress;

    protected int mPortTCP = PORT_TCP;

    private boolean mEnabled = true;
    protected int mStatusTCP = STATUS_DISCONNECTED;
    protected int mStatusUDP = STATUS_DISCONNECTED;


    protected AbstractWorker mWorker; //TODO convert to array??

    public abstract void send(byte[] data);
    protected abstract void initiateConnection();

    public AbstractSelector(MainActivity mainActivity) throws IOException {
        this(mainActivity,null);
    }

    public AbstractSelector(MainActivity mainActivity, InetAddress inetAddress) throws IOException {
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

                while (mStatusTCP != STATUS_DISCONNECTED || mStatusUDP != STATUS_DISCONNECTED) {
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
            //IOUtils.
            try {
                mSelector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(SelectableChannel socket, byte[] data) {
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
        Logger.d("AbstractSelector: Connection Accepted from IP " + socketChannel.socket().getRemoteSocketAddress());
    }

    private void finishConnection(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        try {
            if(socketChannel.finishConnect()) { //Finish connecting.
                //todo negociar algo sobre la conexion?? donde ??
                this.mStatusTCP = STATUS_CONNECTED;
                key.interestOps(SelectionKey.OP_READ);  // Register an interest in reading till send
                Logger.d("AbstractSelector: client (" + socketChannel.socket().getLocalAddress() + ") finished connecting...");
                mMainActivity.setDefaultP2PIp(socketChannel.socket().getLocalAddress().toString().substring(1));
            }
        } catch (IOException e) {
            this.mStatusTCP = STATUS_DISCONNECTED;
            key.cancel();               // Cancel the channel's registration with our selector
            Logger.d("AbstractSelector finishConnection: " + e.toString());
        }
    }

    @SuppressLint("NewApi")
    private void read(SelectionKey key) throws IOException {
        int numRead;
        SelectableChannel socketChannel = key.channel();
        mReadBuffer.clear();   //Clear out our read buffer

        if (socketChannel instanceof SocketChannel ||
           (socketChannel instanceof DatagramChannel && ((DatagramChannel) socketChannel).isConnected())) {
            try {
                numRead = ((ByteChannel) socketChannel).read(mReadBuffer); // Attempt to read off the channel
            } catch (IOException e) {
                disconnectClient(socketChannel);
                return;
            }

            if (numRead == -1) {
                disconnectClient(socketChannel);
                return;
            }

            // Hand the data off to our worker thread
            this.mWorker.addData(this, socketChannel, mReadBuffer.array(), numRead);
        } else if(socketChannel instanceof DatagramChannel) {
            try {
                SocketAddress address = ((DatagramChannel) socketChannel).receive(mReadBuffer); // Attempt to read off the channel

                mReadBuffer.flip();
                Logger.d("Readen from DatagramChannel: " + mReadBuffer.limit() +
                        " bytes from " + ((InetSocketAddress) address).getAddress().getHostAddress() + ":" + ((InetSocketAddress) address).getPort()
                + " on address " + ((InetSocketAddress) ((DatagramChannel) socketChannel).getLocalAddress()).getPort());

                if (mReadBuffer.limit() == -1) {
                    disconnectClient(socketChannel);
                    return;
                } else if (mReadBuffer.limit() == 0) {
                    disconnectClient(socketChannel);
                    return;
                }

                this.mWorker.addData(this, socketChannel, mReadBuffer.array(), mReadBuffer.limit());
            } catch (IOException e) {
                disconnectClient(socketChannel);
                return;
            }
            // Hand the data off to our worker thread
        }
    }

    public void disconnectClient(SelectableChannel socketChannel) {
        try {
            socketChannel.keyFor(mSelector).cancel();       // Remote entity shut the socket down cleanly. Do the same
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        onClientDisconnected(socketChannel);
        mConnections.remove(socketChannel);
        if(socketChannel instanceof SocketChannel) {
            Logger.d("AbstractSelector: client closed connection... IP: " + ((SocketChannel) socketChannel).socket().getLocalAddress());
        } else  if (socketChannel instanceof  DatagramChannel) {
            Logger.d("AbstractSelector: client closed connection... IP: " + ((DatagramChannel) socketChannel).socket().getLocalAddress());
        }
    }

    protected abstract void onClientDisconnected(SelectableChannel socketChannel);

    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        synchronized (mPendingData) {
            List queue = mPendingData.get(socketChannel);
            if(queue == null)
            {
                queue = new ArrayList();
                mPendingData.put(socketChannel, queue);
                Logger.e("AbstractSelector: Tried to write but socket queue was NULL... this should not happen!!");
                return;
            }

            while (!queue.isEmpty()) {                  // Write until there's not more data ...
                ByteBuffer buf = (ByteBuffer) queue.get(0);
                int written = socketChannel.write(buf);
                Logger.e("AbstractSelector: Wrote " + written + " bytes in " + this.getClass());
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
                try {
                    switch (changeRequest.getType()) {
                        case ChangeRequest.CHANGE_OPS:
                            changeRequest.getChannel().keyFor(mSelector).interestOps(changeRequest.getOps());
                            break;
                        case ChangeRequest.REGISTER:
                            changeRequest.getChannel().register(mSelector, changeRequest.getOps());
                            break;
                    }
                } catch (Exception e) {
                    Log.e("AbstractSelector", "Error in process change Request probably client disconnected...");
                }
            }
            this.mPendingChangeRequests.clear();
        }
    }
}
