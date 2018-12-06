package d2d.testing.net.threads.selectors;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import d2d.testing.MainActivity;
import d2d.testing.net.events.ChangeRequest;
import d2d.testing.net.threads.workers.ClientWorker;

public class ClientSelector extends NioSelectorThread{
    protected SocketChannel mSocket;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ClientSelector(InetAddress address, MainActivity mainActivity) throws IOException {
        super(mainActivity,address);

        this.mWorker = new ClientWorker();
        new Thread(mWorker).start();
    }


    protected void initiateConnection() {
        try {
            this.mSocket = SocketChannel.open();

            this.mSocket.configureBlocking(false);             // Create a non-blocking socket channel
            this.mSocket.connect(new InetSocketAddress(mInetAddress.getHostAddress(),PORT));  // Connection establishment

            this.mStatus = STATUS_CONNECTING;
            this.addChangeRequest(new ChangeRequest(mSocket, ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(byte[] data) {
        this.send(mSocket, data);
    }
}

