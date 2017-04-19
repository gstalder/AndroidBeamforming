package ch.ethz.tik.androidbeamforming;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.support.v7.widget.ButtonBarLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class HostActivity extends AppCompatActivity implements ConnectionInfoListener{

    private WifiP2pManager mHostManager;
    WifiP2pManager.Channel mHostChannel;
    BroadcastReceiver mHostReceiver;
    private boolean isWifiP2pEnabled = false;
    private final IntentFilter mIntentFilter = new IntentFilter();
    private List<WifiP2pDevice> peerList;
    private static String TAG = HostActivity.class.getSimpleName();
    private Button showConn;

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        // Indicates a change in Wi-Fi P2P status
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        // Indicates a change in the list of available peers
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        // Indicates the state of Wi-Fi P2P connectivity has changed
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        // Indicates this device's details have changed
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        mHostManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mHostChannel = mHostManager.initialize(this, getMainLooper(), null);
        mHostReceiver = new HostDirectBroadcastReceiver(mHostManager, mHostChannel, this);
        peerList = new ArrayList<>();
        registerReceiver(mHostReceiver, mIntentFilter);

        showConn = (Button) this.findViewById(R.id.showConn);

        showConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    public void onConnectionInfoAvailable (WifiP2pInfo wifiInfo){

    }

    @Override
    public void onStart(){
        super.onStart();
        discoverPeers();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mHostReceiver);
    }

    public void discoverPeers () {
        mHostManager.discoverPeers(mHostChannel, new WifiP2pManager.ActionListener() {

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
}
