package igor.shaula.podcast_listener.services;

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

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import igor.shaula.podcast_listener.R;
import igor.shaula.podcast_listener.activities.DetailActivity;
import igor.shaula.podcast_listener.entity.InfoEntity;
import igor.shaula.podcast_listener.utils.L;
import igor.shaula.podcast_listener.utils.PSF;

/**
 * does all job connected with MediaPlayer \
 */
public class MediaPlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener,
        AudioManager.OnAudioFocusChangeListener {

    private static final String CN = "MediaPlayerService ` ";
    private static final String WIFI_TAG = "MediaPlayerService";

    private InfoEntity infoEntity;

    private MediaPlayer mediaPlayer;

    private boolean autoStartPlayback;

    // for allowing to play only after asynchronous onPrepared-method completion \
    private boolean playerPrepared;

    // for allowing to continue playing after changes in onAudioFocusChange-method \
    private boolean mediaWasPlaying;

    // for escaping situation when icon says it's muted but the sound is ON after new playback \
    private boolean mediaMuted;

    // one way flag to avoid exceed actions \
    private boolean askedFirstTime = true;

    private int hoursDelta;

    private int currentMillis;
    private int durationMillis;
    private String durationDateString;

    private WifiManager.WifiLock wifiLock;

    private final IBinder binder = new LocalBinder();

    // list of callers to give us ability to react there on current media player state \
    private List<CallingComponent> callingComponentList = new LinkedList<>();

    private NotificationManager notificationManager;

    public interface CallingComponent {

        void playbackActive();

        void playbackPaused();

        void mutingDone();

        void mutingCancelled();

        void stopDone();

        void updateProgress(float value);
    }

    /**
     * Class used for the client Binder. Because we know this service always
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

    // ALL CALLBACKS ===============================================================================

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.l(CN + "onStartCommand: flags = " + flags + " & startId = " + startId);

//        prepareService(intent); // i decided to use only binding

        return START_NOT_STICKY; // to avoid writing -> if (intent != null) ... else stopSelf();
    } // end of onStartCommand-method \\

    @Override
    public IBinder onBind(Intent intent) {
        L.l(CN + "onBind");

        prepareService(intent);

        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopMedia();

        if (wifiLock.isHeld()) {
            wifiLock.release();
            L.l(CN + "wifiLock released");
        } else L.a(CN + "wifiLock was not held when trying to release it");

        stopForeground(true);
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        L.l(CN + "after onPrepared: " + System.currentTimeMillis());
        // on Samsung S3 Duos with Android 4.4 this preparation takes around 1200-1300 milliseconds \
        playerPrepared = true;

        if (autoStartPlayback) playMedia();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        L.e(CN + "onError: i = " + i + " , i1 = " + i1);
        playerPrepared = false;

        stopMedia();

        stopForeground(true);

        return true; // because i already handled this error myself here \
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        L.l(CN + "onInfo: i = " + i + " , i1 = " + i1);

        return false; // as this method is just for observing media logs \
    }

    @Override
    public void onAudioFocusChange(int i) {
        switch (i) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // just resuming playback after potential pause \
                if (mediaPlayer == null) prepareMediaPlayer();
                else playMedia();

                if (mediaWasPlaying) playMedia();
                else L.l(CN + "audio focus gained for player, that was not started yet");

                mediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // focus is lost for an unbounded amount of time -> we have to stop and clean the player \
                stopMedia();
                mediaWasPlaying = true;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // focus is lost for a short time but it is expected to appear soon - no cleaning needed \
                pauseMedia();
                mediaWasPlaying = true;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // focus
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.2f, 0.2f);
                break;
        }
    } // end of onAudioFocusChange-method \\

    // FOR CALLER INTERFACE ========================================================================

    public void registerCaller(CallingComponent callingComponent) {
        callingComponentList.add(callingComponent);
    }

    public void unRegisterCaller(CallingComponent callingComponent) {
        if (callingComponentList.remove(callingComponent))
            L.l(CN + "unRegisterCaller - component removed");
        else L.e(CN + "unRegisterCaller - component NOT removed - it was absent");
    }

    // PREPARATIONS ================================================================================

    private void prepareService(Intent intent) {

        infoEntity = intent.getParcelableExtra(PSF.INFO_ENTITY);
        autoStartPlayback = intent.getBooleanExtra(PSF.AUTO_START, false);

        prepareMediaPlayer();

        makeForeground();


    } // end of prepareService-method \\

    // after this method our player is ready to use \
    private void prepareMediaPlayer() {

        // we need explicit link for other possible controlling methods - onDestroy for instance \
        mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(infoEntity.getMediaContentUrl());

            // i'd like to measure time of preparing stream \
            L.l(CN + "before onPrepared: " + System.currentTimeMillis());
            mediaPlayer.prepareAsync();
            // when this step is done - onPrepared will be called \

        } catch (IOException e) {
            e.printStackTrace();
        }

        // setting CPU wake lock ON \
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        // setting WiFi wake lock ON \
        wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, WIFI_TAG);
        wifiLock.acquire();

        // checking audio-focus \
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            L.l(CN + "audio-focus request NOT granted");
        } else L.l(CN + "audio-focus request granted");

    } // end of prepareMediaPlayer-method \\

    // making our service foreground - adding status bar notification \
    private void makeForeground() {

        Intent innerIntent = new Intent(this, DetailActivity.class)
                .putExtra(PSF.INFO_ENTITY, infoEntity);
        PendingIntent pendingIntent = PendingIntent
                .getService(this, 1, innerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

//        Notification.Builder builder = new Notification.Builder(this)
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_icon_play) // obligatory
                .setContentTitle(infoEntity.getTitle()) // obligatory
                .setContentText(infoEntity.getSummary()) // obligatory
                .setPriority(NotificationCompat.PRIORITY_MAX)
//                .setAutoCancel(true)
                .setAutoCancel(false)

                .setTicker(infoEntity.getTitle())
                .setContentIntent(pendingIntent)
                .setOngoing(true);

        Notification notification;
        if (Build.VERSION.SDK_INT > 15) {
            builder.setSubText(getString(R.string.notificationSubText)); // API level 16 and higher
            notification = builder.build();
        } else
            //noinspection deprecation
            notification = builder.getNotification();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(11, notification);
    }

    // MEDIA CONTROLS ==============================================================================
    // these methods are invoked from both this service and binding activity \
    // i decided to add null-checking in every method of this section to avoid any potential crashes \

    public void playMedia() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying() && playerPrepared) {
            mediaPlayer.start();
            for (CallingComponent callingComponent : callingComponentList) {
                callingComponent.playbackActive();
                if (mediaMuted) callingComponent.mutingDone();
                else callingComponent.mutingCancelled();
            }
        } else L.a(CN + "playMedia - trying to start not prepared player");
    }

    public void pauseMedia() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            for (CallingComponent callingComponent : callingComponentList) {
                callingComponent.playbackPaused();
            }
        } else L.a(CN + "pauseMedia - trying to pause while not playing");
    }

    public void stopMedia() {
        // stopping and releasing all media resources here \
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            // if this method was invoked - the user or system had reason to clean resources \
            mediaPlayer.release();
            // explicitly telling GC to clean this heavy container with already useless data \
            mediaPlayer = null;

            notificationManager.cancel(11);

            for (CallingComponent callingComponent : callingComponentList) {
                callingComponent.stopDone();
            }
        } else L.a(CN + "playMedia - trying to stop while not playing");

        for (CallingComponent callingComponent : callingComponentList) {
            callingComponent.stopDone();
        }
    }

    public void muteMedia() {
        if (mediaPlayer != null) {
/*
            // at first - saving previous volume values \
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            // this is useless - mediaPlayer.setVolume(previousVolume, previousVolume); doesn't work \
*/
            mediaPlayer.setVolume(0.0f, 0.0f);

            for (CallingComponent callingComponent : callingComponentList) {
                callingComponent.mutingDone();
            }
        } else L.a(CN + "muteMedia - player is null");

        mediaMuted = true;
    }

    public void unMuteMedia() {
        if (mediaPlayer != null) {

            mediaPlayer.setVolume(1.0f, 1.0f);
//            mediaPlayer.setVolume(previousVolume, previousVolume); // strange - this doesn't work \

            for (CallingComponent callingComponent : callingComponentList) {
                callingComponent.mutingCancelled();
            }
        } else L.a(CN + "unMuteMedia - player is null");

        mediaMuted = false;
    }

    public void rewindMediaBack() {
        if (mediaPlayer != null) {
            // either to the previous supplied time label or 10% back if it's absent \
            long delta = durationMillis / 10;
            int newMillis = (int) (currentMillis - delta);
            // for not exceeding the left edge of timeline \
            if (newMillis > (int) delta) mediaPlayer.seekTo(newMillis);
            else mediaPlayer.seekTo(0);
        } else L.a(CN + "rewindMediaBack - player is null");
    }

    public void rewindMediaForward() {
        if (mediaPlayer != null) {
            // either to the next supplied time label or 10% forward if it's absent \
            long delta = durationMillis / 10;
            int newMillis = (int) (currentMillis + delta);
            // for not exceeding the right edge of timeline \
            if (newMillis < (int) (durationMillis - delta)) mediaPlayer.seekTo(newMillis);
            else mediaPlayer.seekTo(durationMillis);
        } else L.a(CN + "rewindMediaForward - player is null");
    }

    // useful for avoiding flags for checking media controls state in calling activity \
    public boolean isPlaying() {
        if (mediaPlayer != null)
            return mediaPlayer.isPlaying();
        else {
            L.e(CN + "isPlaying - player is null");
            return false;
        }
    }

    public boolean isMediaMuted() {
        return mediaMuted;
    }

    // gets invoked every second \
    public String getStringProgress() {
        if (mediaPlayer != null && playerPrepared) {
            // i noticed that time got is shifted by constant number of hours, so i'll fix this now \

            currentMillis = mediaPlayer.getCurrentPosition();
            Date currentDate = new Date(currentMillis);
            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTime(currentDate);

//            DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
            DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

            int currentHours = currentCalendar.get(Calendar.HOUR);

            if (askedFirstTime) {

                durationMillis = mediaPlayer.getDuration();
                Date durationDate = new Date(durationMillis);
                Calendar durationCalendar = Calendar.getInstance();
                durationCalendar.setTime(durationDate);

                int durationHours = durationCalendar.get(Calendar.HOUR);
                L.l(CN + "durationHours were = " + durationHours);
                L.l(CN + "currentHours were = " + currentHours);

                if (currentHours != 0) hoursDelta = currentHours;

                durationCalendar.set(Calendar.HOUR_OF_DAY, durationHours - hoursDelta);
                durationDate = durationCalendar.getTime();
                // this string is kept while current podcast activity lives \
                durationDateString = formatter.format(durationDate);
            }

            currentCalendar.set(Calendar.HOUR, currentHours - hoursDelta);
            currentDate = currentCalendar.getTime();
            String currentDateString = formatter.format(currentDate);

            // my be this mechanical shift was not beautiful, but i simply have no time to search \

            askedFirstTime = false;
//            L.l(CN + "currentMillis = " + currentMillis + " & durationMillis = " + durationMillis);
            float proportion = ((float) currentMillis / (float) durationMillis) * 100;
//            L.l(CN + "proportion = " + proportion);

            for (CallingComponent callingComponent : callingComponentList) {
                callingComponent.updateProgress(proportion);
            }
            // TODO: 18.09.2016 fix this - updateProgress does NOT work properly !!! \

            return String.valueOf(currentDateString + " / " + durationDateString);
        } else {
//            L.e(CN + "getStringProgress - player is null");
            return null;
        }
    } // end of getStringProgress-method \\

    public void seekTo(int progress) {
        // by default the maximum value is set to 100 - so here we are working with percentage \
        mediaPlayer.seekTo(durationMillis * progress / 100);
    }
}