package d2d.testing.gui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import d2d.testing.R;

import static d2d.testing.R.id.device_name;
import static d2d.testing.R.id.stream_name;

public class StreamListAdapter extends ArrayAdapter<StreamDetail> {
    private Context mContext;

    public StreamListAdapter(Context context , ArrayList<StreamDetail> objects) {
        super(context, -1, objects);
        this.mContext = context;
    }

    @Override
    public View getView(int position,View convertView, ViewGroup parent) {
        String name = getItem(position).getName();
        String ip = getItem(position).getIp();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(R.layout.stream_detail,null);

        TextView stream_nam = convertView.findViewById(stream_name);
        TextView stream_ip = convertView.findViewById(R.id.stream_ip);

        stream_nam.setText(name);
        stream_ip.setText(ip);

        return convertView;
    }
}
