package i_will_pass.to_final_of.devchallenge_x.activities;

import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
    public static final String MAIN_IMAGE_URL_START = "https://radio-t.com/images/radio-t/rt";
    public static final String MAIN_IMAGE_URL_END = ".jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // all starting findViewById & getIntent & Glide's job & setText are placed in this method \
        setViewsForEmptyData();
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
    }

    // invoked only at the very start of this activity - just to clean onCreate from the mess \
    private void setViewsForEmptyData() {

        setContentView(R.layout.activity_detail);

        FrameLayout flMainPicture = (FrameLayout) findViewById(R.id.flMainPicture);
        ImageView ivMainPicture = (ImageView) findViewById(R.id.ivMainPicture);
        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        TextView tvLink = (TextView) findViewById(R.id.tvLink);
        TextView tvPubDate = (TextView) findViewById(R.id.tvPubDate);
        TextView tvSummary = (TextView) findViewById(R.id.tvSummary);

        String title = getIntent().getStringExtra(PSF.IE_TITLE);
        String link = getIntent().getStringExtra(PSF.IE_LINK);
        String pubDate = getIntent().getStringExtra(PSF.IE_PUB_DATE);
        String summary = getIntent().getStringExtra(PSF.IE_SUMMARY);
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
    } // end of setViewsForEmptyData-method \\
}