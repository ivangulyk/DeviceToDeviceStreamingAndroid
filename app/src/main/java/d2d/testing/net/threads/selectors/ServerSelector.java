package d2d.testing.net.threads.selectors;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.List;

import d2d.testing.MainActivity;
import d2d.testing.net.events.ChangeRequest;
import d2d.testing.net.threads.workers.ServerWorker;

public class ServerSelector extends NioSelectorThread  {

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ServerSelector(MainActivity mainActivity) {
        super(mainActivity);

        this.mWorker        = new ServerWorker();
        new Thread(mWorker).start();
    }
    protected Selector initSelector() {
        try {
            Selector socketSelector = SelectorProvider.provider().openSelector();   // Create a new selector
            this.mServerSocket = ServerSocketChannel.open();                        // Create a new non-blocking server socket channel
            mServerSocket.configureBlocking(false);
            mServerSocket.socket().bind(new InetSocketAddress(this.PORT));               // Bind the server socket

                mServerSocket.register(socketSelector, SelectionKey.OP_ACCEPT);         // Register the server socket channel

            return socketSelector;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void send(byte[] data) {
        synchronized (this.mPendingChangeRequests) {
            synchronized (this.mPendingData) {  // And queue the data we want written
                for (SelectionKey key : this.mSelector.keys()) {
                    SocketChannel socket = (SocketChannel) key.channel();

                    this.mPendingChangeRequests.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
                    List queue = (List) this.mPendingData.get(socket);
                    if (queue == null) {
                        queue = new ArrayList();
                        this.mPendingData.put(socket, queue);
                    }
                    queue.add(ByteBuffer.wrap(data));
                }
            }
        }
        // Finally, wake up our selecting thread so it can make the required changes
        this.mSelector.wakeup();
    }
}
