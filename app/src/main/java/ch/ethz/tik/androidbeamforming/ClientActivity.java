package ch.ethz.tik.androidbeamforming;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ClientActivity extends AppCompatActivity {

    private WifiP2pManager mClientManager;
    WifiP2pManager.Channel mClientChannel;
    BroadcastReceiver mClientReceiver;
    private boolean isWifiP2pEnabled = false;
    ClientActivity clientActivity;
    private final IntentFilter mIntentFilter = new IntentFilter();
    private List<WifiP2pDevice> peerList;
    private static String TAG = ClientActivity.class.getSimpleName();
    private WifiP2pDeviceList mWifiP2pDeviceList;

    private Button discPeers;
    private TextView connectedTo;


    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        // Indicates a change in Wi-Fi P2P status
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        // Indicates a change in the list of available peers
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        // Indicates the state of Wi-Fi P2P connectivity has changed
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        // Indicates this device's details have changed
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        mClientManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mClientChannel = mClientManager.initialize(this, getMainLooper(), null);
        mClientReceiver = new ClientDirectBroadcastReceiver(mClientManager, mClientChannel, this);
        peerList = new ArrayList<>();
        registerReceiver(mClientReceiver, mIntentFilter);

        discPeers = (Button) this.findViewById(R.id.discPeers);
        connectedTo = (TextView) this.findViewById(R.id.connectedTo);

        discPeers.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "start discovering peers");
                discoverPeers();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        discoverPeers();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mClientReceiver);
    }

    public void discoverPeers () {
        mClientManager.discoverPeers(mClientChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.w(TAG, "Peers discovered");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.w(TAG, "Peers not discovered");
            }
        });
    }

    public void connect(final WifiP2pDevice peer) {
        // Picking the first device found on the network.
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = peer.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 0;

        mClientManager.connect(mClientChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                setContentView(R.layout.activity_client_connected);
                connectedTo.append(peer.deviceName);
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(ClientActivity.this, "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void displayPeers(final WifiP2pDeviceList peers) {

        ListView peerView = (ListView) findViewById(R.id.peers_listview);

        ArrayList<String> peersStringArrayList = new ArrayList<String>();

        peerView.setClickable(true);


        for (WifiP2pDevice device : peers.getDeviceList()){
            peersStringArrayList.add(device.deviceName);
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, peersStringArrayList.toArray());

        peerView.setAdapter(arrayAdapter);

        peerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view;
                WifiP2pDevice device_to_connect = null;

                for (WifiP2pDevice device : peers.getDeviceList()){
                    if(device.deviceName.equals(tv.getText()))
                        device_to_connect = device;
                }
                if (device_to_connect != null){
                    connect(device_to_connect);
                }
                else {
                    Toast.makeText(ClientActivity.this, "failed", Toast.LENGTH_LONG).show();
                }
            }
        });

        /*
        if (!refreshedPeers.equals(peerList)){
            peerList.clear();
            peerList.addAll(refreshedPeers);
        }

        if (peersStringArrayList.size() > 0){
            WifiP2pDevice deviceToConnect = new WifiP2pDevice();
            for (int i = 0; i < peersStringArrayList.size(); i++) {
                Log.d(TAG, peersStringArrayList.get(i));
                if (peerList.size() > 0) {
                    //deviceToConnect = peerList.get(0);
                    //connect(deviceToConnect);
                }
                else {Log.d(TAG, "No device in PeerList"); }
            }
        }
        else{ Log.d(TAG, "no peers in list");}*/


    }

    /*
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {

        Collection<WifiP2pDevice> refreshedPeers = peerList.getDeviceList();

        if (!refreshedPeers.equals(peers)) {
            peers.clear();
            peers.addAll(refreshedPeers);
            for (int i=0; i < peers.size(); i++){
                WifiP2pDevice device = peers.get(i);
                String deviceName = device.deviceName;
                Toast.makeText(MainActivity.this, deviceName, Toast.LENGTH_LONG).show();
            }
        }
        else {
            Log.d(TAG, "no peers");
        }
    }*/



}
