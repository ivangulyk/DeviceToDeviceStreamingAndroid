package d2d.testing.net.events;

import java.nio.channels.SocketChannel;

import d2d.testing.net.threads.ServerThread;

public class ServerDataEvent {
    public ServerThread server;
    public SocketChannel socket;
    public byte[] data;

    public ServerDataEvent(ServerThread server, SocketChannel socket, byte[] data) {
        this.server = server;
        this.socket = socket;
        this.data = data;
    }
}