package d2d.testing.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

public class WiFiP2pBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pHandler mWifiP2pHandler;

    public WiFiP2pBroadcastReceiver(WifiP2pHandler wifiP2pHandler){
        this.mWifiP2pHandler = wifiP2pHandler;
    }
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() == null) //some problem going on here?
            return;

        switch (intent.getAction()) {
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                // Determine if Wifi P2P mode is enabled or not, alert
                // the Activity.
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Toast.makeText(context,"Wifi is ON", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context,"Wifi is OFF", Toast.LENGTH_SHORT).show();
                }
                break;
            /*  todo
            WIFI_P2P_DISCOVERY_CHANGED_ACTION
            WIFI_P2P_DISCOVERY_STARTED
            WIFI_P2P_DISCOVERY_STOPPED

            added in API level 16
            public static final String WIFI_P2P_DISCOVERY_CHANGED_ACTION
            Broadcast intent action indicating that peer discovery has either started or stopped. One extra EXTRA_DISCOVERY_STATE indicates whether discovery has started or stopped.

            Note that discovery will be stopped during a connection setup. If the application tries to re-initiate discovery during this time, it can fail.

            Constant Value: 1 (0x00000001
            */

            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                if(mWifiP2pHandler.getWifiP2pManager() != null) {
                    mWifiP2pHandler.getWifiP2pManager().requestPeers(mWifiP2pHandler.getChannel(), mWifiP2pHandler.getPeerListListener());
                }
                break;

            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                if(mWifiP2pHandler.getWifiP2pManager() != null) {
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                    if (networkInfo.isConnected()) {

                        // We are connected with the other device, request connection
                        // info to find group owner IP

                        mWifiP2pHandler.getWifiP2pManager().requestConnectionInfo(mWifiP2pHandler.getChannel(), mWifiP2pHandler.getConnectionInfoListener());
                    }
                }
                break;

            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                mWifiP2pHandler.mActivity.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

                break;
        }
    }
}
