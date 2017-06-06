package ch.ethz.tik.androidbeamforming;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.view.View.OnClickListener;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.CHANGE_NETWORK_STATE;
import static android.Manifest.permission.CHANGE_WIFI_MULTICAST_STATE;
import static android.Manifest.permission.CHANGE_WIFI_STATE;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/*Created by Jonas and Gabriel on 19.04.2017
*/


public class MainActivity extends AppCompatActivity {

    public final static int PORT = 56789;
    public final static int UDP_BROADCAST_PORT = 56790;

    // UDP strings, MUST be unique!
    public final static String START_CLIENT_TRANSMISSION = "AABSTART";
    public final static String SYNC_SEQUENCE_DONE = "SYNC";
    public final static String STOP_CLIENT_TRANSMISSION = "AABSTOP";

    private static String TAG = MainActivity.class.getSimpleName();
    private Button setAsHost;
    private Button setAsClient;

    public static final int REQUEST_PERMISSION_CODE = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (!checkPermissions()){
            requestPermissions();
            checkPermissions();
        }

        setAsHost = (Button) this.findViewById(R.id.setAsHost);
        setAsClient = (Button) this.findViewById(R.id.setAsClient);

        setAsHost.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Starting as Host", Toast.LENGTH_LONG).show();
                startHost();
            }
        });

        setAsClient.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Starting as Client", Toast.LENGTH_LONG).show();
                startClient();
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void startClient() {
        Intent clientIntent = new Intent(this, ClientActivity.class);
        startActivity(clientIntent);
    }

    public void startHost() {
        Intent hostIntent = new Intent(this, HostActivity.class);
        startActivity(hostIntent);
    }

    private void requestPermissions() {
        Log.d(TAG, "requesting permissions...");
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{
                INTERNET,
                ACCESS_WIFI_STATE,
                CHANGE_WIFI_STATE,
                CHANGE_WIFI_MULTICAST_STATE,
                ACCESS_NETWORK_STATE,
                CHANGE_NETWORK_STATE,
                READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE,
                RECORD_AUDIO,
                READ_PHONE_STATE},
                REQUEST_PERMISSION_CODE);
    }

    private boolean checkPermissions() {

        Log.d(TAG, "Checking Permissions...");

        int resultInternet = ContextCompat.checkSelfPermission(getApplicationContext(),
                INTERNET);
        int resultAccessWifiState = ContextCompat.checkSelfPermission(getApplicationContext(),
                ACCESS_WIFI_STATE);
        int resultChangeWifiState = ContextCompat.checkSelfPermission(getApplicationContext(),
                CHANGE_WIFI_STATE);
        int resultChangeWifiMulticastState = ContextCompat.checkSelfPermission(getApplicationContext(),
                CHANGE_WIFI_MULTICAST_STATE);
        int resultAccessNetworkState = ContextCompat.checkSelfPermission(getApplicationContext(),
                ACCESS_NETWORK_STATE);
        int resultChangeNetworkState = ContextCompat.checkSelfPermission(getApplicationContext(),
                CHANGE_NETWORK_STATE);
        int resultReadExternalStorage = ContextCompat.checkSelfPermission(getApplicationContext(),
                READ_EXTERNAL_STORAGE);
        int resultWriteExternalStorage = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int resultRecordAudio = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        int resultReadPhoneState = ContextCompat.checkSelfPermission(getApplicationContext(),
                READ_PHONE_STATE);

        int granted = PackageManager.PERMISSION_GRANTED;

        boolean resultTotal = resultInternet == granted &&
                resultAccessWifiState == granted &&
                resultChangeWifiState == granted &&
                resultChangeWifiMulticastState == granted &&
                resultAccessNetworkState == granted &&
                resultChangeNetworkState == granted &&
                resultReadExternalStorage == granted &&
                resultWriteExternalStorage == granted &&
                resultRecordAudio == granted &&
                resultReadPhoneState == granted;

        if(resultTotal)
            Log.d(TAG, "all permissions are granted");
        else
            Log.d(TAG, "not all permissions are available");

        return resultTotal;

    }
}
