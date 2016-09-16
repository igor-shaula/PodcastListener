package i_will_pass.to_final_of.devchallenge_x.utils;

import android.util.Log;

/**
 * useful and minimalistic wrapper for system Log \
 * <p/>
 * in every class where i use it - constant field CN is present - this is for ClassName \
 */
public class L {

    private static final String TAG = "APP";

    // L.l("message") - this is main & typical call for logger in my code - i just like it \
    public static void l(String message) {
        Log.i(TAG, message);
    }

    // L.e("exception") - for logging all exceptions and errors \
    public static void e(String message) {
        Log.e(TAG, message);
    }

    // L.a("assert") - for things that should not ever happen \
    public static void a(String message) {
        Log.wtf(TAG, message);
    }

    // simplest and fastest - even without TAG - may be used to measure speed of doing job \
    public static void f(String message) {
        System.out.println(message);
    }
}