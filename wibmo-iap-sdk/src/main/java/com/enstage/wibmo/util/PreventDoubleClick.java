package com.enstage.wibmo.util;

import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;

import com.enstage.wibmo.sdk.R;
import com.enstage.wibmo.sdk.inapp.InAppUtil;

/**
 * Created by akshath on 27/08/14.
 */
public class PreventDoubleClick {
    private static final String TAG = "PDC";
    private final long TIME_TO_WAIT = 120000;//120~2m

    private long lastClickTime = 0;

    public void reset() {
        lastClickTime = 0;
    }

    public boolean check(Activity activity) {
        // preventing double, using threshold of
        long etime = SystemClock.elapsedRealtime() - lastClickTime;
        if (etime < 1000){
            Log.w(TAG, "pdc stopped req 1");
            InAppUtil.showToast(activity, activity.getString(R.string.lable_please_wait));
            return false;
        } else if (etime < TIME_TO_WAIT){
            Log.w(TAG, "pdc stopped req 2");
            InAppUtil.showToast(activity, activity.getString(R.string.lable_please_wait));
            return false;
        }
        lastClickTime = SystemClock.elapsedRealtime();
        //preventing double, using threshold of

        return true;
    }
}
