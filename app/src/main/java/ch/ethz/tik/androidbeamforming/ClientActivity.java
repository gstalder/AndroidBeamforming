package ch.ethz.tik.androidbeamforming;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
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
import android.widget.ViewFlipper;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ClientActivity extends AppCompatActivity {

    private WifiP2pManager mClientManager;
    WifiP2pManager.Channel mClientChannel;
    BroadcastReceiver mClientReceiver;
    private boolean isWifiP2pEnabled = false;
    private final IntentFilter mIntentFilter = new IntentFilter();
    private List<WifiP2pDevice> peerList;
    private static String TAG = ClientActivity.class.getSimpleName();
    private WifiP2pDeviceList mWifiP2pDeviceList;

    MicCaptureToSocket micCaptureToSocket;
    String hostName;
    public InetAddress hostAddress;

    private Thread waitForStartThread;

    private UDPBroadcast udpBroadcast;

    //layout elements
    private Button discPeers;
    private Button startTransmitting;
    private TextView connectedTo;
    public  TextView clientStatus;
    private ViewFlipper viewFlipper;


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
        deletePersistentGroups();
        mClientReceiver = new ClientDirectBroadcastReceiver(mClientManager, mClientChannel, this);
        peerList = new ArrayList<>();

        // get Layout Elements
        discPeers = (Button) this.findViewById(R.id.discPeers);
        startTransmitting = (Button) this.findViewById(R.id.startTransmitting);
        connectedTo = (TextView) this.findViewById(R.id.connectedTo);
        clientStatus = (TextView) this.findViewById(R.id.clientStatus);
        viewFlipper = (ViewFlipper) this.findViewById(R.id.viewFlipper);


        udpBroadcast = new UDPBroadcast(MainActivity.UDP_BROADCAST_PORT);


        discPeers.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "start discovering peers");
                discoverPeers();
            }
        });

        startTransmitting.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                clientStatus.setText("waiting for start from host\n");
                if(hostAddress != null) {
                    micCaptureToSocket = new MicCaptureToSocket(hostAddress, MainActivity.PORT);
                    udpBroadcast.listenFor(MainActivity.START_CLIENT_TRANSMISSION, clientStatus);

                    waitForStartThread = new Thread(new Runnable() {
                        public void run() {

                            while(!udpBroadcast.checkReceived());

                            micCaptureToSocket.start();
                        }
                    }, "Waiting for START from Server Thread");
                    waitForStartThread.start();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mClientReceiver, mIntentFilter);
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

    protected void onStop() {
        micCaptureToSocket.stop();
        //unregisterReceiver(mClientReceiver);
        disconnect();
        deletePersistentGroups();
        super.onStop();
    }

    protected void onDestroy() {
        micCaptureToSocket.stop();
        disconnect();
        deletePersistentGroups();
        super.onDestroy();
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
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = peer.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 0;

        mClientManager.connect(mClientChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                hostName = peer.deviceName;
                viewFlipper.showNext();
                connectedTo.append(hostName);
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(ClientActivity.this, "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void displayPeers(final WifiP2pDeviceList peers) {

        ListView peerView = (ListView) findViewById(R.id.peers_listview);
        ListView statusView = (ListView) findViewById(R.id.status_listview);

        ArrayList<String> peersStringArrayList = new ArrayList<String>();
        ArrayList<String> statusStringArrayList = new ArrayList<String>();

        peerView.setClickable(true);


        for (WifiP2pDevice device : peers.getDeviceList()){
            peersStringArrayList.add(device.deviceName);
            statusStringArrayList.add("Status: " + device.status);
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, peersStringArrayList.toArray());
        ArrayAdapter arrayAdapter1 = new ArrayAdapter(this, android.R.layout.simple_list_item_1, statusStringArrayList.toArray());


        peerView.setAdapter(arrayAdapter);
        statusView.setAdapter(arrayAdapter1);

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
    }

    public void setConnectionInfo (WifiP2pInfo info){
        hostAddress = info.groupOwnerAddress;
    }

    private void deletePersistentGroups(){
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(mClientManager, mClientChannel, netid, null);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect (){
        if (mClientChannel != null && mClientChannel != null) {
            mClientManager.cancelConnect(mClientChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "cancelConnect succeed.");
                }

                @Override
                public void onFailure(int reason) {
                    Log.d(TAG, "cancelConnect failed." + reason);
                }
            });
            mClientManager.requestGroupInfo(mClientChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null){
                        mClientManager.removeGroup(mClientChannel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "removeGroup success");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d(TAG, "removeGroup failed." + reason);
                            }
                        });
                    }
                }
            });
        }
    }

}