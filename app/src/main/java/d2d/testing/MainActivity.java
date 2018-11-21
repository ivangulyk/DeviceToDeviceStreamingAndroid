package d2d.testing;

import android.Manifest;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import d2d.testing.net.WifiP2pController;

public class MainActivity extends AppCompatActivity {
    public static final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 1;

    Button btnOnOff, btnsrch;
    ListView listView;
    TextView textView;

    WifiP2pController mWifiP2pController;

    IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialWork();
        execListener();
        this.checkPermissions();
    }

    private void execListener() {
        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mWifiP2pController.isWifiEnabled()) {
                    mWifiP2pController.setWifiEnabled(false);
                    btnOnOff.setText("ON");
                }
                else{
                    mWifiP2pController.setWifiEnabled(true);
                    btnOnOff.setText("OFF");
                }
            }
        });
        btnsrch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWifiP2pController.discoverPeers(new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        textView.setText("Discovery Started");
                    }

                    @Override
                    public void onFailure(int reason) {
                        textView.setText("Discovery Starting Failed");
                    }
                });
            }
        });
    }

    private void initialWork() {
        btnOnOff = (Button) findViewById(R.id.onOff);
        btnsrch = (Button) findViewById(R.id.discover);
        listView = (ListView) findViewById(R.id.peerListView);
        textView = (TextView) findViewById(R.id.connectionStatus);

        mWifiP2pController = new WifiP2pController(this);
        mIntentFilter = new IntentFilter();
        // Indicates a change in the Wi-Fi P2P status.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }

    public void updatePeers(String [] deviceNameArray)
    {
        if (deviceNameArray.length == 0) {
            //Log.d(WiFiDirectActivity.TAG, "No devices found");
            Toast.makeText(getApplicationContext(), "No Devices Found", Toast.LENGTH_SHORT).show();
            return;
        }
        else
        {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
            listView.setAdapter(adapter);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(this.mWifiP2pController.getWiFiP2pBroadcastReceiver(), mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(this.mWifiP2pController.getWiFiP2pBroadcastReceiver());
    }

    public void checkPermissions()
    {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                Toast.makeText(getApplicationContext(), "WE NEED PERMISSIONS BECAUSE YES.. BLAH BLAH BLAH JA JA JA", Toast.LENGTH_SHORT).show();
                //ask later
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkPermissions();
                    }
                }, 5 * 1000); // afterDelay will be executed after (secs*1000) milliseconds.
            } else {

                // We can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_COARSE_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted,
                    Toast.makeText(getApplicationContext(), "PERMISSON GRANTED", Toast.LENGTH_SHORT).show();
                } else {

                    // permission denied, wifi direct wont work under version ??? maybe we dont need it....
                    Toast.makeText(getApplicationContext(), "PERMISSON NOT GRANTED", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions
        }
    }
}
