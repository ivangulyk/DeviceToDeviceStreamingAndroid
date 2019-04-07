package d2d.testing.net.threads.selectors;

import java.io.IOException;

import d2d.testing.net.threads.workers.RTSPServerWorker;

public class RTSPServerSelector extends ServerSelector {
    public RTSPServerSelector(int port) throws IOException {
        super(null);

        mPortTCP = port;
        mWorker = new RTSPServerWorker();
        mWorker.start();
    }

    @Override
    public void send(byte[] data) {

    }

}
