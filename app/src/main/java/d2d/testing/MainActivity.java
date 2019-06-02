package d2d.testing;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.hardware.Camera;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;

import d2d.testing.gui.DeviceListAdapter;
import d2d.testing.gui.FragmentDevices;
import d2d.testing.gui.FragmentStreams;
import d2d.testing.gui.ViewPagerAdapter;
import d2d.testing.net.threads.selectors.RTSPServerSelector;
import d2d.testing.utils.Logger;
import d2d.testing.wifip2p.WifiP2pController;
import d2d.testing.utils.Permissions;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_COARSE_LOCATION_CODE = 101;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int MY_AUDIO_REQUEST_CODE = 104;
    private static final int MY_WRITE_EXTERNAL_STORAGE_CODE = 103;
    private static final int CHOOSE_FILE_CODE = 102;
    private boolean camera_has_perm = false;
    private boolean audio_has_perm = false;
    private boolean location_has_perm = false;
    private boolean storage_has_perm = false;
    private Camera mCamera;
    private String defaultP2PIp = "192.168.49.1";


    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FragmentDevices devices_fragment;
    private FragmentStreams streams_fragment;

    WifiP2pController mWifiP2pController;
    Permissions wiFiP2pPermissions;


    IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialWork();
    }

    public void set_camera_has_perm(boolean camera){
        this.camera_has_perm = camera;
    }
    public void set_audio_has_perm(boolean audio){
        this.audio_has_perm = audio;
    }
    public void set_location_has_perm(boolean location){
        this.location_has_perm = location;
    }
    public void set_storage_has_perm(boolean storage){
        this.storage_has_perm = storage;
    }

    public Permissions getWiFiP2pPermissions() {
        return wiFiP2pPermissions;
    }
    public boolean get_storage_has_perm(){
        return storage_has_perm;
    }


    private void initialWork() {

        tabLayout = findViewById(R.id.tablayout);
        viewPager = findViewById(R.id.viewpager);

        devices_fragment = new FragmentDevices();
        streams_fragment = new FragmentStreams();

        streams_fragment.setMainActivity(this);
        devices_fragment.setMainActivity(this);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.AddFragment(devices_fragment,"WiFi Devices");
        adapter.AddFragment(streams_fragment, "Streams Available");

        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);

        mWifiP2pController = WifiP2pController.getInstance(this);
        devices_fragment.setmWifiP2pController(mWifiP2pController);

        mIntentFilter = new IntentFilter();
        wiFiP2pPermissions = new Permissions(this,this);
        // Indicates a change in the Wi-Fi P2P status.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        // Indicates a change in the list of available peers.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        // Indicates the state of Wi-Fi P2P connectivity has changed.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        // Indicates this device's details have changed.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        try {
            RTSPServerSelector.initiateInstance(this).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void DiscoverPeers(){
        mWifiP2pController.discoverPeers(new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                devices_fragment.setTextView("Discovery Started");
        }

            @Override
            public void onFailure(int reason) {
                devices_fragment.setTextView("Discovery Starting Failed");
            }
        });
    }

    private void handleCamera(){
        //this.mCamera = getCameraInstance();
        //openCameraActivity();
        openStreamActivity();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }
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
                    DiscoverPeers();
                }
                return true;
            case R.id.atn_direct_camera:
                if(checkCameraHardware()) {
                    wiFiP2pPermissions.camera();
                    wiFiP2pPermissions.audio();
                    if(camera_has_perm && audio_has_perm) {
                        //TODO here goes all the functionality
                        handleCamera();
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
         devices_fragment.updateMyDeviceStatus(device);
    }

    public void updateMsg(final String str){
        runOnUiThread(new Runnable() {
            public void run() {
               devices_fragment.updateMsg(str);
            }
        });
    }

    public void updateStreamList(final boolean on_off,final String ip, final String name){
        runOnUiThread(new Runnable() {
            public void run() {
                streams_fragment.updateList(on_off, ip, name);
            }
        });
    }

    public void updatePeers(WifiP2pDevice[] deviceArray)
    {
       devices_fragment.updateDeviceArray(deviceArray);
        if (deviceArray.length == 0) {
            Toast.makeText(getApplicationContext(), "No Devices Found", Toast.LENGTH_SHORT).show();
            devices_fragment.setTextView("Discovery Finished");
        }
        else
        {
            DeviceListAdapter deviceListAdapter = new DeviceListAdapter(this, deviceArray);
            //listView.setAdapter(deviceListAdapter);
            devices_fragment.updateList(deviceListAdapter);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            RTSPServerSelector.getInstance().stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.location_has_perm = true;
                    // permission was granted,
                    Toast.makeText(getApplicationContext(), "LOCATION PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
                    DiscoverPeers();
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
                    if(audio_has_perm) {
                        handleCamera();
                    }
                    else {
                        wiFiP2pPermissions.audio();
                    }
                } else {

                    // permission denied, wifi direct wont work under version ??? maybe we dont need it...
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                        //Show permission explanation dialog...
                        Toast.makeText(getApplicationContext(), "YOU DENIED PERMISSION AND CHECKED TO NEVER ASK AGAIN, GO SETTING AND ADD CAMERA PERMISSION MANUALLY TO USE THIS", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "CAMERA PERMISSION NOT GRANTED, YOU WONT BE ABLE TO USE THIS", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            case MY_AUDIO_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.audio_has_perm = true;
                    // permission was granted,
                    Toast.makeText(getApplicationContext(), "AUDIO RECORD PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
                    if(camera_has_perm) {
                        handleCamera();
                    }
                    else {
                        wiFiP2pPermissions.camera();
                    }
                } else {

                    // permission denied, wifi direct wont work under version ??? maybe we dont need it...
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                        //Show permission explanation dialog...
                        Toast.makeText(getApplicationContext(), "YOU DENIED PERMISSION AND CHECKED TO NEVER ASK AGAIN, GO SETTING AND ADD AUDIO PERMISSION MANUALLY TO USE THIS", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "AUDIO RECORD PERMISSION NOT GRANTED, YOU WONT BE ABLE TO USE THIS", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            case MY_WRITE_EXTERNAL_STORAGE_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.storage_has_perm = true;
                    // permission was granted,
                    Toast.makeText(getApplicationContext(), "STORAGE PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
                } else {

                    // permission denied, wifi direct wont work under version ??? maybe we dont need it...
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //Show permission explanation dialog...
                        Toast.makeText(getApplicationContext(), "YOU DENIED PERMISSION AND CHECKED TO NEVER ASK AGAIN, GO SETTING AND ADD STORAGE PERMISSION MANUALLY TO USE THIS", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "STORAGE PERMISSION NOT GRANTED, YOU WONT BE ABLE TO USE THIS,TRY AGAIN", Toast.LENGTH_SHORT).show();
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

    private void openCameraActivity() {
        Intent cameraActivityIntent = new Intent(this, CameraActivity.class);
        this.startActivity(cameraActivityIntent);
    }

    private void openStreamActivity() {
        Intent streamActivityIntent = new Intent(this, StreamActivity.class);
        this.startActivity(streamActivityIntent);
    }


    public void openViewStreamActivity(String ip) {
        Intent streamActivityIntent = new Intent(this, ViewStreamActivity.class);
        streamActivityIntent.putExtra("IP",ip);
        this.startActivity(streamActivityIntent);
    }

    public String getMyIpAddress(){
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }

    public void setDefaultP2PIp(final String ip){
        runOnUiThread(new Runnable() {
            public void run() {
                defaultP2PIp = ip;
            }
        });
    }
}
