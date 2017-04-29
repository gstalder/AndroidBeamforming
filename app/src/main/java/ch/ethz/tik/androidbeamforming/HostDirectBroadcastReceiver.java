package ch.ethz.tik.androidbeamforming;

/**
 * Created by Jonas and Gabriel on 19.04.2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;


public class HostDirectBroadcastReceiver extends BroadcastReceiver{
    private WifiP2pManager mHostManager;
    private WifiP2pManager.Channel mHostChannel;
    private HostActivity mHostActivity;



    private static String TAG = HostDirectBroadcastReceiver.class.getSimpleName();

    public HostDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       HostActivity activity)
    {
        super();
        this.mHostManager = manager;
        this.mHostChannel = channel;
        this.mHostActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
        if (device != null) {
            mHostActivity.setOwnDevice(device);
        }

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // UI update to indicate wifi p2p status
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                //WiFi P2P mode enabled
                Log.w(TAG, "WiFi enabled");
                mHostActivity.setIsWifiP2pEnabled(true);
            } else {
                // WiFi P2P is not enable
                Log.w(TAG, "WiFi disabled");
                mHostActivity.setIsWifiP2pEnabled(false);
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "Peers changed");
            mHostActivity.discoverPeers();
            if (mHostManager != null) {
                mHostManager.requestPeers(mHostChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        mHostActivity.displayStates(peers);
                    }
                });
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            if (mHostManager == null) {
                return;
            }
            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

            }
            else{
                Log.d(TAG, "networkInfo false");
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing

        }
    }
}
