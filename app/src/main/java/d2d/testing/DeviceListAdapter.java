package d2d.testing;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DeviceListAdapter  extends BaseAdapter {
    Context context;
    WifiP2pDevice[] deviceArray;
    MainActivity activity;

    public DeviceListAdapter(Context context, WifiP2pDevice[] deviceArray, MainActivity activity){
        this.context = context;
        this.deviceArray = deviceArray;
        this.activity = activity;
    }

    private static String getDeviceStatus(int deviceStatus) {
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
    @Override
    public int getCount() {
        return deviceArray.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       View v = convertView;
        LayoutInflater vi = (LayoutInflater) this.activity.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        v = vi.inflate(R.layout.device_detail,null);

        TextView name = (TextView)v.findViewById(R.id.device_name);
        TextView address = (TextView)v.findViewById(R.id.device_adress);
        TextView status = (TextView)v.findViewById(R.id.device_status);

        name.setText(deviceArray[position].deviceName);
        address.setText(deviceArray[position].deviceAddress);
        status.setText(getDeviceStatus(deviceArray[position].status));
        return v;
    }
}
