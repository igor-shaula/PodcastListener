package i_will_pass.to_final_of.devchallenge_x.activities;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import i_will_pass.to_final_of.devchallenge_x.R;
import i_will_pass.to_final_of.devchallenge_x.entity.InfoEntity;
import i_will_pass.to_final_of.devchallenge_x.receiver.NetworkStateReceiver;
import i_will_pass.to_final_of.devchallenge_x.rv_adapter.RVAdapter;
import i_will_pass.to_final_of.devchallenge_x.rv_listener.RVOnItemTouchListener;
import i_will_pass.to_final_of.devchallenge_x.services.StartingIntentService;
import i_will_pass.to_final_of.devchallenge_x.utils.L;
import i_will_pass.to_final_of.devchallenge_x.utils.PSF;
import i_will_pass.to_final_of.devchallenge_x.utils.PSUtils;

public class StartingActivity extends AppCompatActivity implements NetworkStateReceiver.ConnectionStateCallback {

    // for my style of logging \
    private static final String CN = "StartingActivity ` ";

    private static final String HTTP = "http://";
    private static final String WWW = "www.";
    private static final String INFO_ENTITY_ARRAY = "infoEntityArray";

    // to register / unregister receiver in this activity \
    private NetworkStateReceiver networkStateReceiver = new NetworkStateReceiver();

    private String rssFeedUrl;

    private TextView tvHeadLink;

    // main data container declared here for saving/restoring while screen is rotated \
    private InfoEntity[] infoEntityArray;

    // ALL CALLBACKS ===============================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_starting);

        tvHeadLink = (TextView) findViewById(R.id.tvHeadLink);

        rssFeedUrl = HTTP + getString(R.string.defaultRssFeedUrl);

        // trying to recreate data without asking network for what has already been delivered \
        if (savedInstanceState != null) {
            L.l(CN + "savedInstanceState is not null - trying to restore data without internet");
            // we need to get data for a new instance of this activity that was recreated \
            Parcelable[] parcelableArray = savedInstanceState.getParcelableArray(INFO_ENTITY_ARRAY);
            infoEntityArray = getInfoEntityArrayFrom(parcelableArray);
        }
        L.l(CN + "created activity hashCode = " + hashCode());
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
    protected void onSaveInstanceState(Bundle outState) {
        // initially I've chosen ArrayList instead of List just to avoid type casting in this method \
        outState.putParcelableArray(INFO_ENTITY_ARRAY, infoEntityArray);
        // calling this to superclass is needed for saving states of all views with id \
        super.onSaveInstanceState(outState);
        // just for note - onSaveInstanceState gets invoked before onStop \
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

            if (infoEntityArray == null)
                // launching request to selected RSS-feed to get all data for the list of podcasts \
                askRssFeed(rssFeedUrl);

            tvHeadLink.setTextColor(Color.GREEN);
        } else {
            tvInetStatus.setText(getString(R.string.inetDisconnected));
            tvInetStatus.setTextColor(Color.RED);

            tvHeadLink.setTextColor(Color.RED);
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

            showParsedHeaders(data);
            showParsedItems(data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // PAYLOAD =====================================================================================

    private void askRssFeed(String sUrl) {

        if (PSUtils.isMyServiceRunning(this, StartingIntentService.class))
            // doing nothing to prevent many similar parallel network requests \
            L.l(CN + "instance of StartingIntentService is already working");
        else {
            PendingIntent pendingIntent = createPendingResult(PSF.R_CODE_SERVICE, new Intent(), 0);
/*
                // i also tried this way - but it doesn't work \
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                        PSF.R_CODE_SERVICE, new Intent(), 0);
*/
            Intent intent = new Intent(this, StartingIntentService.class)
                    .putExtra(PSF.STARTING_INTENT_SERVICE, pendingIntent)
                    .putExtra(PSF.RSS_FEED_URL, sUrl)
                    .putExtra(PSF.S_ACTIVITY_HASH, hashCode());
            startService(intent);
        }
    }

    private void showParsedHeaders(Intent data) {

        String headTitle = data.getStringExtra(PSF.RSS_HEAD_TITLE);
        String headLink = data.getStringExtra(PSF.RSS_HEAD_LINK);
        String headSummary = data.getStringExtra(PSF.RSS_HEAD_SUMMARY);
        L.l(CN + "head title = " + headTitle);
        L.l(CN + "head link = " + headLink);
        L.l(CN + "head summary = " + headSummary);

        TextView tvHeadTitle = (TextView) findViewById(R.id.tvHeadTitle);
        TextView tvHeadSummary = (TextView) findViewById(R.id.tvHeadSummary);

        // i decided to hide all specific symbols in URL from user here \
        String reducedHeadLink = headLink.replaceFirst(HTTP + WWW, "");
        int reducedLength = reducedHeadLink.length();
        if (reducedHeadLink.charAt(reducedLength - 1) == '/') {
            reducedHeadLink = reducedHeadLink.substring(0, reducedLength - 1);
        }

        tvHeadTitle.setText(headTitle);
        tvHeadLink.setText(reducedHeadLink);
        tvHeadSummary.setText(headSummary);
    }

    private void showParsedItems(Intent data) {

        RecyclerView rvPodCasts = (RecyclerView) findViewById(R.id.rvPodCasts);

        rvPodCasts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        Parcelable[] parcelableArray = data.getParcelableArrayExtra(PSF.RSS_ITEMS_ARRAY);

        infoEntityArray = getInfoEntityArrayFrom(parcelableArray);

        rvPodCasts.setAdapter(new RVAdapter(this, infoEntityArray));

        rvPodCasts.addOnItemTouchListener(new RVOnItemTouchListener(this) {
            @Override
            public void onListItemTouch(int whichIndex) {
                launchDetailsActivityFor(infoEntityArray[whichIndex]);
            }
        });
    }

    private InfoEntity[] getInfoEntityArrayFrom(Parcelable[] parcelableArray) {
        // doing trick because we cannot simply cast Parcelable[] to InfoEntity[] \
        int arraySize = parcelableArray.length;
        InfoEntity[] infoEntityArray = new InfoEntity[arraySize];
        for (int i = 0; i < arraySize; i++)
            infoEntityArray[i] = (InfoEntity) parcelableArray[i];
        return infoEntityArray;
    }

    // temporary launching next activity - later i'll change it to launch fragment \
    private void launchDetailsActivityFor(InfoEntity infoEntity) {

        Intent intent = new Intent(this, DetailActivity.class)
//                .putExtra(PSF.IE_TITLE, infoEntity.getTitle())
//                .putExtra(PSF.IE_LINK, infoEntity.getLink())
//                .putExtra(PSF.IE_PUB_DATE, infoEntity.getPubDate())
//                .putExtra(PSF.IE_MEDIA_CONTENT_URL, infoEntity.getMediaContentUrl())
//                .putExtra(PSF.IE_FILE_SIZE, infoEntity.getFileSize())
//                .putExtra(PSF.IE_TYPE, infoEntity.getType())
//                .putExtra(PSF.IE_SUMMARY, infoEntity.getSummary());
                .putExtra(PSF.INFO_ENTITY, infoEntity);
        // here we're going to the next level activity \
        startActivity(intent);
    }
}