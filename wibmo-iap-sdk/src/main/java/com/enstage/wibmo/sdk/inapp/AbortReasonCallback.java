package com.enstage.wibmo.sdk.inapp;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Akshathkumar Shetty on 14/09/16.
 */

public interface AbortReasonCallback {
    void onSelection(Context context, int requestCode, int resultCode, Intent data, String abortReasonCode,String abortReasonName);
}
