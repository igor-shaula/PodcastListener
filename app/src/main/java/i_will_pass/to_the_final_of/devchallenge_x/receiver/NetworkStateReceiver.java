package i_will_pass.to_the_final_of.devchallenge_x.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import i_will_pass.to_the_final_of.devchallenge_x.utils.PSUtils;

/**
 * general receiver - reacts on internet availability change and trigger requests \
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    // for linking with other system components \
    public interface ConnectionStateCallback {

        // general network state - for getting lightweight data \
        void onInetStateSwitched(boolean isOn);

        // high speed connection - only for downloading media \
        void onWiFiStateSwitched(boolean isOn);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // as this receiver is registered in activity - context here is activity actually \
        ConnectionStateCallback connectionStateCallback = (ConnectionStateCallback) context;

        // i decided to invoke both methods every time when connectivity changes \
        connectionStateCallback.onInetStateSwitched(PSUtils.isInternetEnabled(context));
        connectionStateCallback.onWiFiStateSwitched(PSUtils.isHighSpeedAvailable(context));
    }
}