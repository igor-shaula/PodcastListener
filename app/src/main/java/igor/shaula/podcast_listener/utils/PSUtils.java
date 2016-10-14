package igor.shaula.podcast_listener.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * shared methods container \
 * <p>
 * PSUtils = Public Static Utility methods \
 */
public class PSUtils {

    private static final String CN = "PSUtils ` ";

    // created for avoiding code duplication in two network state checks \
    private static NetworkInfo getNetworkInfo(Context context) {
        if (context == null) {
            L.a(CN + "context is null !!!");
            return null;
        }
        // now we assume that context is valid \
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // i assume that connectivityManager cannot be null - because system service exists always \
        return connectivityManager.getActiveNetworkInfo();
    }

    // universal check of internet availability \
    public static boolean isInternetEnabled(Context context) {

        NetworkInfo networkInfo = getNetworkInfo(context);
        // simplified style of writing - I like this \
        return networkInfo != null && networkInfo.isConnected()
                && (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE
                || networkInfo.getType() == ConnectivityManager.TYPE_MOBILE_DUN
                || networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET
                || networkInfo.getType() == ConnectivityManager.TYPE_WIMAX
                || networkInfo.getType() == ConnectivityManager.TYPE_WIFI);
    }

    // check for detection of WiFi state - files and streams will be got only over WiFi \
    public static boolean isHighSpeedAvailable(Context context) {

        NetworkInfo networkInfo = getNetworkInfo(context);
        return networkInfo != null && networkInfo.isConnected()
                && (networkInfo.getType() == ConnectivityManager.TYPE_WIFI
                || networkInfo.getType() == ConnectivityManager.TYPE_WIMAX);
    }

    // crazy simple magic method - it finds my already launched service among others \
    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) return true;
        }
        return false;
    }
}