package i_will_pass.to_final_of.devchallenge_x.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import i_will_pass.to_final_of.devchallenge_x.R;
import i_will_pass.to_final_of.devchallenge_x.utils.L;
import i_will_pass.to_final_of.devchallenge_x.utils.PSF;

/**
 * represents and manages second screen of UI - details of chosen podcast \
 */
public class DetailActivity extends AppCompatActivity {

    private static final String CN = "DetailActivity ` ";

    private RecyclerView rvComments;

/*
    // container for save-restore this activity data during screen orientation changes \
    private ArrayList<UserComment> userComments;
*/

    // defining handler in main thread - so messages will come here \
    private Handler handler = new Handler();

    // OVERRIDDEN CALLBACKS ========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // all starting findViewById & getIntent & setText are placed in this method \
        setViewsForEmptyData();

        // trying to recreate data without asking network for what has already been delivered \
        if (savedInstanceState != null) {
            L.l(CN + "savedInstanceState is not null - trying to restore data without internet");
            // we need to get data for a new instance of this activity that was recreated \
//            userComments = savedInstanceState.getParcelableArrayList(USER_COMMENT_LIST);
        }
//        L.l(CN + "created activity hash = " + hashCode());
    }

/*
    @Override
    protected void onStart() {
        super.onStart();
        // decided to do this job here and not in onCreate because here we deal with visible results \
        if (userComments == null) {
            // of course if data is absent - we have to launch network request right now \
            String mediaId = getIntent().getStringExtra(PSF.MEDIA_ID);
            // either way of processing network job is working well - i left all of them in reserve\

            // 1 - via good old HttpUrlConnection launched from IntentService in its worker thread \
            if (PSUtils.isMyServiceRunning(this, StartingIntentService.class))
                L.l(CN + "instance of StartingIntentService is already working");
            else {
                PendingIntent pendingIntent = createPendingResult(PSF.R_CODE_SERVICE, new Intent(), 0);
*/
/*
                // i also tried this way - but it doesn't work \
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                        PSF.R_CODE_SERVICE, new Intent(), 0);
*//*

                Intent intent = new Intent(this, StartingIntentService.class)
                        .putExtra(PSF.N_I_SERVICE, pendingIntent)
                        .putExtra(PSF.S_POST_URL, BASE_MEDIA_URL + mediaId + COMMENTS_PART + PSF.ACCESS_TOKEN)
                        .putExtra(PSF.S_ACTIVITY_HASH, hashCode());
                startService(intent);
            }
        } else // userComments != null - so we have data and should is it now \
            setViewsForReadyData();
    } // end of onStart-method \\
*/

/*
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // initially I've chosen ArrayList instead of List just to avoid type casting in this method \
        outState.putParcelableArrayList(USER_COMMENT_LIST, userComments);
        // calling this to superclass is needed for saving states of all views with id \
        super.onSaveInstanceState(outState);
        // just for note - onSaveInstanceState gets invoked before onStop \
    }
*/

    @Override
    protected void onStop() {
        super.onStop();
        // clearing handler to avoid potential memory leaks \
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PSF.R_CODE_SERVICE) {
            // ensuring ourselves in fact of taking the response from previous activity's request \
            L.l(CN + "current activity hash = " + hashCode());
            L.l(CN + "sending activity hash = " + data.getIntExtra(PSF.S_ACTIVITY_HASH, 0));

//            processNetworkResponse(data.getStringExtra(PSF.RESPONSE));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // MAIN ACTIONS ================================================================================

    // runs in worker thread - from IntentService or OkHttp or any other network agent \
    private void processNetworkResponse(String response) {

/*
        ResponseParser responseParser = new ResponseParser(PSF.DETAIL_ACTIVITY);
        //noinspection unchecked
        userComments = (ArrayList<UserComment>) responseParser.parse(response);

        // just a precaution \
        if (userComments == null) return;
*/

        // i'm afraid that such work with View may potentially lead to memory leaks \
/*
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setViewsForReadyData();
            }
        });
*/
        // so i decided to use handler myself and clear its callbacks in onStop \
        handler.post(new Runnable() {
            @Override
            public void run() {
//                setViewsForReadyData();
            }
        });
    }

    // invoked only at the very start of this activity - just to clean onCreate from the mess \
    private void setViewsForEmptyData() {
        setContentView(R.layout.activity_detail);

        // getting access to all used views \
        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        TextView tvLink = (TextView) findViewById(R.id.tvLink);
        ImageView ivMainPicture = (ImageView) findViewById(R.id.ivMainPicture);

        // getting passed info from the intent started by previous activity \
        String title = getIntent().getStringExtra(PSF.IE_TITLE);
        String link = getIntent().getStringExtra(PSF.IE_LINK);
        // TODO: 17.09.2016 fix image URL - make it from depending on link \
        String mainImageUrl = "https://radio-t.com/images/radio-t/rt512.jpg";

/*
        as I know - all pictures in Instagram are squares \
        so it is obvious to preset the needed dimensions to avoid redraw and blinking later\
*/

        // preparing the main picture - I decided to redo calculations for every configuration change \
        Point point = new Point();
        // strange method chain with void in the end - it just saves values to given point \
        getWindowManager().getDefaultDisplay().getSize(point);
        int squareSide = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                ? point.x : point.y;
        L.l(CN + "squareSide = " + squareSide);

        if (ivMainPicture != null) {
            // if not LinearLayout.LayoutParams - the app crashes here \
            ivMainPicture.setLayoutParams(new LinearLayout.LayoutParams(squareSide, squareSide));

            // this library does here only one request and than keeps result in special cache \
            Glide.with(this)
                    .load(mainImageUrl)
                    .crossFade()
//                    .error(R.drawable.icon_for_downloading_error)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(ivMainPicture);
        } else L.a(CN + "ivMainPicture == null");

        if (tvTitle != null) tvTitle.setText(title);
        else L.a(CN + "tvTitle == null");

        // preparing recycler - but its adapter will be set later when data is ready \
        rvComments.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    } // end of setViewsForEmptyData-method \\

/*
    // this method gets invoked only if we already have not null data for our recycler \
    private void setViewsForReadyData() {
        // removing busy indicator - now we're ready to show ReyclerView \
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.pbIndicator);
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        // now all data is ready to build the list of it and show it to user \
        rvComments.setAdapter(new CommentsRVAdapter(userComments, DetailActivity.this));
        L.l(CN + "successful end of DetailActivity's starting chain");
    }
*/

}