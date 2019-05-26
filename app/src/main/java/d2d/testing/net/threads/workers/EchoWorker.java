package d2d.testing.net.threads.workers;

import d2d.testing.net.packets.DataReceived;

public class EchoWorker extends AbstractWorker {

    @Override
    protected void parsePackets(DataReceived dataReceived) {
        //hacemos echo
        dataReceived.getSelector().send(dataReceived.getData());
    }
}