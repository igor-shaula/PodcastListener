package igor.shaula.podcast_listener.utils;

/**
 * shared constants container \
 * <p>
 * PSF = Public Static Final \
 * all constants used in more than one class, are gathered here \
 */
public class PSF {

    public static final String RSS_FEED_URL = "url of the current RSS-feed";
    public static final String RSS_ITEMS_ARRAY = "response from the network";
    public static final String RSS_HEAD_TITLE = "headTitle";
    public static final String RSS_HEAD_LINK = "headLink";
    public static final String RSS_HEAD_SUMMARY = "headSummary";

    public static final String STARTING_INTENT_SERVICE = "StartingIntentService";
    //    public static final String MEDIA_PLAYER_SERVICE = "MediaPlayerService";
    public static final String INFO_ENTITY = "InfoEntity object";
    public static final String AUTO_START = "whether or not start automatic playback";

    public static final String S_ACTIVITY_HASH = "hashCode of current activity instance";

    public static final int R_CODE_SERVICE = 10; // for intent starting StartingIntentService \
    public static final int P_I_SERVICE = 100; // for PendingIntent back from StartingIntentService \
}
/*
// SOME EXPLANATION ================================================================================

i decided to avoid Hungarian Notation here because it's not necessary any longer in modern IDEs \

first of all architecture will be like MVC - based on system components filled with logic \

*/