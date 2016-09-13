package i_will_pass.to_the_final_of.devchallenge_x.networking;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import i_will_pass.to_the_final_of.devchallenge_x.utils.L;
import i_will_pass.to_the_final_of.devchallenge_x.utils.PSF;

/**
 * asks RSS-feed for data and parses it - all job is done in a worker thread \
 */
public class NetworkIntentService extends IntentService {

    public static final String CN = "NetworkIntentService ` ";

    private static final String TAG_WHERE = "item"; // we're currently interested only in those tags \
    private static final String TAG_WHAT = "title"; // names are the only needed tags inside streams tags \

    // default constructor is required here by manifest \
    public NetworkIntentService() {
        // giving name to our worker thread - it's important only for debugging \
        super(CN.replace(' ', '`'));
        L.l(CN + CN.replace(' ', '`'));
        // i decided not to place chars to constants - just trying to keep simplicity \
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String requestedUrl = intent.getStringExtra(PSF.RSS_FEED_URL);
        int activityHashCode = intent.getIntExtra(PSF.S_ACTIVITY_HASH, 0);

        L.l(CN + "requestedUrl = " + requestedUrl);

        // all network job is done here \
        String receivedString = new HttpUrlConnAgent().getStringFromWeb(requestedUrl);
        L.l(CN + "receivedString = " + receivedString);

        // now the time of parsing begins \
        String[] result = parseXml(receivedString);

        // opening intent as envelope and getting our PendingIntent to send it back to its activity \
        PendingIntent pendingIntent = intent.getParcelableExtra(PSF.N_I_SERVICE);
        try {
            Intent newIntent = new Intent()
                    .putExtra(PSF.RSS_RESULT, result)
                    .putExtra(PSF.S_ACTIVITY_HASH, activityHashCode);
            pendingIntent.send(this, PSF.P_I_SERVICE, newIntent);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }

//        stopSelf(); // no need to stop the service here - it stops itself by default - it's checked \
    }

    private String[] parseXml(String stringToParse) {

        if (stringToParse == null) {
            L.a(CN + "stringToParse is null !!!");
            return null;
        }

        InputStream inputStream = new ByteArrayInputStream(stringToParse.getBytes());

        // preparing parser for usage \
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parser.nextTag();

            // for adding multiple elements while it's not clear how many of them \
            List<String> stringList = new LinkedList<>();

            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG
                        && parser.getName().equals(TAG_WHERE)) {
                    parser.nextTag();

                    if (parser.getName().equals(TAG_WHAT)) {
                        String content = parser.nextText();

                        stringList.add(content);
                        L.l(CN + "TAG_WHAT: added to stringList = " + content);
                    }
                }
                parser.next();
            }
            String[] stringArray = new String[stringList.size()];
//            for (int i = 0; i < stringList.size(); i++) {
//                stringArray[i] = stringList.get(i);
//            }
            // transporting elements from LinkedList to Array in a single queue \
            int i = 0;
            for (String stringItem : stringList) {
                stringArray[i] = stringItem;
                i++;
            }
            // decided to return the array of strings to fit with intent.putExtra() method later \
            return stringArray;

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // normally it is not supposed to get here and really return null \
        return null;
    } // end of parseXml-method \\
}