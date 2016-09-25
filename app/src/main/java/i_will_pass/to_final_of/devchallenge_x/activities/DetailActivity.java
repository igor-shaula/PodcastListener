package i_will_pass.to_final_of.devchallenge_x.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.Timer;
import java.util.TimerTask;

import i_will_pass.to_final_of.devchallenge_x.R;
import i_will_pass.to_final_of.devchallenge_x.entity.InfoEntity;
import i_will_pass.to_final_of.devchallenge_x.services.MediaPlayerService;
import i_will_pass.to_final_of.devchallenge_x.utils.L;
import i_will_pass.to_final_of.devchallenge_x.utils.PSF;
import i_will_pass.to_final_of.devchallenge_x.utils.PSUtils;

/**
 * represents and manages second screen of UI - details of chosen podcast \
 * also controls dedicated media-service, which is doing the main job of this application \
 */
public class DetailActivity extends AppCompatActivity implements
        View.OnClickListener, MediaPlayerService.CallingComponent {

    private static final String CN = "DetailActivity ` ";
    private static final String TIMELINE_TIMER = "TimelineTimer";

    private static final String MAIN_IMAGE_URL_START = "https://radio-t.com/images/radio-t/rt";
    private static final String MAIN_IMAGE_URL_END = ".jpg";

    // all data needed to start MediaPlayerService and make user happy \
    private InfoEntity infoEntity;

    TextView tvTiming;

    Timer progressTimer;

    SeekBar sbTiming;
    // buttons with changing background \
    private Button bMute;
    private Button bPlayPause;
    private Button bStop;

    private MediaPlayerService mediaPlayerService;
    private boolean serviceBound;

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mediaPlayerService = binder.getService();
            mediaPlayerService.registerCaller(DetailActivity.this);
            serviceBound = true;

            progressTimer = new Timer(TIMELINE_TIMER, true);
            progressTimer.schedule(new TimelineTimerTask(), 0, 1000);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;
        }
    };

    private class TimelineTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String progress = mediaPlayerService.getStringProgress();
                    if (progress == null)
                        tvTiming.setText(getString(R.string.defaultProgress));
                    else
                        tvTiming.setText(progress);
                }
            });
        }
    }

    // ALL CALLBACKS ===============================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // all starting findViewById & getIntent & Glide's job & setText are placed in this method \
        setInitialViewsAndListeners();

        bindMediaPlayerService(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mediaPlayerService != null)
            mediaPlayerService.registerCaller(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        mediaPlayerService.unRegisterCaller(this);
        progressTimer.cancel();
    }

    @Override
    public void onClick(View view) {

        // i just like notifying user about button press immediately \
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(100);
//        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY); // not working

        switch (view.getId()) {

            case R.id.bPlayPause:
                L.l(CN + "bPlayPause pressed, isPlaying = " + mediaPlayerService.isPlaying());
                if (mediaPlayerService.isPlaying())
                    mediaPlayerService.pauseMedia(); // NullPointerException
                else {
                    if (PSUtils.isMyServiceRunning(this, MediaPlayerService.class)) {
                        L.l(CN + "bPlayPause - service is already running");
                        mediaPlayerService.playMedia();
                    } else {
                        L.l(CN + "bPlayPause - service was not running - we'll bind it now");
                        bindMediaPlayerService(true);
                    }
                }
                break;

            case R.id.bStop:
                if (PSUtils.isMyServiceRunning(this, MediaPlayerService.class))
                    mediaPlayerService.stopMedia();
                else L.e(CN + "bStop - nothing to stop = service is not running");

                // unbinding from the service
                if (serviceBound) {
                    unbindService(serviceConnection);
                    serviceBound = false;
//                    mediaPlayerService.unRegisterCaller(this);
                }
                break;

            case R.id.bMute:
                if (mediaPlayerService.isMediaMuted())
                    mediaPlayerService.unMuteMedia();
                else mediaPlayerService.muteMedia();
                break;

            case R.id.bRewind:
                mediaPlayerService.rewindMediaBack();
                break;

            case R.id.bForward:
                mediaPlayerService.rewindMediaForward();
                break;
        }
    } // end of onClick-method \\

    @Override
    public void playbackActive() {
        bPlayPause.setBackgroundResource(R.drawable.icon_media_pause_blue);
        bStop.setBackgroundResource(R.drawable.icon_media_stop);
    }

    @Override
    public void playbackPaused() {
        bPlayPause.setBackgroundResource(R.drawable.icon_media_play_blue);
    }

    @Override
    public void mutingDone() {
        bMute.setBackgroundResource(R.drawable.icon_media_mute_accent);
    }

    @Override
    public void mutingCancelled() {
        bMute.setBackgroundResource(R.drawable.icon_media_mute);
    }

    @Override
    public void stopDone() {
        bStop.setBackgroundResource(R.drawable.icon_media_stop_accent);
        bPlayPause.setBackgroundResource(R.drawable.icon_media_play_blue);
        Toast.makeText(this, "MediaPlayer completely stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateProgress(float value) {
        // works from TimelineTimerTask \
        sbTiming.setProgress((int) value);
        // after that onProgressChanged-method in its listener gets invoked - we have to go there \
    }

    // MAIN ACTIONS ================================================================================

    // invoked only at the very start of this activity - just to clean onCreate from the mess \
    private void setInitialViewsAndListeners() {

        setContentView(R.layout.activity_detail);

        FrameLayout flMainPicture = (FrameLayout) findViewById(R.id.flMainPicture);
        ImageView ivMainPicture = (ImageView) findViewById(R.id.ivMainPicture);
        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        TextView tvLink = (TextView) findViewById(R.id.tvLink);
        TextView tvPubDate = (TextView) findViewById(R.id.tvPubDate);
        TextView tvSummary = (TextView) findViewById(R.id.tvSummary);
        sbTiming = (SeekBar) findViewById(R.id.sbTiming);
        tvTiming = (TextView) findViewById(R.id.tvTiming);
        bMute = (Button) findViewById(R.id.bMute);
        bPlayPause = (Button) findViewById(R.id.bPlayPause);
        bStop = (Button) findViewById(R.id.bStop);
        Button bRewind = (Button) findViewById(R.id.bRewind);
        Button bForward = (Button) findViewById(R.id.bForward);
/*
        as i see for now - all podcast pictures in radio-t.com are small squares 200 x 200 px \
        so i decided to place this picture into container in the top of the screen \
*/
        // preparing the main picture - I decided to redo calculations for every configuration change \
        Point point = new Point();
        // strange method chain with void in the end - it just saves values to given point \
        getWindowManager().getDefaultDisplay().getSize(point);
        int squareSide = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                ? point.x : point.y;
        L.l(CN + "squareSide = " + squareSide);

        flMainPicture.setLayoutParams(new RelativeLayout.LayoutParams(squareSide, squareSide / 2));

        infoEntity = getIntent().getParcelableExtra(PSF.INFO_ENTITY);
        if (infoEntity == null) {
            L.a(CN + "infoEntity is null -> nothing can be done without data");
            return;
        }
        // quick way of getting link for the picture - no time for additional site parsing \
        String link = infoEntity.getLink();
        String podCastIndex = link.substring(link.length() - 4, link.length() - 1);
        String mainImageUrl = MAIN_IMAGE_URL_START + podCastIndex + MAIN_IMAGE_URL_END;
        // example of mainImageUrl: "https://radio-t.com/images/radio-t/rt510.jpg";
        L.l(CN + "podCastIndex  = " + podCastIndex);

        // i've chosen Glide because it's faster then Picasso and it's recommended by Google \
        Glide.with(this)
                .load(mainImageUrl)
                .placeholder(R.drawable.placeholder_waiting)
                .crossFade(2000)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .error(R.drawable.placeholder_error)
                .into(ivMainPicture);

        tvTitle.setText(infoEntity.getTitle());
        tvLink.setText(link);
        tvPubDate.setText(infoEntity.getPubDate());
        tvSummary.setText(infoEntity.getSummary());

        sbTiming.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                L.l(CN + "onProgressChanged: i = " + i + ", b = " + b);
                if (!b) // initiated by setProgress-method, not by user \
                    mediaPlayerService.seekTo(i);
                // user-initiated progress is worked in onStopTrackingTouch - when setting is done \
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                L.l(CN + "onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                L.l(CN + "onStopTrackingTouch");
                // works in case when user sets progress himself - after touch was released \
                mediaPlayerService.seekTo(seekBar.getProgress());
            }
        });

        bMute.setOnClickListener(this);
        bRewind.setOnClickListener(this);
        bPlayPause.setOnClickListener(this);
        bForward.setOnClickListener(this);
        bStop.setOnClickListener(this);
    } // end of setInitialViewsAndListeners-method \\

    private void bindMediaPlayerService(boolean autoStart) {

        Intent intent = new Intent(this, MediaPlayerService.class)
                .putExtra(PSF.INFO_ENTITY, infoEntity)
                .putExtra(PSF.AUTO_START, autoStart);

        // binding, preparing and waiting for start playback \
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
}