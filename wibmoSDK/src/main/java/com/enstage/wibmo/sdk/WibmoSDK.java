/*
 * Copyright (C) 2014 enStage Inc. Cupertino, California USA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.enstage.wibmo.sdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import com.enstage.wibmo.sdk.inapp.InAppInitActivity;
import com.enstage.wibmo.sdk.inapp.InAppTxnIdCallback;
import com.enstage.wibmo.sdk.inapp.InAppUtil;
import com.enstage.wibmo.sdk.inapp.pojo.W2faInitRequest;
import com.enstage.wibmo.sdk.inapp.pojo.W2faResponse;
import com.enstage.wibmo.sdk.inapp.pojo.WPayInitRequest;
import com.enstage.wibmo.sdk.inapp.pojo.WPayResponse;
import com.enstage.wibmo.util.HttpUtil;

import java.util.List;

public class WibmoSDK {
	private static final String TAG = WibmoSDK.class.getSimpleName();
    public static final String VERSION = "1.4.1";

    public static final String RES_CODE_NO_ERROR = "000";
    public static final String RES_CODE_FAILURE_TIMED_OUT = "203"; //User Timedout
    public static final String RES_CODE_FAILURE_USER_ABORT = "204";
    public static final String RES_CODE_FAILURE_SYSTEM_ABORT = "205";
    public static final String RES_CODE_FAILURE_INTERNAL_ERROR = "051";
    public static final String RES_CODE_TOO_EARLY = "080";

    public static final int REQUEST_CODE_MPOS = 0x0000c0be; // Only use bottom 16 bits - 49342
    public static final int REQUEST_CODE_IAP_2FA  = 0x0000605f; // Only use bottom 16 bits - 24671
    public static final int REQUEST_CODE_IAP_PAY  = 0x00006060; // Only use bottom 16 bits - 24672

	public static final String DEFAULT_TITLE = "Install Wibmo Wallet?";
	public static final String DEFAULT_MESSAGE =
		"This application requires Wibmo Wallet. Would you like to install it?";
	public static final String DEFAULT_YES = "Yes";
	public static final String DEFAULT_NO = "No";

    public static final boolean IS_PHONE_STATE_PERMISSION_REQ = false;

    private static String wibmoIntentActionPackage = "com.enstage.wibmo.sdk.inapp.main";
    private static String wibmoAppPackage;

    public static final String PAYMENT_TYPE_ALL = "*";
    public static final String PAYMENT_TYPE_NONE = "w.ds.pt.none";
    public static final String PAYMENT_TYPE_WALLET_CARD = "w.ds.pt.card_wallet";
    //public static final String PAYMENT_TYPE_VISA_CARD = "w.ds.pt.card_visa";
    //public static final String PAYMENT_TYPE_MASTER_CARD = "w.ds.pt.card_mastercard";

    public static final String TRANSACTION_TYPE_W2FA = "W2fa";
    public static final String TRANSACTION_TYPE_WPAY = "WPay";
    public static final String TRANSACTION_TYPE_EBILL_GENERIC = "WPay-Ebill-Generic";
    public static final String TRANSACTION_TYPE_EBILL_POS = "WPay-Ebill-POS";

    private static InAppTxnIdCallback inAppTxnIdCallback;

    public static void startForInApp(Activity activity, W2faInitRequest w2faInitRequest) {
        if(activity==null) {
            throw new IllegalArgumentException("Activity passed was null");
        }
        if(w2faInitRequest==null) {
            throw new IllegalArgumentException("W2faInitRequest passed was null");
        }

        Intent intent = new Intent(activity, InAppInitActivity.class);
        intent.putExtra("W2faInitRequest", w2faInitRequest);
        activity.startActivityForResult(intent, REQUEST_CODE_IAP_2FA);
    }

    public static void startForInApp(Activity activity, WPayInitRequest wPayInitRequest) {
        Log.i(TAG, "Called startForInApp");
        if(activity==null) {
            throw new IllegalArgumentException("Activity passed was null");
        }
        if(wPayInitRequest==null) {
            throw new IllegalArgumentException("WPayInitRequest passed was null");
        }

        Intent intent = new Intent(activity, InAppInitActivity.class);
        intent.putExtra("WPayInitRequest", wPayInitRequest);
        activity.startActivityForResult(intent, REQUEST_CODE_IAP_PAY);
    }

    public static W2faResponse processInAppResponseW2fa(Intent data) {
        if(data==null) return null;

        W2faResponse response = new W2faResponse();

        response.setResCode(data.getStringExtra(InAppUtil.EXTRA_KEY_RES_CODE));
        response.setResDesc(data.getStringExtra(InAppUtil.EXTRA_KEY_RES_DESC));

        response.setWibmoTxnId(data.getStringExtra(InAppUtil.EXTRA_KEY_WIBMO_TXN_ID));
        response.setDataPickUpCode(data.getStringExtra(InAppUtil.EXTRA_KEY_DATA_PICKUP_CODE));

        response.setMerAppData(data.getStringExtra(InAppUtil.EXTRA_KEY_MER_APP_DATA));
        response.setMerTxnId(data.getStringExtra(InAppUtil.EXTRA_KEY_MER_TXN_ID));

        response.setMsgHash(data.getStringExtra(InAppUtil.EXTRA_KEY_MSG_HASH));

        return response;
    }

    public static WPayResponse processInAppResponseWPay(Intent data) {
        if(data==null) return null;

        WPayResponse response = new WPayResponse();

        response.setResCode(data.getStringExtra(InAppUtil.EXTRA_KEY_RES_CODE));
        response.setResDesc(data.getStringExtra(InAppUtil.EXTRA_KEY_RES_DESC));

        response.setWibmoTxnId(data.getStringExtra(InAppUtil.EXTRA_KEY_WIBMO_TXN_ID));
        response.setDataPickUpCode(data.getStringExtra(InAppUtil.EXTRA_KEY_DATA_PICKUP_CODE));

        response.setMerAppData(data.getStringExtra(InAppUtil.EXTRA_KEY_MER_APP_DATA));
        response.setMerTxnId(data.getStringExtra(InAppUtil.EXTRA_KEY_MER_TXN_ID));

        response.setMsgHash(data.getStringExtra(InAppUtil.EXTRA_KEY_MSG_HASH));

        return response;
    }


	public static boolean isWibmoIAPIntentAppAvailable(Activity activity, String wibmoPackage) {
        //com.enstage.wibmo.sdk.inapp.main.InApp
        String intentAction = wibmoPackage+".InApp";
        Log.v(TAG, "intentAction: "+intentAction);
        Intent intent = new Intent(intentAction);
        //intent.addCategory(Intent.CATEGORY_DEFAULT);

		PackageManager pm = activity.getApplicationContext().getPackageManager();
		List<ResolveInfo> availableApps = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        Log.v(TAG, "availableApps: "+availableApps.size());

        return availableApps.size() > 0;
        /*
        if (availableApps != null) {
			for (ResolveInfo availableApp : availableApps) {
				String packageName = availableApp.activityInfo.packageName;
				if (WIBMO_PACKAGE.equals(packageName)) {
					return packageName;
				}
			}
		}
		return null;
		*/
	}
    /*
    public static boolean isPackageExisted2(Activity activity, String targetPackage){
        List<ApplicationInfo> packages;
        PackageManager pm = activity.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if(packageInfo.packageName.equals(targetPackage)) return true;
        }
        return false;
    }
    */
    public static boolean isPackageExisted(Activity activity, String targetPackage){
        PackageManager pm = activity.getApplicationContext().getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

	public static AlertDialog showDownloadDialog(final Activity activity) {
		AlertDialog.Builder downloadDialog = new AlertDialog.Builder(activity);

		String title = DEFAULT_TITLE;
		String message = DEFAULT_MESSAGE;
		String buttonYes = DEFAULT_YES;
		String buttonNo = DEFAULT_NO;

		downloadDialog.setTitle(title);
		downloadDialog.setMessage(message);
        downloadDialog.setCancelable(false);
		downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				Uri uri = Uri.parse("market://details?id=" + getWibmoAppPackage());
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				try {
					activity.startActivity(intent);
				} catch (ActivityNotFoundException anfe) {
					// Hmm, market is not installed
					Log.w(TAG, "Android Market is not installed; cannot install Wibmo");
				}
			}
		});
		downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
			}
		});
		return downloadDialog.show();
	}

    /**
     * @deprecated use getWibmoIntentActionPackage
     * @return
     */
    public static String getWibmoPackage() {
        return getWibmoIntentActionPackage();
    }
    /**
     * @deprecated use setWibmoIntentActionPackage
     * @return
     */
    public static void setWibmoPackage(String wibmoPackage) {
        setWibmoIntentActionPackage(wibmoPackage);
    }

    public static String getWibmoIntentActionPackage() {
        return wibmoIntentActionPackage;
    }

    public static void setWibmoIntentActionPackage(String wibmoPackage) {
        Log.i(TAG, "WibmoIntentActionPackage: "+wibmoPackage);
        if(wibmoPackage.indexOf(".sdk.")==-1) {
            throw  new IllegalArgumentException("Wibmo intent package name is wrong ["+wibmoPackage+"]!! Please contact support!");
        }
        if(wibmoPackage!=null) {
            WibmoSDK.wibmoIntentActionPackage = wibmoPackage;
        } else {
            WibmoSDK.wibmoIntentActionPackage = "com.enstage.wibmo.sdk.inapp.main";
        }
    }

    public static void init(Context context) {
        HttpUtil.init(context);
    }

    public static String getWibmoAppPackage() {
        return wibmoAppPackage;
    }

    public static void setWibmoAppPackage(String wibmoAppPackage) {
        Log.i(TAG, "wibmoAppPackage: "+wibmoAppPackage);
        WibmoSDK.wibmoAppPackage = wibmoAppPackage;
    }

    public static InAppTxnIdCallback getInAppTxnIdCallback() {
        return inAppTxnIdCallback;
    }

    public static void setInAppTxnIdCallback(InAppTxnIdCallback inAppTxnIdCallback) {
        WibmoSDK.inAppTxnIdCallback = inAppTxnIdCallback;
    }
}
