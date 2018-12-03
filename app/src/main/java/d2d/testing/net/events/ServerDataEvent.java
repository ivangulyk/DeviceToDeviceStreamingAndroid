package d2d.testing.net.events;

import java.nio.channels.SocketChannel;

import d2d.testing.net.threads.selectors.ServerSelectorThread;

public class ServerDataEvent {
    public ServerSelectorThread server;
    public SocketChannel socket;
    public byte[] data;

    public ServerDataEvent(ServerSelectorThread server, SocketChannel socket, byte[] data) {
        this.server = server;
        this.socket = socket;
        this.data = data;
    }
}