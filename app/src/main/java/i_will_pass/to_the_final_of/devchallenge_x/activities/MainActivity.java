package i_will_pass.to_the_final_of.devchallenge_x.activities;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import i_will_pass.to_the_final_of.devchallenge_x.R;
import i_will_pass.to_the_final_of.devchallenge_x.receiver.NetworkStateReceiver;
import i_will_pass.to_the_final_of.devchallenge_x.utils.L;

public class MainActivity extends AppCompatActivity implements NetworkStateReceiver.ConnectionStateCallback {

    // used for my style of logging \
    private static final String CN = "MainActivity ` ";

    // we need global link to register / unregister receiver in this activity \
    private NetworkStateReceiver networkStateReceiver = new NetworkStateReceiver();

    // ALL CALLBACKS ===============================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkStateReceiver);
    }

    @Override
    public void onInetStateSwitched(boolean isOn) {
        L.l(CN + "network ON is " + isOn);
        // here we can download RSS-feed and start parsing \
    }

    @Override
    public void onWiFiStateSwitched(boolean isOn) {
        L.l(CN + "WiFi ON is " + isOn);
        // here we can start downloading media files \
    }

    // PAYLOAD =====================================================================================
}