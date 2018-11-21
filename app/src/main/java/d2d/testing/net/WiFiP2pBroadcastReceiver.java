package d2d.testing.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import d2d.testing.MainActivity;

public class WiFiP2pBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pHandler mWifiP2pHandler;

    public WiFiP2pBroadcastReceiver(WifiP2pHandler wifiP2pHandler){
        this.mWifiP2pHandler = wifiP2pHandler;
    }
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
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

            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                if(mWifiP2pHandler.getWifiP2pManager() != null) {
                    mWifiP2pHandler.getWifiP2pManager().requestPeers(mWifiP2pHandler.getChannel(), mWifiP2pHandler.getPeerListListener());
                }
                break;

            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                // Connection state changed! We should probably do something about
                // that.
                break;

            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                /*  DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
                        .findFragmentById(R.id.frag_list);
                fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                        WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
                */
                break;
        }
    }
}
