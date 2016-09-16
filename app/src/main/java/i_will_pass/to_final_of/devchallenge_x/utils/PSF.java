package i_will_pass.to_final_of.devchallenge_x.utils;

/**
 * shared constants container \
 * <p/>
 * PSF = Public Static Final \
 * all constants used in more than one class, are gathered here \
 */
public class PSF {

    public static final String RSS_FEED_URL = "url of the current RSS-feed";
    public static final String RSS_ITEMS_ARRAY = "response from the network";
    public static final String RSS_HEAD_TITLE = "headTitle";
    public static final String RSS_HEAD_LINK = "headLink";
    public static final String RSS_HEAD_SUMMARY = "headSummary";
    //    public static final String RSS_TAG_COUNTER = "tagCounter";
    public static final String N_I_SERVICE = "StartingIntentService";

    public static final String IE_TITLE = "InfoEntity title";
    public static final String IE_LINK = "InfoEntity link";
    public static final String IE_PUB_DATE = "InfoEntity publication date";
    public static final String IE_MEDIA_CONTENT_URL = "InfoEntity media content URL";
    public static final String IE_FILE_SIZE = "InfoEntity file size";
    public static final String IE_TYPE = "InfoEntity type";
    public static final String IE_SUMMARY = "InfoEntity summary";

    public static final String S_ACTIVITY_HASH = "hashCode of current activity instance";

    public static final int R_CODE_SERVICE = 10; // for intent starting StartingIntentService \
    public static final int P_I_SERVICE = 100; // for PendingIntent back from StartingIntentService \
}
/*
// SOME EXPLANATION ================================================================================

i decided to avoid Hungarian Notation here because it's not necessary any longer in modern IDEs \

first of all architecture will be like MVC - based on system components filled with logic \

*/