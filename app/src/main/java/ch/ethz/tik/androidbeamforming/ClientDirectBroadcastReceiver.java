package ch.ethz.tik.androidbeamforming;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Jonas and Gabriel on 19.04.2017.
 */

public class ClientDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager mClientManager;
    private WifiP2pManager.Channel mClientChannel;
    private ClientActivity mClientActivity;


    private static String TAG = ClientDirectBroadcastReceiver.class.getSimpleName();

    public ClientDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                         ClientActivity activity)
    {
        super();
        this.mClientManager = manager;
        this.mClientChannel = channel;
        this.mClientActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // UI update to indicate wifi p2p status
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                //WiFi P2P mode enabled
                Log.w(TAG, "WiFi enabled");
                mClientActivity.setIsWifiP2pEnabled(true);
            } else {
                // WiFi P2P is not enable
                Log.w(TAG, "WiFi disabled");
                mClientActivity.setIsWifiP2pEnabled(false);
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            //request available peers from the wifi p2p manager. This is an
            //asynchronous call and the calling activity is notified with a
            //callback on PeerListListener.onPeersAvailable
            if (mClientManager != null) {
                mClientManager.requestPeers(mClientChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        mClientActivity.displayPeers(peers);
                    }
                });
            }
            Log.d(TAG, "P2P peers changed");

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            if (mClientManager == null) {
                return;
            }
            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                mClientManager.requestConnectionInfo(mClientChannel,new WifiP2pManager.ConnectionInfoListener() {
                            @Override
                            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                                if (info != null) {
                                    mClientActivity.setConnectionInfo(info); // When connection is established with other device, We can find that info from wifiP2pInfo here.
                                }
                            }
                        }
                );
            }
            else{
                Log.d(TAG, "networkInfo false");
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }
}
