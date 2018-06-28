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
package com.enstage.wibmo.sdk.inapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.enstage.wibmo.sdk.R;
import com.enstage.wibmo.sdk.WibmoSDK;
import com.enstage.wibmo.sdk.WibmoSDKConfig;
import com.enstage.wibmo.sdk.inapp.pojo.W2faInitRequest;
import com.enstage.wibmo.sdk.inapp.pojo.W2faInitResponse;
import com.enstage.wibmo.sdk.inapp.pojo.WPayInitRequest;
import com.enstage.wibmo.sdk.inapp.pojo.WPayInitResponse;
import com.enstage.wibmo.util.AnalyticalUtil;

import java.util.HashMap;
import java.util.Map;

import static com.enstage.wibmo.sdk.inapp.InAppHandler.getPkgForIapRestrict;

/**
 * Created by akshath on 17/10/14.
 */
public class InAppInitActivity extends Activity {
    private static final String TAG = "sdk.InAppInit";

    public static final int REQUEST_CODE_IAP_READY = 0x00006061; // Only use bottom 16 bits

    private View view = null;

    private Activity activity = null;

    private String qrMsg;
    private W2faInitRequest w2faInitRequest;
    private WPayInitRequest wPayInitRequest;
    private W2faInitResponse w2faInitResponse;
    private WPayInitResponse wPayInitResponse;
    private View mainView;

    private boolean isAppReady;
    private static String readyPackage;
    private boolean isCheckAppDetails;

    private AsyncTask asyncTask = null;

    private Map<String, Object> extraDataReporting;
    private boolean isReTry;

    private String abortReasonCode;
    private String abortReasonName;

    private long startTime;
    private long endTime;
    private boolean autoSubmitIAPTxn;
    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "OnCreate");
        activity = this;
        startTime = System.currentTimeMillis();

        InAppUtil.addBreadCrumb(InAppUtil.BREADCRUMB_InAppInitActivity);
        isReTry = false;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            w2faInitRequest = (W2faInitRequest) extras
                    .getSerializable("W2faInitRequest");
            wPayInitRequest = (WPayInitRequest) extras
                    .getSerializable("WPayInitRequest");
            autoSubmitIAPTxn = extras.getBoolean("autoSubmitIAPTxn", false);

            //reset
            w2faInitResponse = null;
            wPayInitResponse = null;
            isCheckAppDetails = extras.getBoolean("checkPVStatus", false);

            if (isCheckAppDetails == false) {
                if (w2faInitRequest != null || wPayInitRequest != null) {
                    qrMsg = "InApp payment";
                } else {
                    Log.e(TAG, "W2faInitRequest and wPayInitRequest was null!");
                    //add event if needed
                    sendAbort(WibmoSDK.RES_CODE_FAILURE_SYSTEM_ABORT, "sdk init - W2faInitRequest and wPayInitRequest was null!");
                    return;
                }
            }
        }

        preferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        if(isCheckAppDetails == false) {

            //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
            //setProgressBarIndeterminateVisibility(false);

            LayoutInflater inflator = getLayoutInflater();
            view = inflator.inflate(R.layout.activity_inapp_init, null, false);
            view.startAnimation(AnimationUtils.loadAnimation(this,
                    android.R.anim.slide_in_left));
            setContentView(view);

            TextView text = (TextView) findViewById(R.id.text);
            text.setText(qrMsg);

            mainView = (View) findViewById(R.id.main_view);


            final Activity activity = this;

            Button abortButton = (Button) findViewById(R.id.abort_button);
            abortButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelIAP();
                    sendAbort();
                }
            });


            if (WibmoSDK.IS_PHONE_STATE_PERMISSION_REQ) {
                if (WibmoSDKPermissionUtil.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "Permission not granted! READ_PHONE_STATE");

                    WibmoSDKPermissionUtil.showRequestPermissionRationalel(activity,
                            getString(R.string.wibmosdk_phone_state_permission_rationale),
                            new Runnable() {
                                @Override
                                public void run() {
                                    ActivityCompat.requestPermissions(activity,
                                            new String[]{Manifest.permission.READ_PHONE_STATE},
                                            WibmoSDKPermissionUtil.REQUEST_CODE_ASK_PERMISSION_PHONE_STATE);
                                }
                            },
                            new Runnable() {
                                @Override
                                public void run() {
                                    WibmoSDKPermissionUtil.showPermissionMissingUI(activity, getString(R.string.wibmosdk_phone_state_permission_missing_msg));
                                    sendAbort(WibmoSDK.RES_CODE_FAILURE_SYSTEM_ABORT, "sdk init - no permission ph state");
                                }
                            });

                    return;//we need this so can;t go on
                }
            }

            if (savedInstanceState != null) {
                if (savedInstanceState.getBoolean("wasSaved", false)) {
                    Log.i(TAG, "was restored activity.. lets stop here");
                    return;
                }
            }
        }
        doIAPStuff();
    }

    private void doIAPStuff() {
        Log.v(TAG, "doIAPStuff");
            if(isCheckAppDetails == false) {
                isAppReady = false;
                startIAP();
            } else {
                Intent intent = new Intent();
                intent.putExtra("isPhoneVerified", false);
                intent.putExtra("isAppInstalled", false);
                setResult(REQUEST_CODE_IAP_READY, intent);
                finish();
                return;
            }
    }

    private void cancelIAP() {
        if(asyncTask!=null) {
            asyncTask.cancel(true);
            asyncTask = null;
        }
    }

    private void startIAP() {
        if(WibmoSDKConfig.isPromptAbortReason()) {
            InAppCancelReasonHelper.loadReasonIfReq(getApplicationContext());
        }

        if (w2faInitRequest != null) {
            w2faInitRequest.setDeviceInfo(InAppUtil.makeDeviceInfo(activity, WibmoSDK.VERSION));
            w2faInitRequest.getDeviceInfo().setAppInstalled(isAppReady);
            asyncTask = new Init2FAReqTask().execute(w2faInitRequest);
            return;
        }

        if (wPayInitRequest != null) {
            wPayInitRequest.setDeviceInfo(InAppUtil.makeDeviceInfo(activity, WibmoSDK.VERSION));
            wPayInitRequest.getDeviceInfo().setAppInstalled(isAppReady);
            asyncTask = new InitPayReqTask().execute(wPayInitRequest);
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == WibmoSDKPermissionUtil.REQUEST_CODE_ASK_PERMISSION_PHONE_STATE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Got permission READ_PHONE_STATE");
                // success!
                doIAPStuff();
            } else {
                Log.w(TAG, "Permission not got! READ_PHONE_STATE");//we need this
                WibmoSDKPermissionUtil.showPermissionMissingUI(activity, getString(R.string.wibmosdk_phone_state_permission_missing_msg));
                sendAbort(WibmoSDK.RES_CODE_FAILURE_SYSTEM_ABORT, "sdk init - no permission ph state");
            }
            return;
        }
    }

    public void startInAppReadinessCheck(WPayInitResponse wPayInitResponse, W2faInitResponse w2faInitResponse) {

        String restrictToProgram = preferences.getString("restrictToProgram", null);

        if (wPayInitResponse != null) {
            if(restrictToProgram != null && restrictToProgram.isEmpty() == false){
                setIAPPackageToRestrict(restrictToProgram);
            } else if(wPayInitResponse.getRestrictedProgram() != null && wPayInitResponse.getRestrictedProgram().isEmpty() == false) {
                setIAPPackageToRestrict(wPayInitResponse.getRestrictedProgram());
            } else {
                goToWibmoProgram(getActivity());
            }

        } else if (w2faInitResponse != null) {
            if(restrictToProgram != null && restrictToProgram.isEmpty() == false){
                setIAPPackageToRestrict(restrictToProgram);
            } else if(w2faInitResponse.getRestrictedProgram() != null && w2faInitResponse.getRestrictedProgram().isEmpty() == false) {
                setIAPPackageToRestrict(w2faInitResponse.getRestrictedProgram());
            } else {
                goToWibmoProgram(getActivity());
            }
        }
        return;
    }

    private void setIAPPackageToRestrict(String program){

        String progVersion = activity.getResources().getString(R.string.prog_ver_main);

        if(WibmoSDK.getWibmoIntentActionPackage().contains(activity.getResources().getString(R.string.prog_ver_staging))) {
            progVersion = activity.getResources().getString(R.string.prog_ver_staging);
        } else if(WibmoSDK.getWibmoIntentActionPackage().contains(activity.getResources().getString(R.string.prog_ver_qa))) {
            progVersion = activity.getResources().getString(R.string.prog_ver_qa);
            WibmoSDK.setWibmoAppPackage(activity.getResources().getString(R.string.qa_pkg));
        } else if(WibmoSDK.getWibmoIntentActionPackage().contains(activity.getResources().getString(R.string.prog_ver_dev))){
            progVersion = activity.getResources().getString(R.string.prog_ver_dev);
            WibmoSDK.setWibmoAppPackage(activity.getResources().getString(R.string.dev_pkg));
        }

        switch (program) {

            case "6019":
                if(progVersion.equals(activity.getResources().getString(R.string.prog_ver_main))) {
                    WibmoSDK.setWibmoAppPackage(activity.getResources().getString(R.string.payzapp_pkg));
                } else if(progVersion.equals(activity.getResources().getString(R.string.prog_ver_staging))){
                    WibmoSDK.setWibmoAppPackage(activity.getResources().getString(R.string.payzapp_pkg_staging));
                }

                goToWibmoProgram(activity);
                break;

            case "6022":
                if(progVersion.equals(activity.getResources().getString(R.string.prog_ver_main))) {
                    WibmoSDK.setWibmoAppPackage(activity.getResources().getString(R.string.payapt_pkg));
                } else if(progVersion.equals(activity.getResources().getString(R.string.prog_ver_staging))){
                    WibmoSDK.setWibmoAppPackage(activity.getResources().getString(R.string.payapt_pkg_staging));
                }

                goToWibmoProgram(activity);
                break;

            case "6005":
                if(progVersion.equals(activity.getResources().getString(R.string.prog_ver_main))) {
                    WibmoSDK.setWibmoAppPackage(activity.getResources().getString(R.string.fay_pkg));
                } else if(progVersion.equals(activity.getResources().getString(R.string.prog_ver_staging))){
                    WibmoSDK.setWibmoAppPackage(activity.getResources().getString(R.string.fay_pkg_staging));
                }

                goToWibmoProgram(activity);
                break;

            case "6008":
                if(progVersion.equals(activity.getResources().getString(R.string.prog_ver_main))) {
                    WibmoSDK.setWibmoAppPackage(activity.getResources().getString(R.string.mclip_pkg));
                } else if(progVersion.equals(activity.getResources().getString(R.string.prog_ver_staging))){
                    WibmoSDK.setWibmoAppPackage(activity.getResources().getString(R.string.mclip_pkg_staging));
                }

                goToWibmoProgram(activity);
                break;

            default:
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                String packageName = sharedPref.getString(program, null);

                if(packageName!=null){
                    WibmoSDK.setWibmoAppPackage(packageName);
                    goToWibmoProgram(activity);
                    break;
                } else{
                    asyncTask = new GetPackageNameToRestrictPgm(program).execute();
                    break;
                }
        }
        return;
    }

    private class GetPackageNameToRestrictPgm extends AsyncTask<Void, Void, Void> {
        private boolean showError = false;
        private String lastError;
        private String packageName;
        private String program;

        public GetPackageNameToRestrictPgm(String program) {
            this.program = program;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {

                wPayInitResponse = InAppHandler.initPay(wPayInitRequest);

                if (wPayInitResponse.getRestrictedProgram() != null) {
                    Thread t = new Thread() {
                        public void run() {
                            try {
                                packageName = getPkgForIapRestrict(activity, wPayInitRequest, wPayInitResponse);
                            } catch (Exception e) {
                                Log.e(TAG, "Error: "+e, e);
                            }
                        }
                    };
                    t.setName("InAppTxnIdCallback");
                    t.start();
                }

            } catch (Exception ex) {
                Log.e(TAG, "Error: " + ex, ex);
                lastError = ex.toString();
                showError = true;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            Log.v(TAG, "onPreExecute pl wait.. start");
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.v(TAG, "onPostExecute pl wait.. done");

            if (showError) {
                askRetryOnError(lastError);
            } else {
                SharedPreferences sharedPref = activity.getSharedPreferences(program, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(program, packageName);
                editor.commit();

                WibmoSDK.setWibmoAppPackage(packageName);
                goToWibmoProgram(activity);
            }
        }
    }

    public static void goToWibmoProgram(Activity activity){
        String targetAppPackage = WibmoSDK.getWibmoIntentActionPackage();

        Intent intent = new Intent(targetAppPackage + ".ReadinessChecker");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        if (WibmoSDK.getWibmoAppPackage() != null) {
            intent.setPackage(WibmoSDK.getWibmoAppPackage());
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        try{
            activity.startActivityForResult(intent, REQUEST_CODE_IAP_READY);
        } catch (Exception e){
            Log.e(TAG, "exception:"+e,e);
        }
    }

    public static void startInAppFlowInBrowser(Activity activity,
           W2faInitRequest w2faInitRequest, W2faInitResponse w2faInitResponse) {
        Log.v(TAG, "startInAppFlowInBrowser");
        Intent intent = new Intent(activity, InAppBrowserActivity.class);
        intent.putExtra("W2faInitRequest", w2faInitRequest);
        intent.putExtra("W2faInitResponse", w2faInitResponse);
        activity.startActivityForResult(intent, WibmoSDK.REQUEST_CODE_IAP_2FA);
    }

    public static void startInAppFlowInBrowser(Activity activity,
            WPayInitRequest wPayInitRequest, WPayInitResponse wPayInitResponse) {
        Log.v(TAG, "startInAppFlowInBrowser");
        Intent intent = new Intent(activity, InAppBrowserActivity.class);
        intent.putExtra("WPayInitRequest", wPayInitRequest);
        intent.putExtra("WPayInitResponse", wPayInitResponse);
        activity.startActivityForResult(intent, WibmoSDK.REQUEST_CODE_IAP_PAY);
    }

    public static void startInAppFlowInApp(Activity activity,
               W2faInitRequest w2faInitRequest, W2faInitResponse w2faInitResponse) {
        Log.v(TAG, "startInAppFlowInApp");
        Intent intent = new Intent(WibmoSDK.getWibmoIntentActionPackage()+".InApp");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("W2faInitRequest", w2faInitRequest);
        intent.putExtra("W2faInitResponse", w2faInitResponse);

        /*
        String targetAppPackage = WibmoSDK.getWibmoPackage();
        boolean flag = WibmoSDK.isPackageExisted(activity, targetAppPackage);
        if (flag==false) {
            WibmoSDK.showDownloadDialog(activity);
        }
        */
        if(readyPackage!=null) {
            intent.setPackage(readyPackage);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        activity.startActivityForResult(intent, WibmoSDK.REQUEST_CODE_IAP_2FA);
    }

    public static void startInAppFlowInApp(Activity activity,
              WPayInitRequest wPayInitRequest, WPayInitResponse wPayInitResponse) {
      startInAppFlowInApp(activity, wPayInitRequest, wPayInitResponse, false);
    }

    public static void startInAppFlowInApp(Activity activity, WPayInitRequest wPayInitRequest,
                                           WPayInitResponse wPayInitResponse, boolean autoSubmitTxn) {
        Log.v(TAG, "startInAppFlowInApp");
        Intent intent = new Intent(WibmoSDK.getWibmoIntentActionPackage()+".InApp");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("WPayInitRequest", wPayInitRequest);
        intent.putExtra("WPayInitResponse", wPayInitResponse);
        intent.putExtra("autoSubmitIAPTxn", autoSubmitTxn);
        /*
        String targetAppPackage = WibmoSDK.getWibmoPackage();
        boolean flag = WibmoSDK.isPackageExisted(activity, targetAppPackage);
        if (flag==false) {
            WibmoSDK.showDownloadDialog(activity);
        }
        */
        if(readyPackage!=null) {
            intent.setPackage(readyPackage);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        activity.startActivityForResult(intent, WibmoSDK.REQUEST_CODE_IAP_PAY);
    }

    private void sendAbort() {
        sendAbort("sdk init - user abort");
    }

    private void sendAbort(String reason) {
        sendAbort(WibmoSDK.RES_CODE_FAILURE_USER_ABORT, reason);
    }

    private void sendAbort(String resCode, String resDesc) {
        Intent resultData = new Intent();
        resultData.putExtra(InAppUtil.EXTRA_KEY_RES_CODE, resCode);
        resultData.putExtra(InAppUtil.EXTRA_KEY_RES_DESC, resDesc);

        addTransactionInfoToDataInCaseMissing(resultData);
        setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }

    private void addTransactionInfoToDataInCaseMissing(Intent resultData) {
        if(resultData==null || resultData.getStringExtra(InAppUtil.EXTRA_KEY_MER_TXN_ID)!=null) {
            return;
        }

        if(w2faInitResponse!=null) {
            resultData.putExtra(InAppUtil.EXTRA_KEY_WIBMO_TXN_ID, w2faInitResponse.getWibmoTxnId());
            if(w2faInitResponse.getTransactionInfo()!=null) {
                resultData.putExtra(InAppUtil.EXTRA_KEY_MER_TXN_ID, w2faInitResponse.getTransactionInfo().getMerTxnId());
                resultData.putExtra(InAppUtil.EXTRA_KEY_MER_APP_DATA, w2faInitResponse.getTransactionInfo().getMerAppData());
            }
        } else if(wPayInitResponse!=null) {
            resultData.putExtra(InAppUtil.EXTRA_KEY_WIBMO_TXN_ID, wPayInitResponse.getWibmoTxnId());
            if(wPayInitResponse.getTransactionInfo()!=null) {
                resultData.putExtra(InAppUtil.EXTRA_KEY_MER_TXN_ID, wPayInitResponse.getTransactionInfo().getMerTxnId());
                resultData.putExtra(InAppUtil.EXTRA_KEY_MER_APP_DATA, wPayInitResponse.getTransactionInfo().getMerAppData());
            }
        }
    }

    private void sendFailure(String resCode, String resDesc) {
        Intent resultData = new Intent();
        resultData.putExtra(InAppUtil.EXTRA_KEY_RES_CODE, resCode);
        resultData.putExtra(InAppUtil.EXTRA_KEY_RES_DESC, "sdk init - "+resDesc);

        setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }
    private void sendFailure(W2faInitResponse w2faInitResponse) {
        Intent resultData = new Intent();
        resultData.putExtra(InAppUtil.EXTRA_KEY_RES_CODE, w2faInitResponse.getResCode());
        resultData.putExtra(InAppUtil.EXTRA_KEY_RES_DESC, "sdk init - "+w2faInitResponse.getResDesc());
        if(w2faInitResponse.getWibmoTxnId()!=null) {
            resultData.putExtra(InAppUtil.EXTRA_KEY_WIBMO_TXN_ID, w2faInitResponse.getWibmoTxnId());
            if(w2faInitResponse.getTransactionInfo()!=null) {
                resultData.putExtra(InAppUtil.EXTRA_KEY_MER_TXN_ID, w2faInitResponse.getTransactionInfo().getMerTxnId());
                resultData.putExtra(InAppUtil.EXTRA_KEY_MER_APP_DATA, w2faInitResponse.getTransactionInfo().getMerAppData());
            }
        }
        setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }
    private void sendFailure(WPayInitResponse wPayInitResponse) {
        Intent resultData = new Intent();
        resultData.putExtra(InAppUtil.EXTRA_KEY_RES_CODE, wPayInitResponse.getResCode());
        resultData.putExtra(InAppUtil.EXTRA_KEY_RES_DESC, "sdk init - "+wPayInitResponse.getResDesc());
        if(wPayInitResponse.getWibmoTxnId()!=null) {
            resultData.putExtra(InAppUtil.EXTRA_KEY_WIBMO_TXN_ID, wPayInitResponse.getWibmoTxnId());
            if(wPayInitResponse.getTransactionInfo()!=null) {
                resultData.putExtra(InAppUtil.EXTRA_KEY_MER_TXN_ID, wPayInitResponse.getTransactionInfo().getMerTxnId());
                resultData.putExtra(InAppUtil.EXTRA_KEY_MER_APP_DATA, wPayInitResponse.getTransactionInfo().getMerAppData());
            }
        }

        setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: "+requestCode);
        if(requestCode== WibmoSDK.REQUEST_CODE_IAP_PAY || requestCode== WibmoSDK.REQUEST_CODE_IAP_2FA) {
            addTransactionInfoToDataInCaseMissing(data);
        }

        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult~ requestCode: " + requestCode + "; resultCode: " + resultCode);

        if(requestCode==REQUEST_CODE_IAP_READY) {
            if(resultCode == Activity.RESULT_OK) {
                isAppReady = true;
            } else {
                isAppReady = false;
            }
            Log.d(TAG, "isAppReady: "+isAppReady);

            if(isCheckAppDetails) {
                setResult(resultCode, data);
                finish();
                return;
            }

            if(data!=null) {
                readyPackage = data.getStringExtra("Package");
                Log.v(TAG, "readyPackage: " + readyPackage);
                Log.v(TAG, "UsernameSet: " + data.getStringExtra("UsernameSet"));
                Log.v(TAG, "LoggedIn: " + data.getStringExtra("LoggedIn"));
                Log.v(TAG, "AppVersionCode: " + data.getStringExtra("AppVersionCode"));//added in 2070400

                extraDataReporting = new HashMap<>(8);
                extraDataReporting.put("WalletPackage", readyPackage);
                extraDataReporting.put("WalletVersionCode", data.getStringExtra("AppVersionCode"));
            } else {
                Log.d(TAG, "Data is NUll");
                extraDataReporting = null;
            }

            if(wPayInitResponse != null) {
                processInitPayRes();
            } else if(w2faInitResponse != null){
                processInit2FARes();
            }
            return;
        }

        if(resultCode != Activity.RESULT_OK) {
            if(requestCode== WibmoSDK.REQUEST_CODE_IAP_PAY || requestCode== WibmoSDK.REQUEST_CODE_IAP_2FA) {
                boolean isChargeLater = false;

                if(wPayInitRequest!=null) {
                    isChargeLater = wPayInitRequest.getTransactionInfo().isChargeLater();
                } else if(w2faInitRequest!=null) {
                    isChargeLater = w2faInitRequest.getTransactionInfo().isChargeLater();
                }

                if(isChargeLater) {
                    if(data!=null && data.getStringExtra(InAppUtil.EXTRA_KEY_BIN_USED)!=null) {
                        Log.d(TAG, "result is not ok for iap and 2fa .. lets ask if retry");

                        confirmIAPRetry(requestCode, resultCode, data);
                        return;
                    }
                }

                if(data!=null) {
                    String resCode = data.getStringExtra(InAppUtil.EXTRA_KEY_RES_CODE);
                    boolean isUserAbortCaptured = data.getBooleanExtra("isUserAbortCaptured", false);
                    Log.v(TAG, "isUserAbortCaptured: "+isUserAbortCaptured);

                    if (WibmoSDK.RES_CODE_FAILURE_USER_ABORT.equals(resCode)) {
                        if (WibmoSDKConfig.isPromptAbortReason() && !isUserAbortCaptured) {
                            InAppUtil.askReasonForAbort(activity, requestCode, resultCode, data, new AbortReasonCallback() {
                                @Override
                                public void onSelection(Context context, int requestCode, int resultCode, Intent data, String aReasonCode, String aReasonName) {
                                    abortReasonCode = aReasonCode;
                                    abortReasonName = aReasonName;

                                    processOnActivityResult(requestCode, resultCode, data);
                                }
                            });
                            return;
                        }
                    }
                }
            }
        }

        processOnActivityResult(requestCode, resultCode, data);
    }

    private void processOnActivityResult(int requestCode, int resultCode, Intent data) {
        if(data!=null) {
            String breadCrumb = data.getStringExtra(InAppUtil.EXTRA_KEY_BREADCRUMB);
            if (breadCrumb != null) {
                InAppUtil.appendBreadCrumb(breadCrumb);
            }
            InAppUtil.setLastBinUsed(data.getStringExtra(InAppUtil.EXTRA_KEY_BIN_USED));

            if(extraDataReporting==null) {
                extraDataReporting = new HashMap<>(9);
            }
            extraDataReporting.put("BreadCrumb", InAppUtil.getBreadCrumb());
            extraDataReporting.put("BinUsed", InAppUtil.getLastBinUsed());
            extraDataReporting.put("ITPPassed", data.getStringExtra(InAppUtil.EXTRA_KEY_ITP_PASSED));
            extraDataReporting.put("CustProgramId", data.getStringExtra(InAppUtil.EXTRA_KEY_PROGRAM_ID));
            extraDataReporting.put("PcAccountNumber", data.getStringExtra(InAppUtil.EXTRA_KEY_PC_AC_NUMBER));
            extraDataReporting.put("Username", data.getStringExtra(InAppUtil.EXTRA_KEY_USERNAME));
            extraDataReporting.put("SdkReTry", isReTry);
            extraDataReporting.put("LastURL", data.getStringExtra(InAppUtil.EXTRA_KEY_LAST_URL));
            extraDataReporting.put("Comments", data.getStringExtra(InAppUtil.EXTRA_KEY_COMMENTS));

            if(abortReasonName!=null && abortReasonCode!=null) {
                extraDataReporting.put("abortReasonCode", abortReasonCode);
                extraDataReporting.put("abortReasonName", abortReasonName);
            }

            Log.d(TAG, "BreadCrumb: " + InAppUtil.getBreadCrumb());
            Log.d(TAG, "BinUsed: " + InAppUtil.getLastBinUsed());
            Log.d(TAG, "ITPPassed: " + data.getStringExtra(InAppUtil.EXTRA_KEY_ITP_PASSED));
            Log.d(TAG, "CustProgramId: " + data.getStringExtra(InAppUtil.EXTRA_KEY_PROGRAM_ID));
            Log.d(TAG, "Username: " + data.getStringExtra(InAppUtil.EXTRA_KEY_USERNAME));

            InAppUtil.clearBreadCrumb();
            InAppUtil.setLastBinUsed(null);
            data.removeExtra(InAppUtil.EXTRA_KEY_BREADCRUMB);
            data.removeExtra(InAppUtil.EXTRA_KEY_BIN_USED);
            data.removeExtra(InAppUtil.EXTRA_KEY_ITP_PASSED);
            data.removeExtra(InAppUtil.EXTRA_KEY_PROGRAM_ID);
            data.removeExtra(InAppUtil.EXTRA_KEY_PC_AC_NUMBER);
            data.removeExtra(InAppUtil.EXTRA_KEY_USERNAME);
            data.removeExtra(InAppUtil.EXTRA_KEY_LAST_URL);
            data.removeExtra(InAppUtil.EXTRA_KEY_COMMENTS);
        } else {
            if(extraDataReporting==null) {
                extraDataReporting = new HashMap<>(1);
            }
        }

        setResult(resultCode, data);
        endTime = System.currentTimeMillis();

        long timeTaken = endTime - startTime;
        extraDataReporting.put("TimeTakenMs", timeTaken);

        if(wPayInitRequest!=null) {
            AnalyticalUtil.logTxn(activity.getApplicationContext(), extraDataReporting,
                    wPayInitRequest, wPayInitResponse, requestCode, resultCode, data);
        } else {
            AnalyticalUtil.logTxn(activity.getApplicationContext(), extraDataReporting,
                    w2faInitRequest, w2faInitResponse, requestCode, resultCode, data);
        }

        finish();
    }

    public void confirmIAPRetry(final int requestCode, final int resultCode, final Intent data) {
        final Activity activity = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        String errorMessage;
        String defaultErrorMsg = activity.getString(R.string.confirm_retry);
        try {
            String resCode = data.getExtras().getString(InAppUtil.EXTRA_KEY_RES_CODE);
            if (resCode!=null && resCode.equalsIgnoreCase(WibmoSDK.RES_CODE_FAILURE_VELOCITY_LIMIT_REACHED)) {
                String messageFromResult = data.getExtras().getString(InAppUtil.EXTRA_KEY_RES_DESC);
                errorMessage = messageFromResult.substring(messageFromResult.indexOf("-")+2) + defaultErrorMsg.substring(defaultErrorMsg.indexOf(".")+1);
            } else {
                errorMessage = defaultErrorMsg;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error:" + e, e);
            errorMessage = defaultErrorMsg;
        }
        builder.setMessage(errorMessage)
                .setCancelable(false)
                .setPositiveButton(
                        activity.getString(R.string.label_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                isReTry = true;

                                String breadCrumb = data.getStringExtra(InAppUtil.EXTRA_KEY_BREADCRUMB);
                                if (breadCrumb != null) {
                                    InAppUtil.appendBreadCrumb(breadCrumb);
                                    InAppUtil.appendBreadCrumb("R:");//re-try
                                }

                                startIAP();
                            }
                        })
                .setNegativeButton(
                        activity.getString(R.string.label_no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                if (dialog != null) {
                                    try {
                                        dialog.dismiss();
                                    } catch (IllegalArgumentException e) {
                                        Log.e(TAG, "Error: " + e);
                                    }
                                }

                                String resCode = data.getStringExtra(InAppUtil.EXTRA_KEY_RES_CODE);
                                boolean isUserAbortCaptured = data.getBooleanExtra("isUserAbortCaptured", false);
                                if (WibmoSDK.RES_CODE_FAILURE_USER_ABORT.equals(resCode)) {
                                    if (WibmoSDKConfig.isPromptAbortReason() && !isUserAbortCaptured) {
                                        InAppUtil.askReasonForAbort(activity, requestCode, resultCode, data, new AbortReasonCallback() {
                                            @Override
                                            public void onSelection(Context context, int requestCode, int resultCode, Intent data, String aReasonCode, String aReasonName) {
                                                abortReasonCode = aReasonCode;
                                                abortReasonName = aReasonName;

                                                processOnActivityResult(requestCode, resultCode, data);
                                            }
                                        });
                                        return;
                                    }
                                }

                                processOnActivityResult(requestCode, resultCode, data);
                            }
                        });

        Dialog dialog = builder.create();
        try {
            dialog.show();
        } catch (Throwable e) {
            Log.e(TAG, "Error: " + e, e);

            processOnActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        //fix for jira id: 6499
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    public void showWait(boolean flag) {

    }

    public void startWait() {

    }

    public void stopWait() {

    }

    public Activity getActivity() {
        return this;
    }

    private void processInit2FARes() {
        String weburl = w2faInitResponse.getWebUrl();

        if("000".equals(w2faInitResponse.getResCode())==false) {
            sendFailure(w2faInitResponse);
            return;
        }

        if(isAppReady) {
            startInAppFlowInApp(activity, w2faInitRequest, w2faInitResponse);
        } else {
            startInAppFlowInBrowser(activity, w2faInitRequest, w2faInitResponse);
            //Log.d(TAG, "Weburl: "+weburl);
        }
        mainView.setVisibility(View.INVISIBLE);
    }



    private void processInitPayRes() {
        if("000".equals(wPayInitResponse.getResCode())==false) {
            sendFailure(wPayInitResponse);
            return;
        }

        String weburl = wPayInitResponse.getWebUrl();
        Log.d(TAG, "Weburl: "+weburl);

        if(isAppReady) {
            if(autoSubmitIAPTxn) {
                startInAppFlowInApp(activity, wPayInitRequest, wPayInitResponse, autoSubmitIAPTxn);
            } else {
                startInAppFlowInApp(activity, wPayInitRequest, wPayInitResponse);
            }
        } else {
            startInAppFlowInBrowser(activity, wPayInitRequest, wPayInitResponse);
        }
        mainView.setVisibility(View.INVISIBLE);
    }

    private void manageError(Throwable e) {
        if(e!=null) {
            Log.e(TAG, "Error: " + e, e);
        }
        Toast toast = Toast.makeText(activity,
                getString(R.string.error_generic_try_after_st),
                Toast.LENGTH_LONG);
        toast.show();
    }

    private class Init2FAReqTask extends AsyncTask<W2faInitRequest, Void, Void> {
        private boolean showError = false;
        private String lastError;

        @Override
        protected void onPreExecute() {
            Log.v(TAG, "pl wait.. start");
        }

        @Override
        protected Void doInBackground(W2faInitRequest... data) {
            try {
                w2faInitResponse = InAppHandler.init2FA(data[0]);

                if(w2faInitResponse!=null && WibmoSDK.getInAppTxnIdCallback()!=null) {
                    Thread t = new Thread() {
                        public void run() {
                            boolean recorderByApp = false;
                            for(int i=0;i<3;i++) {
                                Log.v(TAG, "Calling app callback.. "+i);
                                try {
                                    recorderByApp = WibmoSDK.getInAppTxnIdCallback().recordInit(activity.getApplicationContext(),
                                            w2faInitResponse.getWibmoTxnId(), w2faInitResponse.getTransactionInfo().getMerTxnId());
                                } catch (Throwable e) {
                                    Log.e(TAG, "Error: "+e,e);
                                    break;
                                }

                                if (recorderByApp) {
                                    break;
                                }
                            }
                        }
                    };
                    t.setName("InAppTxnIdCallback");
                    t.start();
                }
            } catch (Exception ex) {
                Log.e(TAG, "Error: " + ex, ex);
                lastError = ex.toString();
                showError = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.v(TAG, "pl wait.. done");

            if (showError) {
                askRetryOnError(lastError);
            } else {
                if(InAppUtil.isWibmoInstalled(getActivity())) {
                    startInAppReadinessCheck(null, w2faInitResponse);
                    return;
                } else {
                    processInit2FARes();
                }
            }
        }
    }

    private class InitPayReqTask extends AsyncTask<WPayInitRequest, Void, Void> {
        private boolean showError = false;
        private String lastError;

        @Override
        protected void onPreExecute() {
            Log.v(TAG, "pl wait.. start");
            if(WibmoSDKConfig.isTestMode()) {
                Toast toast = Toast.makeText(activity,
                        "WibmoSDK: We are running in test mode!!",
                        Toast.LENGTH_LONG);
                toast.show();
            }
        }

        @Override
        protected Void doInBackground(WPayInitRequest... data) {
            try {
                wPayInitResponse = InAppHandler.initPay(data[0]);

                if(wPayInitResponse!=null
                        && WibmoSDK.RES_CODE_NO_ERROR.equals(wPayInitResponse.getResCode())
                        && WibmoSDK.getInAppTxnIdCallback()!=null) {
                    Thread t = new Thread() {
                        public void run() {
                            boolean recorderByApp = false;
                            for(int i=0;i<3;i++) {
                                Log.v(TAG, "Calling app callback.. "+i);
                                try {
                                    recorderByApp = WibmoSDK.getInAppTxnIdCallback().recordInit(activity.getApplicationContext(),
                                            wPayInitResponse.getWibmoTxnId(), wPayInitResponse.getTransactionInfo().getMerTxnId());
                                } catch (Throwable e) {
                                    Log.e(TAG, "Error: "+e,e);
                                    break;
                                }

                                if (recorderByApp) {
                                    break;
                                }
                            }
                        }
                    };
                    t.setName("InAppTxnIdCallback");
                    t.start();
                }
            } catch (Exception ex) {
                Log.e(TAG, "Error: " + ex, ex);
                lastError = ex.toString();
                showError = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.v(TAG, "pl wait.. done.. "+showError);

            if (showError) {
                askRetryOnError(lastError);
            } else {
                if(InAppUtil.isWibmoInstalled(getActivity())) {
                    startInAppReadinessCheck(wPayInitResponse, null);
                    return;
                } else {
                    processInitPayRes();
                }
            }
        }
    }

    private void askRetryOnError(final String lastError) {
        Log.i(TAG, "askRetryOnError: "+lastError);
        String msg = getString(R.string.msg_servers_issue);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(
                        activity.getString(R.string.label_try_again),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startIAP();
                            }
                })
                .setNegativeButton(
                        activity.getString(R.string.label_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (dialog != null) {
                                    try {
                                        dialog.dismiss();
                                    } catch (IllegalArgumentException e) {
                                        Log.e(TAG, "Error: " + e);
                                    }
                                }

                                manageError(null);
                                sendFailure(WibmoSDK.RES_CODE_FAILURE_INTERNAL_ERROR,
                                        "init failed with server error.. chk logs " + lastError);
                            }
                        });

        Dialog dialog = builder.create();
        try {
            dialog.show();
        } catch (Throwable e) {
            Log.e(TAG, "Error: " + e, e);

            manageError(null);
            sendFailure(WibmoSDK.RES_CODE_FAILURE_INTERNAL_ERROR,
                    "init failed with server error.. chk logs " + lastError);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.v(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putBoolean("wasSaved", true);
    }
}