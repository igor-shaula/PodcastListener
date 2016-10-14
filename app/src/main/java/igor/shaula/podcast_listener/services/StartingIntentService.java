package igor.shaula.podcast_listener.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import igor.shaula.podcast_listener.R;
import igor.shaula.podcast_listener.entity.InfoEntity;
import igor.shaula.podcast_listener.networking.HttpUrlConnAgent;
import igor.shaula.podcast_listener.utils.L;
import igor.shaula.podcast_listener.utils.PSF;

/**
 * asks RSS-feed for data and parses what was received - all job is done in a worker thread \
 */
public class StartingIntentService extends IntentService {

    private static final String CN = "StartingIntentService ` ";

    private static final String TAG_ITEM = "item"; // we're currently interested only in those tags \
    private static final String TAG_TITLE = "title"; // names are the only needed tags inside streams tags \
    private static final String TAG_LINK = "link";
    private static final String TAG_PUB_DATE = "pubDate";
    private static final String TAG_MEDIA_CONTENT = "media:content";
    private static final String ATTR_URL = "url";
    private static final String ATTR_FILESIZE = "filesize";
    private static final String ATTR_TYPE = "type";
    private static final String TAG_SUMMARY = "itunes:summary";

    // for saving headers of current RSS-feed \
    private Map<String, String> headersMap = new HashMap<>();

//    private int tagCounter;

    // default constructor is required here by manifest \
    public StartingIntentService() {
        // giving name to our worker thread - it's important only for debugging \
        super(CN.replace(' ', '`'));
        // i decided not to place chars to constants - just trying to keep simplicity \
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String requestedUrl = intent.getStringExtra(PSF.RSS_FEED_URL);
        int activityHashCode = intent.getIntExtra(PSF.S_ACTIVITY_HASH, 0);

        L.l(CN + "requestedUrl = " + requestedUrl);

        // all network job is done here \
        String receivedString = new HttpUrlConnAgent().getStringFromWeb(requestedUrl);

        String headTitle = null;
        String headLink = null;
        String headSummary = null;

        InfoEntity[] infoEntities = new InfoEntity[0];

        if (receivedString == null) {
            // show user that this URL is wrong or perhaps internet is OFF \
            headTitle = getString(R.string.failedTitle);
            headLink = requestedUrl;
            headSummary = getString(R.string.failedSummary);
        } else {
            // now the time of parsing begins - headersMap is filled-up here \
            infoEntities = parseXml(receivedString);

            // in purpose of logging only \
            if (infoEntities != null)
//            L.l(CN + "parsed entities = " + infoEntities.length);
                for (InfoEntity infoEntity : infoEntities)
                    L.l(CN + infoEntity);

            if (!headersMap.isEmpty()) {
                headTitle = headersMap.get(TAG_TITLE);
                headLink = headersMap.get(TAG_LINK);
                headSummary = headersMap.get(TAG_SUMMARY);
            }
        }
        // opening intent as envelope and getting our PendingIntent to send it back to its activity \
        PendingIntent pendingIntent = intent.getParcelableExtra(PSF.STARTING_INTENT_SERVICE);
        try {
            Intent newIntent = new Intent()
                    .putExtra(PSF.RSS_HEAD_TITLE, headTitle)
                    .putExtra(PSF.RSS_HEAD_LINK, headLink)
                    .putExtra(PSF.RSS_HEAD_SUMMARY, headSummary)
//                    .putExtra(PSF.RSS_TAG_COUNTER, tagCounter)
                    .putExtra(PSF.RSS_ITEMS_ARRAY, infoEntities)
                    .putExtra(PSF.S_ACTIVITY_HASH, activityHashCode);
            pendingIntent.send(this, PSF.P_I_SERVICE, newIntent);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
//        stopSelf(); // no need to stop the service here - it stops itself by default - it's checked \
    } // end of onHandleIntent-method \\

    // PARSING SECTION =============================================================================

    // converts data in tags into main array of info-objects \
    private InfoEntity[] parseXml(String stringToParse) {
        // null-check is done before this method invocation - on the upper logic level \
/*
        if (stringToParse == null) {
            L.a(CN + "stringToParse is null !!!");
            return null;
        }
*/
        InputStream inputStream = new ByteArrayInputStream(stringToParse.getBytes());
/*
         i use XmlPullParser instead of SAX and others because Google recommends it,
         and else i assume that document from RSS-feed cannot be so huge not to be able
         to get loaded into RAM wholly \ and as well i like the way it works \
*/
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parser.nextTag();

            // for local adding multiple elements while it's not clear how many of them \
            List<InfoEntity> infoEntityList = new LinkedList<>();

            // separates parsing items from headers \
            boolean parsingItem = false;

            // main loop to scan every tag of received XML \
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {

                // after this check we'll process only starting tags, skipping everything else here \
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    // if not start tag - it may be end tag, and if it's TAG_ITEM -> restoring flag \
                    if (parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals(TAG_ITEM))
                        parsingItem = false; // in fact here this mode ends \

                    // moving further to the next loop iteration with initial START_TAG check \
                    parser.next();
                    continue;
                }
/*
                tagCounter++;
                L.l(CN + "OUTER LOOPING TAG # " + tagCounter + " = " + parser.getName());
*/
                // here we previously left only inspection of starting tags \
                if (parser.getName().equals(TAG_ITEM))
                    parsingItem = true;

                // i decided to additionally parse headers to get general info about this RSS-feed \
                if (!parsingItem)
                    parseHeaderTag(parser);
                    // result of work is written to headersMap \
                else {
                    InfoEntity infoEntity = parseItemBlock(parser);
                    if (infoEntity != null)
                        infoEntityList.add(infoEntity);
                }

                parser.next();
            } // end of while-loop \\

            InfoEntity[] infoEntityArray = new InfoEntity[infoEntityList.size()];
/*
            // this way is obvious but inefficient because of long reading of LinkedList every time \
            for (int i = 0; i < stringList.size(); i++) {
                stringArray[i] = stringList.get(i);
            }
*/
            // transporting elements from LinkedList to Array in one pass \
            int i = 0;
            for (InfoEntity infoEntity : infoEntityList) {
                infoEntityArray[i] = infoEntity;
                i++;
            }
            // decided to return the array of strings to fit with intent.putExtra() method later \
            return infoEntityArray;

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
        L.a(CN + "parseXml() returns null");
        return null;
    } // end of parseXml-method \\

    private void parseHeaderTag(XmlPullParser parser) throws XmlPullParserException, IOException {

        // firstly getting headers general info - before items \
        String headTitle;
        String headLink;
        String headSummary;

        switch (parser.getName()) {
            case TAG_TITLE:
                headTitle = parser.nextText();
                headersMap.put(TAG_TITLE, headTitle);
                L.l(CN + "headTitle = " + headTitle);
                break;
            case TAG_LINK:
                headLink = parser.nextText();
                headersMap.put(TAG_LINK, headLink);
                L.l(CN + "headLink = " + headLink);
                break;
            case TAG_SUMMARY:
                headSummary = parser.nextText();
                headersMap.put(TAG_SUMMARY, headSummary);
                L.l(CN + "headSummary = " + headSummary);
                break;
            default:
                L.e(CN + "unidentified tag during parsing head");
                break;
        }
    }

    // inner parsing loop inside outer parsing loop - for every item \
    @SuppressWarnings("ConstantConditions")
    private InfoEntity parseItemBlock(XmlPullParser parser) throws XmlPullParserException, IOException {
        // as we're starting from TAG_ITEM - we have to move to the net tag just now \
        L.l(CN + "INNER ENTERING TAG = " + parser.getName());
        parser.next();

        // secondly parsing all items - we have to build objects from these variables \
        String title = getString(R.string.noTitle);
        String link = getString(R.string.noLink);
        String pubDate = getString(R.string.noPubDate);
        String mediaContentUrl = getString(R.string.noMediaContentUrl);
        long fileSize = 0;
        String type = getString(R.string.noType);
        String summary = getString(R.string.noSummary);

        // for escaping endless loop while not changing obvious logic \
        if (parser.getName() == null)
            return null; // yes, men, sometimes this shit happens - tied with initial parser.next() \

        // we need inner loop here - to work directly inside item-tag and set all its fields at once \
        while (!(parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals(TAG_ITEM))) {

            if (parser.getEventType() != XmlPullParser.START_TAG) {
                parser.next();
                continue;
            }
            L.l(CN + "inner looping tag = " + parser.getName());

            if (parser.getName().equals(TAG_ITEM)) break;

            switch (parser.getName()) {
                case TAG_TITLE:
                    title = parser.nextText();
                    break;
                case TAG_LINK:
                    link = parser.nextText();
                    break;
                case TAG_PUB_DATE:
                    pubDate = parser.nextText();
                    break;
                case TAG_MEDIA_CONTENT:
                    // using general approach for any sequence of attributes ordering \
                    for (int i = 0; i < parser.getAttributeCount(); i++)
                        switch (parser.getAttributeName(i)) {
                            case ATTR_URL:
                                mediaContentUrl = parser.getAttributeValue(i);
                                break;
                            case ATTR_FILESIZE:
                                fileSize = Integer.decode(parser.getAttributeValue(i));
                                break;
                            case ATTR_TYPE:
                                type = parser.getAttributeValue(i);
                                break;
                        } // end of inner switch for attributes \\
                    break;
                case TAG_SUMMARY:
                    summary = parser.nextText();
                    break;
                default:
                    L.e(CN + "unknown tag = " + parser.getName());
                    // having to mimic the job of nextText-method of other cases \
                    parser.next();
                    break;
            } // end of outer switch for tags \\

            // moving to the next tag inside current item block \
            parser.next();
        } // end of while-loop \\

        // special part for repeating charset in the end of every summary description on radio-t \
        String repeatingChars1 = getString(R.string.repeatingChars1);
        String repeatingChars2 = getString(R.string.repeatingChars2);
/*
        // this way seems to be simple, but here we parse String with 3 or 4 passes \
        int repeatingCharsBegin = summary.length();
        if (summary.contains(repeatingChars1)) {
            repeatingCharsBegin = summary.indexOf(repeatingChars1);
        } else if (summary.contains(repeatingChars1))
            repeatingCharsBegin = summary.indexOf(repeatingChars2);
        summary = summary.substring(0, repeatingCharsBegin).trim();
*/
        int uniquePartEnd = summary.length();
        int repeatingChars1Begin = summary.indexOf(repeatingChars1); // first String parsing
        int repeatingChars2Begin;
        // this algorithm may look over-complicated, but it's as optimal and fast as possible \
        if (repeatingChars1Begin > 0) {
            uniquePartEnd = repeatingChars1Begin;
        } else {
            // placed here to avoid excess parsing String \
            repeatingChars2Begin = summary.indexOf(repeatingChars2); // second String parsing
            if (repeatingChars2Begin > 0) {
                uniquePartEnd = repeatingChars2Begin;
            }
        }
        summary = summary.substring(0, uniquePartEnd).trim();

        return new InfoEntity(title, link, pubDate, mediaContentUrl, fileSize, type, summary);
    } // end of parseItemBlock-method \\
}