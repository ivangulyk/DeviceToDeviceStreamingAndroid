package d2d.testing.net.threads.selectors;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import d2d.testing.MainActivity;
import d2d.testing.helpers.Logger;
import d2d.testing.net.events.ChangeRequest;
import d2d.testing.net.threads.workers.ServerWorker;

public class ServerSelector extends NioSelectorThread  {

    private ServerSocketChannel mServerSocket;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ServerSelector(MainActivity mainActivity) throws IOException {
        super(mainActivity);

        this.mWorker = new ServerWorker();
        new Thread(mWorker).start();
    }

    @Override
    protected void initiateConnection() {
        try {
            this.mServerSocket = ServerSocketChannel.open();                        // Create a new non-blocking server socket channel

            this.mServerSocket.configureBlocking(false);
            this.mServerSocket.socket().bind(new InetSocketAddress(PORT));     // Bind the server socket

            this.mStatus = STATUS_LISTENING;
            this.addChangeRequest(new ChangeRequest(mServerSocket, ChangeRequest.REGISTER, SelectionKey.OP_ACCEPT));
            Logger.d("ServerSelector: initiateConnection as server listening on port " + PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(byte[] data) {
        for (SocketChannel socket : this.mConnections) {
            this.send(socket,data);
        }
    }
}
