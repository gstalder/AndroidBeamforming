package ch.ethz.tik.androidbeamforming;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View.OnClickListener;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class HostActivity extends AppCompatActivity implements ConnectionInfoListener{

    private static String TAG = HostActivity.class.getSimpleName();

    //TODO muss hier nur der serversocket geschlossen werden oder auch alle sockets zu den clients? reicht "einseitiges" schliessen?

    private WifiP2pManager mHostManager;
    WifiP2pManager.Channel mHostChannel;
    BroadcastReceiver mHostReceiver;
    private SocketToFile socketToFile;
    private boolean isWifiP2pEnabled = false;
    private boolean isAcceptingConnections = true;
    private final IntentFilter mIntentFilter = new IntentFilter();
    private List<WifiP2pDevice> peers;

    private Button showConn;
    private Button startReceiving;
    private Button stopReceiving;
    private Button resetConnections;
    private Button udpTest;
    public TextView udpStatus;
    public TextView ownName;
    private WifiP2pDevice ownDevice;
    private String ownDeviceName;
    public InetAddress hostAddress;

    private ServerSocket serverSocket;
    private List<SocketToFile> socketToFileList;
    private List<InetAddress> clientAddressList;
    private Thread connectionAcceptThread;
    private String filename;
    private int clientNumber = 0;

    private UDPBroadcast udpBroadcast;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        Log.d(TAG, "host activity created");

        //WIFIP2P IMPLEMENTATION
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        // Indicates a change in the list of available peers
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        // Indicates the state of Wi-Fi P2P connectivity has changed
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        // Indicates this device's details have changed
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mHostManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mHostChannel = mHostManager.initialize(this, getMainLooper(), null);

        deletePersistentGroups();
        mHostReceiver = new HostDirectBroadcastReceiver(mHostManager, mHostChannel, this);
        peers = new ArrayList<>();

        //BUTTONS
        showConn = (Button) this.findViewById(R.id.showConn);
        startReceiving = (Button) this.findViewById(R.id.startReceiving);
        stopReceiving = (Button) this.findViewById(R.id.stopReceiving);
        resetConnections = (Button) this.findViewById(R.id.resetConnections);
        udpTest = (Button) this.findViewById(R.id.udpTest);
        udpStatus = (TextView) this.findViewById(R.id.udpStatus);

        registerReceiver(mHostReceiver, mIntentFilter);
        discoverPeers();



        //udpBroadcast = new UDPBroadcast(MainActivity.UDP_BROADCAST_PORT);



        connectionAcceptThread = new Thread(new Runnable() {
            public void run() {

                while(hostAddress == null){
                    Log.d(TAG, "no host");
                    getHostAddress();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Log.d(TAG, "Host: " + hostAddress);

                try {
                    serverSocket = new ServerSocket(MainActivity.PORT, 8, hostAddress);
                    Log.d(TAG, "server socket created on " + hostAddress);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                socketToFileList = new ArrayList<>();
                filename = getFilename();

                while(isAcceptingConnections) {
                    try {
                        Log.d(TAG, "before accept");
                        Socket newClient = serverSocket.accept();
                        Log.d(TAG, "after accept");

                        ownName.append("Your IP: " + newClient.getLocalAddress() + "\n" +
                                "New Client: " + newClient.getInetAddress() + "\n" +
                                "WiFi P2P GO address is: " + hostAddress + "\n");
                        clientNumber++;
                        if(isAcceptingConnections) {
                            socketToFileList.add(new SocketToFile(newClient, filename + "_" + clientNumber + ".pcm"));
                            clientAddressList.add(newClient.getInetAddress());
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }
        }, "Connection Accepting Thread");
        connectionAcceptThread.start();


        showConn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateConnectionStatus();
                mHostManager.requestConnectionInfo(mHostChannel, new ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        Log.d(TAG, info.toString());
                        if (info.groupFormed && info.isGroupOwner){
                            Log.d(TAG, "is group owner");
                        }
                        else if (info.groupFormed){
                            Log.d(TAG, "not group owner");
                        }
                    }
                });
                Log.d(TAG, Integer.toString(ownDevice.status) + " status");
            }
        });

        resetConnections.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
                deletePersistentGroups();
            }
        });

        startReceiving.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v){
                isAcceptingConnections = false;
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                for (int i = 0; i < socketToFileList.size(); i++)
                    socketToFileList.get(i).Start();
                //nur auskommentiert zum testen
                //udpBroadcast.send(MainActivity.START_CLIENT_TRANSMISSION, udpStatus, HostActivity.this.getApplicationContext());
            }
        });

        stopReceiving.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v){
                stopAndClose();
            }
        });

        udpTest.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                udpStatus.setText("Status: trying to send");
                //udpBroadcast.send(MainActivity.START_CLIENT_TRANSMISSION, udpStatus, HostActivity.this.getApplicationContext());
            }
        });

    }


    //Life cycle methods --------------------------------------------------------------------------------------------------------
    @Override
    public void onStart(){
        super.onStart();
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
        stopAndClose();
        disconnect();
        deletePersistentGroups();
        super.onStop();
        //unregisterReceiver(mHostReceiver);
    }

    protected void onDestroy() {
        stopAndClose();
        disconnect();
        deletePersistentGroups();
        super.onDestroy();
    }

    // WifiP2p methods ---------------------------------------------------------------------------------------------------------------
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    public void setOwnDevice (WifiP2pDevice device){
        ownDevice = device;
        ownDeviceName = device.deviceName;
        ownName = (TextView) findViewById(R.id.ownName);
        ownName.setText("Your Name is " + this.ownDeviceName);
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

    public void displayStates(final WifiP2pDeviceList peers){
        ListView peerStatusView = (ListView) findViewById(R.id.peersStatus_listview);
        ArrayList<String> peersStatusStringArrayList = new ArrayList<String>();

        for (WifiP2pDevice device : peers.getDeviceList()){
            peersStatusStringArrayList.add(device.deviceName + " Status: " + device.status + " GO: " + checkGroupOwner(device));
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, peersStatusStringArrayList.toArray());
        peerStatusView.setAdapter(arrayAdapter);
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

    public void updateConnectionStatus() {
        mHostManager.requestPeers(mHostChannel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                displayStates(peers);
            }
        });
    }

    public String checkGroupOwner (WifiP2pDevice device) {
        if (device.status < 3) {
            if (device.isGroupOwner()) return "no";
            return "yes";
        }
        else return "nc";
    }

    private void deletePersistentGroups(){
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(mHostManager, mHostChannel, netid, null);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void getHostAddress(){
        mHostManager.requestConnectionInfo(mHostChannel,new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        if (info != null) {
                            hostAddress = info.groupOwnerAddress;
                        }
                    }
                }
        );
    }

    public void disconnect() {
        if (mHostManager != null && mHostChannel != null) {
            mHostManager.cancelConnect(mHostChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "cancelConnect succeed.");
                }

                @Override
                public void onFailure(int reason) {
                    Log.d(TAG, "cancelConnect failed." + reason);
                }
            });
            mHostManager.requestGroupInfo(mHostChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null){
                        mHostManager.removeGroup(mHostChannel, new WifiP2pManager.ActionListener() {
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

    // TCP methods ---------------------------------------------------------------------------------------------------------------------
    private String getFilename() {
        Date curDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        return format.format(curDate);
    }

    private void stopAndClose(){
        for (int i = 0; i < socketToFileList.size(); i++)
            socketToFileList.get(i).Stop();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}