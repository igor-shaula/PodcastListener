package i_will_pass.to_final_of.devchallenge_x.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import i_will_pass.to_final_of.devchallenge_x.R;
import i_will_pass.to_final_of.devchallenge_x.services.MediaPlayerService;
import i_will_pass.to_final_of.devchallenge_x.utils.L;
import i_will_pass.to_final_of.devchallenge_x.utils.PSF;
import i_will_pass.to_final_of.devchallenge_x.utils.PSUtils;

/**
 * represents and manages second screen of UI - details of chosen podcast \
 * also controls dedicated media-service, which is doing the main job of this application \
 */
public class DetailActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String CN = "DetailActivity ` ";

    private static final String MAIN_IMAGE_URL_START = "https://radio-t.com/images/radio-t/rt";
    private static final String MAIN_IMAGE_URL_END = ".jpg";

    private String mediaContentUrl;

//    private MediaPlayer mediaPlayer;

//    private boolean prepared;

    private boolean isPlaying;
    private Button bMute;
    private Button bPlayPause;
    private Button bStop;

    private MediaPlayerService mService;
    boolean mBound = false;

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // all starting findViewById & getIntent & Glide's job & setText are placed in this method \
        setInitialViewsAndListeners();

        // TODO: 17.09.2016 add MediaController and make visible controls on screen \
        MediaController mediaController = new MediaController(this, false);
        mediaController.setAnchorView(findViewById(R.id.llPlayerControls));
    }

/*
    @Override
    protected void onStart() {
        super.onStart();
        // we need explicit link for onStop and other possible controlling methods \
        mediaPlayer = new MediaPlayer();
        prepareMediaPlayer(mediaPlayer);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // stopping and releasing all media resources here \
        if (mediaPlayer.isPlaying())
            mediaPlayer.stop();
        mediaPlayer.release();
        // explicitly telling GC to clean this heavy container with already useless data \
        mediaPlayer = null;
    }
*/

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.bMute:

                break;
            case R.id.bRewind:

                break;
            case R.id.bPlayPause:
                if (isPlaying) {
                    bPlayPause.setBackgroundResource(R.drawable.icon_media_play_blue);
                    mService.stopMedia(); // NullPointerException
                } else {
                    bPlayPause.setBackgroundResource(R.drawable.icon_media_pause_blue);
                    Intent intent = new Intent(this, MediaPlayerService.class)
                            .putExtra(PSF.IE_MEDIA_CONTENT_URL, mediaContentUrl);

                    if (PSUtils.isMyServiceRunning(this, MediaPlayerService.class)) {

                        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
                        mService.playMedia();
                        L.l(CN + "bPlayPause - service is already running");

                    } else {
                        // media will be played as soon as player is prepared in the service \
                        if (mediaContentUrl != null) {
                            startService(intent);
                        } else
                            L.a(CN + "mediaContentUrl is null before starting service !!!");
                    }
                }
                // as our view is changed anyway - we have to update the flag for a new click \
                isPlaying = !isPlaying;
                break;
            case R.id.bForward:

                break;
            case R.id.bStop:
                // Unbind from the service
                if (mBound) {
                    unbindService(serviceConnection);
                    mBound = false;
                }
                break;
        }
    } // end of onClick-method \\

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
        bMute = (Button) findViewById(R.id.bMute);
        Button bRewind = (Button) findViewById(R.id.bRewind);
        bPlayPause = (Button) findViewById(R.id.bPlayPause);
        Button bForward = (Button) findViewById(R.id.bForward);
        bStop = (Button) findViewById(R.id.bStop);

        String title = getIntent().getStringExtra(PSF.IE_TITLE);
        String link = getIntent().getStringExtra(PSF.IE_LINK);
        String pubDate = getIntent().getStringExtra(PSF.IE_PUB_DATE);
        String summary = getIntent().getStringExtra(PSF.IE_SUMMARY);
        mediaContentUrl = getIntent().getStringExtra(PSF.IE_MEDIA_CONTENT_URL);
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

        // quick way of getting link for the picture - no time for additional site parsing \
        String podCastIndex = link.substring(link.length() - 4, link.length() - 1);
        String mainImageUrl = MAIN_IMAGE_URL_START + podCastIndex + MAIN_IMAGE_URL_END;
//        String mainImageUrl = "https://radio-t.com/images/radio-t/rt510.jpg"; // example
        L.l(CN + "podCastIndex  = " + podCastIndex);

        // i've chosen Glide because it's faster then Picasso and it's recommended by Google \
        Glide.with(this)
                .load(mainImageUrl)
                .placeholder(R.drawable.placeholder_waiting)
                .crossFade(2000)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .error(R.drawable.placeholder_error)
                .into(ivMainPicture);

        tvTitle.setText(title);
        tvLink.setText(link);
        tvPubDate.setText(pubDate);
        tvSummary.setText(summary);

        bMute.setOnClickListener(this);
        bRewind.setOnClickListener(this);
        bPlayPause.setOnClickListener(this);
        bForward.setOnClickListener(this);
        bStop.setOnClickListener(this);
    } // end of setInitialViewsAndListeners-method \\

/*
    private void prepareMediaPlayer(MediaPlayer mediaPlayer) {

        mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
                L.l(CN + "onInfo: i = " + i + " , i1 = " + i1);
                return false;
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                L.e(CN + "onError: i = " + i + " , i1 = " + i1);
                prepared = false;
                return false;
            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                L.e(CN + "after onPrepared: " + System.currentTimeMillis());
                prepared = true;

                mediaPlayer.start();
            }
        });

        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(mediaContentUrl);

            // i'd like to measure time of preparing stream \
            L.e(CN + "before onPrepared: " + System.currentTimeMillis());
            mediaPlayer.prepareAsync();
            // when this step is done - onPrepared will be called \

        } catch (IOException e) {
            e.printStackTrace();
        }
    } // end of prepareMediaPlayer-method \\
*/


}