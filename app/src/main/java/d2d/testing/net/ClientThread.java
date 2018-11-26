package d2d.testing.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ClientThread extends Thread {

    private static final int PORT = 958;

    private boolean enabled = false;

    private Socket mSocket;
    private InetAddress mInetAddress;

    public ClientThread(Socket socket) {
        this.mSocket = socket;
    }

    public ClientThread(InetAddress address) throws IOException {
        this.mInetAddress = address;
        this.mSocket = new Socket(mInetAddress, PORT);
    }

    @Override
    public void run(){
        try {
            while (mSocket.isConnected())
            {
                mSocket.getInputStream();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
