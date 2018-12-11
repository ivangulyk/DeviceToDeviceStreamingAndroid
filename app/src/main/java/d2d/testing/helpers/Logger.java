package d2d.testing.helpers;

import android.util.Log;

public class Logger {
    public static final String TAG = "WIFI-P2P-NETWORK";

    public static void v(String str) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, str);
        }
    }
    public static void v(String str, Throwable t) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, str, t);
        }
    }

    public static void d(String str) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, str);
        }
    }
    public static void d(String str, Throwable t) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, str, t);
        }
    }
}
