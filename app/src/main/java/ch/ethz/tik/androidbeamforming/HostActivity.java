package ch.ethz.tik.androidbeamforming;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class HostActivity extends AppCompatActivity implements ConnectionInfoListener{

    private WifiP2pManager mHostManager;
    WifiP2pManager.Channel mHostChannel;
    BroadcastReceiver mHostReceiver;
    private SocketToFile socketToFile;
    private boolean isWifiP2pEnabled = false;
    private final IntentFilter mIntentFilter = new IntentFilter();
    private List<WifiP2pDevice> peers;
    private static String TAG = HostActivity.class.getSimpleName();
    private Button showConn;
    private Button startReceiving;
    private Button stopReceiving;
    private WifiP2pDevice ownDevice;
    private String ownDeviceName;


    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    public void setOwnDevice (WifiP2pDevice device){
        this.ownDevice = device;
    }

    public void setOwnDeviceName(String name){
        this.ownDeviceName = name;
        TextView textView = (TextView) findViewById(R.id.ownName);
        textView.setText("Your Name is " + this.ownDeviceName);
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
        peers = new ArrayList<>();

        socketToFile = new SocketToFile(MainActivity.PORT);

        showConn = (Button) this.findViewById(R.id.showConn);
        startReceiving = (Button) this.findViewById(R.id.startReceiving);
        stopReceiving = (Button) this.findViewById(R.id.stopReceiving);

        showConn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mHostManager.requestConnectionInfo(mHostChannel, new ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        Log.d(TAG, info.toString());
                        if (info.groupFormed && info.isGroupOwner){
                            Log.d(TAG, "is group owner");
                        }
                        else if (info.groupFormed){
                            Log.d(TAG, "not group owner");
                            setAsGroupOwner(ownDevice);
                        }
                    }
                });
                Log.d(TAG, Integer.toString(ownDevice.status) + " status");
            }
        });

        startReceiving.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v){
                socketToFile.Start();
            }
        });

        stopReceiving.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v){
                socketToFile.Stop();
            }
        });

    }

    @Override
    public void onStart(){
        super.onStart();
        registerReceiver(mHostReceiver, mIntentFilter);
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
    protected void onStop() {
        super.onStop();
        if (mHostManager != null && mHostChannel != null) {
            mHostManager.cancelConnect(mHostChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "disconnect succeed.");
                }

                @Override
                public void onFailure(int reason) {
                    Log.d(TAG, "disconnect failed.");
                }
            });
        }
        //unregisterReceiver(mHostReceiver);
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

    public void onConnectionInfoAvailable(final WifiP2pInfo info){
        Log.d(TAG, "in onConnectionInfoAvailable");
        mHostManager.requestConnectionInfo(mHostChannel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                Log.d(TAG, info.toString());
            }
        });
    }

    public void setAsGroupOwner(WifiP2pDevice device){
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 0;
    }
}
