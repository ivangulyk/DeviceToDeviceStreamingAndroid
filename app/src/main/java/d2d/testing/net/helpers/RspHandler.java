package d2d.testing.net.helpers;

import android.util.Log;

public class RspHandler {
    private byte[] rsp = null;

    public synchronized boolean handleResponse(byte[] rsp) {
        this.rsp = rsp;
        this.notify();
        return true;
    }

    public synchronized void waitForResponse() {
        while(this.rsp == null) {
            try {
                this.wait();
            } catch (InterruptedException e) {
            }
        }

        Log.d("CLIENT",new String(this.rsp));
        System.out.println(new String(this.rsp));
    }
}
