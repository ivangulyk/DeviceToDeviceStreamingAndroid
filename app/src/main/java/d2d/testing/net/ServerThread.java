package d2d.testing.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {

    private static final int PORT = 958;

    private boolean enabled = false;

    private Socket socket;
    private ServerSocket serverSocket;
    private InetAddress inetAddress;


    public ServerThread(InetAddress address) throws IOException {
        this.inetAddress = address;
        serverSocket = new ServerSocket(PORT);
    }

    @Override
    public void run(){
        try {
            while(enabled)
            {
                Socket client = serverSocket.accept();

                //negociar conexion??

                //comprobar app.. login

                //creamos un cliente con la conexion
                new ClientThread(client);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
