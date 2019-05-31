package d2d.testing.gui;

import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.videolan.libvlc.util.AndroidUtil;

import java.util.ArrayList;
import java.util.List;

import d2d.testing.MainActivity;
import d2d.testing.R;
import d2d.testing.net.packets.DataPacketBuilder;
import d2d.testing.streaming.Stream;

public class FragmentStreams extends Fragment {
    View view;
    private ArrayList<StreamDetail> streamList = new ArrayList();
    private TextView there_is_stream;
    private StreamListAdapter arrayAdapter;
    private ListView streamsListView;
    private MainActivity mainActivity;
    public FragmentStreams(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.streams_fragment,container,false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        streamsListView = view.findViewById(R.id.streamListView);
        there_is_stream = view.findViewById(R.id.text_stream);
        arrayAdapter = new StreamListAdapter(mainActivity.getApplicationContext(), streamList);
        streamsListView.setAdapter(arrayAdapter);
        execListener();
    }

    public void setMainActivity(MainActivity activity){
        mainActivity = activity;
    }

    private void streamAvailable(){
        if(!streamList.isEmpty())
            there_is_stream.setVisibility(View.GONE);
        else there_is_stream.setVisibility(View.VISIBLE);
    }

    public void updateList(boolean on_off, String ip, String name){
        if(!ip.equals("0.0.0.0")) {
            StreamDetail detail = new StreamDetail(name,ip);
            if (on_off) {
                if (!streamList.contains(detail))
                    streamList.add(detail);
            } else {
                if (streamList.contains(detail))
                    streamList.remove(detail);
            }
            streamAvailable();
        }
    }
    private void execListener() {

        streamsListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               mainActivity.openViewStreamActivity(streamList.get(position).getIp());
            }
        });

    }
}
