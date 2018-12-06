package d2d.testing.net.events;

import java.nio.channels.spi.AbstractSelectableChannel;

public class ChangeRequest {
    public static final int REGISTER = 1;
    public static final int CHANGE_OPS = 2;

    public AbstractSelectableChannel socket;
    public int type;
    public int ops;

    public ChangeRequest(AbstractSelectableChannel socket, int type, int ops) {
        this.socket = socket;
        this.type = type;
        this.ops = ops;
    }
}
