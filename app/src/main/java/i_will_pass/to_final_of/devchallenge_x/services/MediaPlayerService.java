package i_will_pass.to_final_of.devchallenge_x.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import java.io.IOException;

import i_will_pass.to_final_of.devchallenge_x.R;
import i_will_pass.to_final_of.devchallenge_x.activities.DetailActivity;
import i_will_pass.to_final_of.devchallenge_x.utils.L;
import i_will_pass.to_final_of.devchallenge_x.utils.PSF;

/**
 * does all job connected with MediaPlayer \
 */
public class MediaPlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener {

    private static final String CN = "MediaPlayerService ` ";
    private static final String WIFI_TAG = "MediaPlayerService";

//    private static final String ACTION_PLAY = "com.example.action.PLAY";

    private MediaPlayer mediaPlayer;

    private boolean playerPrepared;

    private WifiManager.WifiLock wifiLock;

    private final IBinder binder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MediaPlayerService.this;
        }
    }

    public MediaPlayerService() {
        // required by system \
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String mediaContentUrl = intent.getStringExtra(PSF.IE_MEDIA_CONTENT_URL);
        // we need explicit link for other possible controlling methods - onDestroy for instance \
        mediaPlayer = new MediaPlayer();
        prepareMediaPlayer(mediaPlayer, mediaContentUrl);

        // making our service foreground - adding status bar notification \

        Intent innerIntent = new Intent(this, DetailActivity.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, innerIntent, 0);

        // Use new API
        Notification.Builder builder = new Notification.Builder(this)
                .setAutoCancel(false)
                .setTicker("this is ticker text")
                .setContentTitle("content title")
                .setContentText("content text")
                .setSmallIcon(R.drawable.icon_media_play_blue)
                .setContentIntent(pendingIntent)
                .setOngoing(true);

        Notification notification;
        if (Build.VERSION.SDK_INT > 15) {
            builder.setSubText("This is subtext...");   //API level 16
            notification = builder.build();
        } else
            //noinspection deprecation
            notification = builder.getNotification();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(11, notification);
/*
        if (intent.getAction().equals(ACTION_PLAY) && playerPrepared) {
            mediaPlayer.start();
        }
*/
        return START_NOT_STICKY; // to avoid writing -> if (intent != null) ... else stopSelf();
    } // end of onStartCommand-method \\

    @Override
    public void onDestroy() {
        super.onDestroy();
        // stopping and releasing all media resources here \
        if (mediaPlayer.isPlaying())
            mediaPlayer.stop();
        mediaPlayer.release();
        // explicitly telling GC to clean this heavy container with already useless data \
        mediaPlayer = null;

        if (wifiLock.isHeld())
            wifiLock.release();

        stopForeground(true);
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        playerPrepared = true;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        L.e(CN + "onError: i = " + i + " , i1 = " + i1);
        playerPrepared = false;

        // The MediaPlayer has moved to the Error state, must be reset!

        stopForeground(true);
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        L.l(CN + "onInfo: i = " + i + " , i1 = " + i1);
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // MAIN ACTIONS ================================================================================

    private void prepareMediaPlayer(MediaPlayer mediaPlayer, String mediaContentUrl) {

        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);

        // setting CPU wake lock ON \
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        // setting WiFi wake lock ON \
        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, WIFI_TAG);
        wifiLock.acquire();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(mediaContentUrl);

            // i'd like to measure time of preparing stream \
            L.e(CN + "before onPrepared: " + System.currentTimeMillis());
            mediaPlayer.prepareAsync();
            // when this step is done - onPrepared will be called \

        } catch (IOException e) {
            e.printStackTrace();
        }
    } // end of prepareMediaPlayer-method \\

    public void playMedia() {
        if (playerPrepared) mediaPlayer.start();
        else L.a(CN + "playMedia - not prepared !!!");
    }

    public void stopMedia() {
        if (mediaPlayer.isPlaying()) mediaPlayer.stop();
        else L.l(CN + "playMedia - trying to stop while not playing");
    }

}