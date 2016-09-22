package com.enstage.wibmo.sdk.inapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.enstage.wibmo.sdk.R;
import com.enstage.wibmo.sdk.WibmoSDKConfig;
import com.enstage.wibmo.sdk.inapp.pojo.InAppCancelReason;
import com.enstage.wibmo.util.HttpUtil;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by nithyak on 16/09/16.
 */

public class InAppCancelReasonHelper {
    private static final String TAG = "InAppCancelHelper";

    private static final String IAP_REASON_FOR_CANCEL_FILE = "IAP_CANCEL_REASON";
    private static final String PREFS_WIBMO_IAP_SDK = "IAP_SDK_PREF";
    private static final String PREF_HAS_ALARM_IAP_CANCEL_CODES = "IAP_REASON_FOR_CANCEL_FILE";

    private static WeakReference<ArrayList<InAppCancelReason>> reasonsForIAPCancelData;

    public static void loadReasonIfReq(final Context context) {
        ArrayList<InAppCancelReason> myList = InAppCancelReasonHelper.getReasonsForIAPCancelData();
        if(myList!=null) {
            return;
        }

        Thread t = new Thread() {
            @Override
            public void run() {
                ArrayList<InAppCancelReason> myNewList = null;

                boolean isAvailableInDisk = InAppCancelReasonHelper.getReasonInDiskFlag(context);
                if (isAvailableInDisk) {
                    myNewList = InAppCancelReasonHelper.getIAPCancelCodesFromDiskCache(context);
                }

                if(myNewList==null) {
                    myNewList = InAppCancelReasonHelper.fetchReasonForIAPCancel(context);
                }

                setReasonsForIAPCancelData(myNewList);
            }
        };
        t.setName("WibmoSDK-getIAPCancelCodes");
        t.setDaemon(true);
        t.start();
    }

    public static ArrayList<InAppCancelReason> fetchReasonForIAPCancel(final Context applicationContext) {
        try {
            String posturl =  WibmoSDKConfig.getWibmoDomain() + "/v2/in/user/abortMessages";

            String rawres = HttpUtil.getDataUseOkHttp(applicationContext, posturl, false);
            if (rawres == null) {
                //Log.e(TAG, "Error: Response is null.");
                return null;
            }

            Type listType = new TypeToken<ArrayList<InAppCancelReason>>() { }.getType();
            rawres = rawres.trim();
            //Log.v(TAG, "rawres: " + rawres);

            ArrayList<InAppCancelReason> cancelReasonList = InAppUtil.makeGson().fromJson(rawres, listType);

            InAppCancelReasonHelper.saveIAPCancelCodesToDisk(applicationContext, cancelReasonList);

            return cancelReasonList;
        } catch (Exception e) {
            Log.e(TAG, "Error: "+e, e);
        }
        return null;
    }

    public static void saveIAPCancelCodesToDisk(Context context, ArrayList<InAppCancelReason> cancelCode) {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(
                    context.openFileOutput(IAP_REASON_FOR_CANCEL_FILE,
                            Context.MODE_PRIVATE));

            oos.writeLong(System.currentTimeMillis());
            oos.writeObject(cancelCode);

            setReasonInDiskFlag(context, true);
        } catch(IOException e) {
            Log.e(TAG, "Error: "+e,e);
        } finally {
            if(oos!=null) {
                try {
                    oos.close();
                } catch (IOException ex) {
                    Log.e(TAG, "Error: "+ex,ex);
                }
            }
        }
    }

    public static ArrayList<InAppCancelReason> getIAPCancelCodesFromDiskCache(Context context) {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(context.openFileInput(IAP_REASON_FOR_CANCEL_FILE));

            long daysBetweenDates = getDaysBetweenDates(new Date(ois.readLong()));
            int daysValidFor = context.getResources().getInteger(R.integer.days_valid_for_caching_IAP_cancel_codes);

            //Log.v(TAG, "Difference in Dates: "+daysBetweenDates+"///"+daysValidFor);
            if(daysBetweenDates > daysValidFor){
                return null;
            }

            return (ArrayList<InAppCancelReason>) ois.readObject();
        } catch(Exception e) {
            Log.e(TAG, "Error: "+e, e);
        } finally {
            if(ois!=null) {
                try {
                    ois.close();
                } catch (IOException ex) {
                    Log.e(TAG, "Error: "+ex,ex);
                }
            }
        }
        return null;
    }

    public static boolean getReasonInDiskFlag(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFS_WIBMO_IAP_SDK, Context.MODE_PRIVATE);
        return pref.getBoolean(PREF_HAS_ALARM_IAP_CANCEL_CODES, false);
    }

    public static void setReasonInDiskFlag(Context context, boolean flag) {
        SharedPreferences pref = context.getSharedPreferences(PREFS_WIBMO_IAP_SDK, Context.MODE_PRIVATE);
        SharedPreferences.Editor syncContactsEditor = pref.edit();
        syncContactsEditor.putBoolean(PREF_HAS_ALARM_IAP_CANCEL_CODES, flag);
        syncContactsEditor.commit();
    }

    private static long getDaysBetweenDates(Date d2) {
        return TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - d2.getTime());
    }

    public static ArrayList<InAppCancelReason> getReasonsForIAPCancelData() {
        if (reasonsForIAPCancelData != null) {
            return reasonsForIAPCancelData.get();
        } else {
            return null;
        }
    }

    public static void setReasonsForIAPCancelData(ArrayList<InAppCancelReason> reasonsForIAPCancelData) {
        InAppCancelReasonHelper.reasonsForIAPCancelData = new WeakReference<ArrayList<InAppCancelReason>>(reasonsForIAPCancelData);
    }
}
