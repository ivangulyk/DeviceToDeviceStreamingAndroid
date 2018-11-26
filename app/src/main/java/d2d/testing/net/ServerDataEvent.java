package d2d.testing.net;

import java.nio.channels.SocketChannel;

class ServerDataEvent {
    public ServerThread server;
    public SocketChannel socket;
    public byte[] data;

    public ServerDataEvent(ServerThread server, SocketChannel socket, byte[] data) {
        this.server = server;
        this.socket = socket;
        this.data = data;
    }
}