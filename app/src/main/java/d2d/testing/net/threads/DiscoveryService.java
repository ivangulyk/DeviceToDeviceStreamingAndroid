package d2d.testing.net.threads;

import android.net.wifi.p2p.WifiP2pManager;
import d2d.testing.utils.Logger;
import d2d.testing.net.WifiP2pHandler;

public class DiscoveryService implements Runnable {
    private boolean mEnabled;
    private WifiP2pHandler mHandler;

    public DiscoveryService(WifiP2pHandler handler) {
        mHandler = handler;
        mEnabled = true;
    }

    @Override
    public void run() {
        while(mEnabled) {
            mHandler.getWifiP2pManager().discoverPeers(mHandler.getChannel(), new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    Logger.d("Successful in adding Discovery Request");
                    mHandler.getWifiP2pManager().requestPeers(mHandler.getChannel(),mHandler.getPeerListListener());
                }

                @Override
                public void onFailure(int reasonCode) {
                    Logger.d("Failed in adding Discovery Request "+reasonCode);
                }
            });
            try {
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
