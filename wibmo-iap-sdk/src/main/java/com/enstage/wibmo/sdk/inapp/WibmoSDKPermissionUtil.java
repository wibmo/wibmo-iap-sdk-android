package com.enstage.wibmo.sdk.inapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Toast;
import com.enstage.wibmo.sdk.R;

/**
 * Created by Akshathkumar Shetty on 19/11/15.
 */
public class WibmoSDKPermissionUtil {
    private static final String TAG = "WibmoSDKPermissionUtil";

    public static final int REQUEST_CODE_ASK_PERMISSION_PHONE_STATE = 10;

    /**
     * Determine whether <em>you</em> have been granted a particular permission.
     *
     * @param permission The name of the permission being checked.
     *
     * @return {@link android.content.pm.PackageManager#PERMISSION_GRANTED} if you have the
     * permission, or {@link android.content.pm.PackageManager#PERMISSION_DENIED} if not.
     *
     * @see android.content.pm.PackageManager#checkPermission(String, String)
     */
    public static int checkSelfPermission(Context context, String permission) {
        try {
            return ActivityCompat.checkSelfPermission(context, permission);
        } catch(Throwable e) {
            Log.e(TAG, "Error: " + e, e);

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.v(TAG, "Return PERMISSION_DENIED");
                return PackageManager.PERMISSION_DENIED;
            } else {
                Log.v(TAG, "Return PERMISSION_GRANTED");
                return PackageManager.PERMISSION_GRANTED;
            }
        }
    }

    public static void showRequestPermissionRationalel(Activity activity,
                                                       String message, final Runnable requestPermissionAction, final Runnable cancelAction) {
        showMessageOKCancel(activity, message,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissionAction.run();
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelAction.run();
                    }
                }
        );
    }

    public static void showMessageOKCancel(Activity activity, String message, DialogInterface.OnClickListener okListener) {
        showMessageOKCancel(activity, message, okListener, null);
    }

    public static void showMessageOKCancel(Activity activity, String message, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.label_ok, okListener)
                .setNegativeButton(R.string.label_cancel, cancelListener)
                .create()
                .show();
    }

    public static void showPermissionMissingUI(final Activity activity, String message) {
        Toast toast = Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_LONG);
        View view = toast.getView();
        view.setBackgroundColor(activity.getApplicationContext().getResources().getColor(R.color.logo_color_orange));
        toast.show();
    }
}
