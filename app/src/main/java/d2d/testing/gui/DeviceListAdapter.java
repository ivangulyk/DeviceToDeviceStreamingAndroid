package d2d.testing.gui;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import d2d.testing.MainActivity;
import d2d.testing.R;

import static d2d.testing.R.id.device_name;

public class DeviceListAdapter  extends ArrayAdapter<WifiP2pDevice> {
    Context mContext;
    WifiP2pDevice[] mDeviceArray;

    public DeviceListAdapter(Context context, WifiP2pDevice[] deviceArray) {
        super(context, -1, deviceArray);
        this.mContext = context;
        this.mDeviceArray = deviceArray;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.device_detail,null);

        TextView name = row.findViewById(device_name);
        TextView address = row.findViewById(R.id.device_adress);
        TextView status = row.findViewById(R.id.device_status);

        name.setText(mDeviceArray[position].deviceName);
        address.setText(mDeviceArray[position].deviceAddress);
        status.setText(MainActivity.getDeviceStatus(mDeviceArray[position].status));

        return row;
    }
}
