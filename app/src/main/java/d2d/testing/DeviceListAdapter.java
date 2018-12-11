package d2d.testing;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import static d2d.testing.R.id.device_name;

public class DeviceListAdapter  extends BaseAdapter {
    Context context;
    WifiP2pDevice[] deviceArray;
    MainActivity activity;

    public DeviceListAdapter(Context context, WifiP2pDevice[] deviceArray, MainActivity activity){
        this.context = context;
        this.deviceArray = deviceArray;
        this.activity = activity;
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
        LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.device_detail,null);

        TextView name = v.findViewById(device_name);
        TextView address = v.findViewById(R.id.device_adress);
        TextView status = v.findViewById(R.id.device_status);

        name.setText(deviceArray[position].deviceName);
        address.setText(deviceArray[position].deviceAddress);
        status.setText(activity.getDeviceStatus(deviceArray[position].status));

        return v;
    }
}
