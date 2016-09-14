package i_will_pass.to_the_final_of.devchallenge_x.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import i_will_pass.to_the_final_of.devchallenge_x.R;
import i_will_pass.to_the_final_of.devchallenge_x.networking.NetworkIntentService;
import i_will_pass.to_the_final_of.devchallenge_x.receiver.NetworkStateReceiver;
import i_will_pass.to_the_final_of.devchallenge_x.utils.L;
import i_will_pass.to_the_final_of.devchallenge_x.utils.PSF;
import i_will_pass.to_the_final_of.devchallenge_x.utils.PSUtils;

public class MainActivity extends AppCompatActivity implements NetworkStateReceiver.ConnectionStateCallback {

    // for my style of logging \
    private static final String CN = "MainActivity ` ";

    //    private static final String DEFAULT_RSS_FEED_URL = "https://api.instagram.com/v1/users/self/media/recent/?access_token=1";
    private static final String DEFAULT_RSS_FEED_URL = "http://feeds.rucast.net/Radio-t";

    // to register / unregister receiver in this activity \
    private NetworkStateReceiver networkStateReceiver = new NetworkStateReceiver();

    private String rssFeedUrl;

    // ALL CALLBACKS ===============================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rssFeedUrl = DEFAULT_RSS_FEED_URL;
    }

    /*
        // if doing in this way - endless cycle appears - from nowhere in askRssFeed(rssFeedUrl);

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
    */

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(networkStateReceiver);
    }

    @Override
    public void onInetStateSwitched(boolean isOn) {
        L.l(CN + "network ON is " + isOn);
        // here we can download RSS-feed and start parsing \

        TextView tvInetStatus = (TextView) findViewById(R.id.tvInetStatus);
        if (isOn) {
            tvInetStatus.setText(getString(R.string.inetConnected));
            tvInetStatus.setTextColor(Color.GREEN);

            // launching request to selected RSS-feed to get all data for the list of podcasts \
            askRssFeed(rssFeedUrl);

        } else {
            tvInetStatus.setText(getString(R.string.inetDisconnected));
            tvInetStatus.setTextColor(Color.RED);
        }
    }

    @Override
    public void onWiFiStateSwitched(boolean isOn) {
        L.l(CN + "WiFi ON is " + isOn);
        // here we can start downloading media files \

        TextView tvWiFiStatus = (TextView) findViewById(R.id.tvWiFiStatus);
        if (isOn) {
            tvWiFiStatus.setText(getString(R.string.wifiIsAvailable));
            tvWiFiStatus.setTextColor(Color.GREEN);
        } else {
            tvWiFiStatus.setText(getString(R.string.wifiIsUnavailable));
            tvWiFiStatus.setTextColor(Color.RED);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PSF.R_CODE_SERVICE) {
            // ensuring ourselves in fact of taking the response from previous activity's request \
            L.l(CN + "current activity hash = " + hashCode());
            L.l(CN + "sending activity hash = " + data.getIntExtra(PSF.S_ACTIVITY_HASH, 0));

            String[] result = data.getStringArrayExtra(PSF.RSS_RESULT);

            TextView webView = (TextView) findViewById(R.id.wvTest);
            webView.setText(result[19]);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // PAYLOAD =====================================================================================

    private void askRssFeed(String sUrl) {

        if (PSUtils.isMyServiceRunning(this, NetworkIntentService.class))
            // doing nothing to prevent many similar parallel network requests \
            L.l(CN + "instance of NetworkIntentService is already working");
        else {
            PendingIntent pendingIntent = createPendingResult(PSF.R_CODE_SERVICE, new Intent(), 0);
/*
                // i also tried this way - but it doesn't work \
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                        PSF.R_CODE_SERVICE, new Intent(), 0);
*/
            Intent intent = new Intent(this, NetworkIntentService.class)
                    .putExtra(PSF.N_I_SERVICE, pendingIntent)
                    .putExtra(PSF.RSS_FEED_URL, sUrl)
                    .putExtra(PSF.S_ACTIVITY_HASH, hashCode());
            startService(intent);
        }
    }
}