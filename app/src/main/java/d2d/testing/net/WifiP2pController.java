package d2d.testing.net;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import d2d.testing.MainActivity;

public class WifiP2pController {

    private WifiP2pHandler mWifiP2pHandler;
    private MainActivity mContext;

    private WifiManager mWifiManager;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;

    private WiFiP2pBroadcastReceiver mReciever;

    private boolean mWifiStatus;
    private boolean mWifiP2pAvailable = false;

    protected List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    protected String [] deviceNameArray;
    protected WifiP2pDevice[] deviceArray;

    public WifiP2pController(MainActivity context)
    {
        this.mContext = context;
        this.mWifiManager = (WifiManager) this.mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.mWifiP2pManager = (WifiP2pManager) this.mContext.getSystemService(Context.WIFI_P2P_SERVICE);

        this.mWifiP2pHandler = new WifiP2pHandler (this.mContext, this.mWifiP2pManager, this.mChannel, this);
        this.mReciever = new WiFiP2pBroadcastReceiver(this.mWifiP2pHandler);

        this.mWifiStatus = false;
        this.mChannel = this.mWifiP2pManager.initialize(this.mContext, Looper.getMainLooper(), null);
    }

    public boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    public boolean isWifiP2pAvailable() {
        return mWifiP2pAvailable;
    }

    public void setWifiEnabled(boolean enabled){

        if(enabled==false)
        {
            //close connections, threads  etc...
        }
        mWifiManager.setWifiEnabled(enabled);
    }

    public void discoverPeers(WifiP2pManager.ActionListener actionListener) {
        mWifiP2pManager.discoverPeers(this.mChannel,actionListener);
    }

    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {

        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            //List<WifiP2pDevice> refreshedPeers = (List<WifiP2pDevice>) peerList.getDeviceList();
            if ( !peerList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll(peerList.getDeviceList());
                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                int index = 0;

                for (WifiP2pDevice device : peerList.getDeviceList()) {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }
            }

            mContext.updatePeers(deviceNameArray);
        }
    };

    private void connectToPeer(WifiP2pDevice peer) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = peer.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        this.mWifiP2pManager.connect(this.mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(mContext, "Connecting to peer ...", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(int reason) {
                Toast.makeText(mContext, "Peer connection failed with code " + Integer.toString(reason), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public BroadcastReceiver getWiFiP2pBroadcastReceiver() {
        return this.mReciever;
    }
}
