package i_will_pass.to_the_final_of.devchallenge_x.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * shared methods container \
 * <p/>
 * PSUtils = Public Static Utility methods \
 */
public class PSUtils {

    // universal check - I decided to use only WiFi for nice look and feel of processing pictures \
    public static boolean isInternetEnabled(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        // i assume that connectivityManager cannot be null by default - because system service exists always \
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        // simplified style of writing - I like this \
        return networkInfo != null && networkInfo.isConnected()
                && (networkInfo.getType() == ConnectivityManager.TYPE_WIFI
                || networkInfo.getType() == ConnectivityManager.TYPE_MOBILE
                || networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET);
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
// UNUSED SNIPPETS =================================================================================
/*
        if (Build.VERSION.SDK_INT >= 15) {
            // use facebook SDK here \
        } else {
            // currently it's unclear about what to do in this situation \
        }
*/
/*
        // used this because standard way via cmd & keytool gave wrong results \
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "shaula.igor.test_facebook",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                L.l("key hash = " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            // NOP
        }
*/