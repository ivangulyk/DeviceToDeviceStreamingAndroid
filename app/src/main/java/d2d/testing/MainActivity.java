package d2d.testing;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import d2d.testing.gui.DeviceListAdapter;
import d2d.testing.helpers.Logger;
import d2d.testing.net.WifiP2pController;
import d2d.testing.net.packets.DataFormat;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_COARSE_LOCATION_CODE = 101;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int CHOOSE_FILE_CODE = 102;
    private boolean camera_has_perm = false;
    private boolean location_has_perm = false;

    Button btnOnOff;
    Button btnSearch;
    Button btnSend;
    Button btnCamera;
    ListView listView;
    ListView listMsg;
    TextView textView;
    EditText editTextMsg;
    TextView redMsg;
    TextView myName;
    TextView myAdd;
    TextView myStatus;

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

    public void set_camera_has_perm(boolean camera){
        this.camera_has_perm = camera;
    }
    public void set_location_has_perm(boolean location){
        this.location_has_perm = location;
    }

    private void execListener() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWifiP2pController.send(DataFormat.createMessagePacket(editTextMsg.getText().toString()));
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
        btnSend = findViewById(R.id.sendButton);
        listView = findViewById(R.id.peerListView);
        textView = findViewById(R.id.connectionStatus);
        editTextMsg = findViewById(R.id.writeMsg);
        redMsg = findViewById(R.id.readMsg);
        myAdd = findViewById(R.id.my_address);
        myName = findViewById(R.id.my_name);
        myStatus = findViewById(R.id.my_status);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.atn_direct_enable:
                if(mWifiP2pController.isWifiEnabled()) {
                    mWifiP2pController.setWifiEnabled(false);
                }
                else{
                    mWifiP2pController.setWifiEnabled(true);
                }
                return true;

            case R.id.atn_direct_discover:
                wiFiP2pPermissions.location();
                if(location_has_perm) {
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
                return true;
            case R.id.atn_direct_camera:
                if(checkCameraHardware()) {
                    wiFiP2pPermissions.camera();
                    if(camera_has_perm) {
                        //TODO here goes all the functionality
                    }
                }
                return true;
            case R.id.atn_direct_file_transfer:
                    onBrowse();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static String getDeviceStatus(int deviceStatus) {
        //Log.d(MainActivity.TAG, "Peer status :" + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }

    public void updateThisDevice(WifiP2pDevice device) {
         myName.setText(device.deviceName);
         myStatus.setText(getDeviceStatus(device.status));
         myAdd.setText(device.deviceAddress);
    }

    public void updateMsg(final String str){
        runOnUiThread(new Runnable() {
            public void run() {
                redMsg.setText(str);
            }
        });
    }

    public void updatePeers(WifiP2pDevice[] deviceArray)
    {
        this.mDeviceArray = deviceArray;
        if (deviceArray.length == 0) {
            Toast.makeText(getApplicationContext(), "No Devices Found", Toast.LENGTH_SHORT).show();
        }
        else
        {
            DeviceListAdapter deviceListAdapter = new DeviceListAdapter(this,deviceArray,this);
            listView.setAdapter(deviceListAdapter);
        }

    }

    public void onBrowse() {
        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("image/*");
        intent = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(intent, CHOOSE_FILE_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        if(requestCode == CHOOSE_FILE_CODE)
        {
            Uri uri = data.getData();
            Logger.d("Path selected " + uri);
            this.mWifiP2pController.sendFile(uri);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(this.mWifiP2pController.getWiFiP2pBroadcastReceiver(), mIntentFilter);
//        editText.clearFocus();
    }
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(this.mWifiP2pController.getWiFiP2pBroadcastReceiver());
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.location_has_perm = true;
                    // permission was granted,
                    Toast.makeText(getApplicationContext(), "LOCATION PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
                } else {

                    // permission denied, wifi direct wont work under version ??? maybe we dont need it....

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        //Show permission explanation dialog...
                        Toast.makeText(getApplicationContext(), "YOU DENIED PERMISSION AND CHECKED TO NEVER ASK AGAIN, GO SETTING AND ADD LOCATION PERMISSION MANUALLY TO USE THIS", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "LOCATION PERMISSION NOT GRANTED, YOU WONT BE ABLE TO USE THIS", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            case MY_CAMERA_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.camera_has_perm = true;
                    // permission was granted,
                    Toast.makeText(getApplicationContext(), "CAMERA PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
                } else {

                    // permission denied, wifi direct wont work under version ??? maybe we dont need it...
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                        //Show permission explanation dialog...
                        Toast.makeText(getApplicationContext(), "YOU DENIED PERMISSION AND CHECKED TO NEVER ASK AGAIN, GO SETTING AND ADD CAMERA PERMISSION MANUALLY YO USE THIS", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "CAMERA PERMISSION NOT GRANTED, YOU WONT BE ABLE TO USE THIS", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
        }
    }

    private boolean checkCameraHardware() {
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            //Toast.makeText(getApplicationContext(), "YOUR DEVICE HAS CAMERA", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            // no camera on this device
            Toast.makeText(getApplicationContext(), "YOUR DEVICE HAS NO CAMERA", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
