package d2d.testing.net;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.widget.Toast;

import d2d.testing.MainActivity;

/**
 * Created by Koerfer on 16.02.2016.
 */
public class WifiP2pHandler {

    public MainActivity mActivity;
    public WifiP2pManager mWifiP2pManager;
    public WifiP2pManager.Channel mChannel;
    public WifiP2pController mController;

    public WifiP2pManager getWifiP2pManager() {
        return this.mWifiP2pManager;
    }

    public void setWifiP2pManager(WifiP2pManager wifiP2pManager) {
        this.mWifiP2pManager = wifiP2pManager;
    }

    public WifiP2pManager.Channel getChannel() {
        return this.mChannel;
    }

    public void setChannel(WifiP2pManager.Channel channel) {
        this.mChannel = channel;
    }

    public MainActivity getActivity() {

        return this.mActivity;
    }

    public void setActivity(MainActivity activity) {
        this.mActivity = activity;
    }

    public WifiP2pManager.PeerListListener getPeerListListener() {
        return this.mController.peerListListener;
    }

    public WifiP2pManager.ConnectionInfoListener getConnectionInfoListener() {
        return this.mController.connectionInfoListener;
    }

    public WifiP2pHandler(MainActivity activity, WifiP2pManager manager, WifiP2pManager.Channel channel, WifiP2pController controller) {
        this.mActivity = activity;
        this.mWifiP2pManager = manager;
        this.mChannel = channel;
        this.mController = controller;
    }


}
