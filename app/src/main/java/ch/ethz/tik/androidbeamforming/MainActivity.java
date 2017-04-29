package ch.ethz.tik.androidbeamforming;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.view.View.OnClickListener;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/*Created by Jonas and Gabriel on 19.04.2017
*/


public class MainActivity extends AppCompatActivity {

    public final static int PORT = 56789;
    public final static int UDP_BROADCAST_PORT = 56790;

    // UDP strings, MUST be unique!
    public final static String START_CLIENT_TRANSMISSION = "AAB_ST";

    private static String TAG = MainActivity.class.getSimpleName();
    private Button setAsHost;
    private Button setAsClient;

    public static final int REQUEST_PERMISSION_CODE = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkRecordingPermission()){
            requestRecordingPermission();
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

    private void requestRecordingPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
    }

    private boolean checkRecordingPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }
}
