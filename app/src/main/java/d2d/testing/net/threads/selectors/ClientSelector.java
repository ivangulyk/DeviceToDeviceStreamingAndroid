package d2d.testing.net.threads.selectors;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import d2d.testing.MainActivity;
import d2d.testing.helpers.Logger;
import d2d.testing.net.threads.workers.ClientWorker;

public class ClientSelector extends NioSelectorThread{
    private SocketChannel mSocketChannel;


    @RequiresApi(api = Build.VERSION_CODES.N)
    public ClientSelector(InetAddress address, MainActivity mainActivity) throws IOException {
        super(mainActivity,address);

        this.mWorker = new ClientWorker();
        new Thread(mWorker).start();
    }


    protected void initiateConnection() {
        try {
            mSocketChannel = (SocketChannel) SocketChannel.open().configureBlocking(false);
            mSocketChannel.connect(new InetSocketAddress(mInetAddress.getHostAddress(), PORT_TCP));
            // Create a non-blocking socket channel and connect to GroupOwner
            mStatusTCP = STATUS_CONNECTING;
            this.addChangeRequest(new ChangeRequest(mSocketChannel, ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
            Logger.d("ClientSelector: initiateConnection as client trying to connect to " + mInetAddress.getHostAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(byte[] data) {
        this.send(mSocketChannel, data);
    }
}

