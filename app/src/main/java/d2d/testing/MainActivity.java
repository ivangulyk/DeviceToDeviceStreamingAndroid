package d2d.testing;

import android.Manifest;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import d2d.testing.net.WifiP2pController;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_COARSE_LOCATION_CODE = 101;
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    Button btnOnOff;
    Button btnSearch;
    Button btnSend;
    Button btnCamera;
    ListView listView;
    TextView textView;

    WifiP2pController mWifiP2pController;
    WiFiP2pPermissions wiFiP2pPermissions;

    IntentFilter mIntentFilter;
    WifiP2pDevice[] mDeviceArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialWork();
        execListener();
    }

    private void execListener() {
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wiFiP2pPermissions.camera();
            }
        });
        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mWifiP2pController.isWifiEnabled()) {
                    mWifiP2pController.setWifiEnabled(false);
                    btnOnOff.setText("Turn Wifi ON");
                }
                else{
                    mWifiP2pController.setWifiEnabled(true);
                    btnOnOff.setText("Turn Wifi OFF");
                }
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wiFiP2pPermissions.location();
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
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mWifiP2pController.send("asdasd");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mWifiP2pController.connectToPeer(mDeviceArray[position], new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Connecting to peer ...", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(getApplicationContext(), "Peer connection failed with code " + Integer.toString(reason), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void initialWork() {
        btnOnOff = findViewById(R.id.onOff);
        btnCamera = findViewById(R.id.camara);
        btnSearch = findViewById(R.id.discover);
        btnSend = findViewById(R.id.sendButton);
        listView = findViewById(R.id.peerListView);
        textView = findViewById(R.id.connectionStatus);

        mWifiP2pController = new WifiP2pController(this);
        mIntentFilter = new IntentFilter();
        wiFiP2pPermissions = new WiFiP2pPermissions(this,this);
        // Indicates a change in the Wi-Fi P2P status.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        // Indicates a change in the list of available peers.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        // Indicates the state of Wi-Fi P2P connectivity has changed.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        // Indicates this device's details have changed.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }

    public void updatePeers(WifiP2pDevice[] deviceArray)
    {
        this.mDeviceArray = deviceArray;
        if (deviceArray.length == 0) {
            //Log.d(WiFiDirectActivity.TAG, "No devices found");
            Toast.makeText(getApplicationContext(), "No Devices Found", Toast.LENGTH_SHORT).show();
            return;
        }
        else
        {
            int index = 0;
            String [] deviceNameArray = new String[deviceArray.length];

            for (WifiP2pDevice device : deviceArray) {
                deviceNameArray[index] = device.deviceName;
                index++;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                                                              android.R.layout.simple_list_item_1, deviceNameArray);
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
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted,
                    Toast.makeText(getApplicationContext(), "LOCATION PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
                } else {

                    // permission denied, wifi direct wont work under version ??? maybe we dont need it....
                    Toast.makeText(getApplicationContext(), "LOCATION PERMISSION NOT GRANTED", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case MY_CAMERA_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted,
                    Toast.makeText(getApplicationContext(), "CAMERA PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
                } else {

                    // permission denied, wifi direct wont work under version ??? maybe we dont need it....
                    Toast.makeText(getApplicationContext(), "CAMERA PERMISSION NOT GRANTED", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
    //use this function when user trys to stream
    private boolean checkCameraHardware() {
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            Toast.makeText(getApplicationContext(), "YOUR DEVICE HAS NO CAMERA", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
