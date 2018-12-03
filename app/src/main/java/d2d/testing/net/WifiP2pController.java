package d2d.testing.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import d2d.testing.MainActivity;
import d2d.testing.net.helpers.RspHandler;
import d2d.testing.net.threads.ClientThread;
import d2d.testing.net.threads.ServerThread;
import d2d.testing.net.threads.ServerWorker;

/*

If each of the devices in your group supports Wi-Fi direct, you don't need to explicitly ask for the group's password when connecting. To allow a device that doesn't support Wi-Fi Direct to join a group, however, you need to retrieve this password by calling requestGroupInfo(), as shown in the following code snippet:

KOTLINJAVA
mManager.requestGroupInfo(mChannel, new GroupInfoListener() {
  @Override
  public void onGroupInfoAvailable(WifiP2pGroup group) {
      String groupPassword = group.getPassphrase();
  }
});

 */

public class WifiP2pController {

    private WifiP2pHandler mWifiP2pHandler;
    private MainActivity mContext;

    private WifiManager mWifiManager;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;

    private WiFiP2pBroadcastReceiver mReciever;
    private ServerThread mServerThread;
    private ClientThread mClientThread;
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
        this.mChannel = this.mWifiP2pManager.initialize(this.mContext, Looper.getMainLooper(), null);

        this.mWifiP2pHandler = new WifiP2pHandler (this.mContext, this.mWifiP2pManager, this.mChannel, this);
        this.mReciever = new WiFiP2pBroadcastReceiver(this.mWifiP2pHandler);

        this.mWifiStatus = false;
    }

    public boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    public boolean isWifiP2pAvailable() {
        return mWifiP2pAvailable;
    }

    public BroadcastReceiver getWiFiP2pBroadcastReceiver() {
        return this.mReciever;
    }

    public void setWifiEnabled(boolean enabled){

        if(enabled==false)
        {
            //TODO
            //close connections, threads  etc...
        }
        mWifiManager.setWifiEnabled(enabled);
    }

    public void discoverPeers(WifiP2pManager.ActionListener actionListener) {
        mWifiP2pManager.discoverPeers(this.mChannel,actionListener);
    }

    public void connectToPeer(WifiP2pDevice peer, WifiP2pManager.ActionListener actionListener) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = peer.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        this.mWifiP2pManager.connect(this.mChannel, config, actionListener);
    }

    public void send(String data) throws IOException {
        RspHandler handler = new RspHandler();
        mClientThread.send(data.getBytes(), handler);
        //handler.waitForResponse();
    }

    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {

        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            //List<WifiP2pDevice> refreshedPeers = (List<WifiP2pDevice>) peerList.getDeviceList();
            if ( !peerList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                int index = 0;

                for (WifiP2pDevice device : peerList.getDeviceList()) {
                    deviceArray[index] = device;
                    index++;
                }
            }

            mContext.updatePeers(deviceArray);
        }
    };

    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onConnectionInfoAvailable(final WifiP2pInfo info) {

            // InetAddress from WifiP2pInfo struct.
            final InetAddress groupOwnerAddress = info.groupOwnerAddress;

            // After the group negotiation, we can determine the group owner
            // (server).
            if (info.groupFormed && info.isGroupOwner) {
                //TODO
                // Do whatever tasks are specific to the group owner.
                // One common case is creating a group owner thread and accepting
                // incoming connections.
                if(mServerThread != null)
                {

                }
                else
                {
                    try {
                        mServerThread = new ServerThread();
                        new Thread(mServerThread).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //
                Toast.makeText(mContext,"You are Host", Toast.LENGTH_SHORT).show();
            } else if (info.groupFormed) {
                // The other device acts as the peer (client). In this case,
                // you'll want to create a peer thread that connects
                // to the group owner.

                try {
                    mClientThread = new ClientThread(groupOwnerAddress);
                    new Thread(mClientThread).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(mContext,"You are Client", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
