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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
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
import com.google.gson.Gson;
import android.support.v4.app.ActivityCompat;

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

    private AsyncTask asyncTask = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "OnCreate");
        activity = this;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            w2faInitRequest = (W2faInitRequest) extras
                    .getSerializable("W2faInitRequest");
            wPayInitRequest = (WPayInitRequest) extras
                    .getSerializable("WPayInitRequest");

            //reset
            w2faInitResponse = null;
            wPayInitResponse = null;


            if (w2faInitRequest != null || wPayInitRequest!=null) {
                qrMsg = "InApp payment";
            } else {
                Log.e(TAG, "W2faInitRequest and wPayInitRequest was null!");
                sendAbort();
                return;
            }
        }

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(false);

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


        if(WibmoSDK.IS_PHONE_STATE_PERMISSION_REQ) {
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
                                sendAbort("no permission ph state");
                            }
                        });

                return;//we need this so can;t go on
            }
        }

        doIAPStuff();
    }

    private void doIAPStuff() {
        if(InAppUtil.isWibmoInstalled(this)) {
            startInAppReadinessCheck(this);
        } else {
            isAppReady = false;

            startIAP();
        }
    }

    private void cancelIAP() {
        if(asyncTask!=null) {
            asyncTask.cancel(true);
            asyncTask = null;
        }
    }

    private void startIAP() {
        if (w2faInitRequest != null) {
            w2faInitRequest.setDeviceInfo(InAppUtil.makeDeviceInfo(activity, WibmoSDK.VERSION));

            w2faInitRequest.getDeviceInfo().setAppInstalled(isAppReady);

            asyncTask = new Init2FAReqTask().execute(w2faInitRequest);
        }

        if (wPayInitRequest != null) {
            wPayInitRequest.setDeviceInfo(InAppUtil.makeDeviceInfo(activity, WibmoSDK.VERSION));

            wPayInitRequest.getDeviceInfo().setAppInstalled(isAppReady);

            asyncTask = new InitPayReqTask().execute(wPayInitRequest);
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
                sendAbort("no permission ph state");
            }
            return;
        }
    }

    public static void startInAppReadinessCheck(Activity activity) {
        Log.d(TAG, "startInAppReadinessCheck");

        String targetAppPackage = WibmoSDK.getWibmoIntentActionPackage();

        Intent intent = new Intent(targetAppPackage+".ReadinessChecker");
        intent.addCategory(Intent.CATEGORY_DEFAULT);

        if(WibmoSDK.getWibmoAppPackage()!=null) {
            intent.setPackage(WibmoSDK.getWibmoAppPackage());
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        activity.startActivityForResult(intent, REQUEST_CODE_IAP_READY);
    }


    public static void startInAppFlowInBrowser(Activity activity,
           W2faInitRequest w2faInitRequest, W2faInitResponse w2faInitResponse) {
        Intent intent = new Intent(activity, InAppBrowserActivity.class);
        intent.putExtra("W2faInitRequest", w2faInitRequest);
        intent.putExtra("W2faInitResponse", w2faInitResponse);
        activity.startActivityForResult(intent, WibmoSDK.REQUEST_CODE_IAP_2FA);
    }

    public static void startInAppFlowInBrowser(Activity activity,
            WPayInitRequest wPayInitRequest, WPayInitResponse wPayInitResponse) {
        Intent intent = new Intent(activity, InAppBrowserActivity.class);
        intent.putExtra("WPayInitRequest", wPayInitRequest);
        intent.putExtra("WPayInitResponse", wPayInitResponse);
        activity.startActivityForResult(intent, WibmoSDK.REQUEST_CODE_IAP_PAY);
    }

    public static void startInAppFlowInApp(Activity activity,
               W2faInitRequest w2faInitRequest, W2faInitResponse w2faInitResponse) {
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
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        activity.startActivityForResult(intent, WibmoSDK.REQUEST_CODE_IAP_2FA);
    }

    public static void startInAppFlowInApp(Activity activity,
              WPayInitRequest wPayInitRequest, WPayInitResponse wPayInitResponse) {
        Intent intent = new Intent(WibmoSDK.getWibmoIntentActionPackage()+".InApp");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("WPayInitRequest", wPayInitRequest);
        intent.putExtra("WPayInitResponse", wPayInitResponse);

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
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        activity.startActivityForResult(intent, WibmoSDK.REQUEST_CODE_IAP_PAY);
    }

    private void sendAbort() {
        sendAbort("user abort");
    }

    private void sendAbort(String reason) {
        Intent resultData = new Intent();
        resultData.putExtra("ResCode", "204");
        resultData.putExtra("ResDesc", reason);
        if(w2faInitResponse!=null) {
            resultData.putExtra("WibmoTxnId", w2faInitResponse.getWibmoTxnId());
            if(w2faInitResponse.getTransactionInfo()!=null) {
                resultData.putExtra("MerTxnId", w2faInitResponse.getTransactionInfo().getMerTxnId());
            }
        } else if(wPayInitResponse!=null) {
            resultData.putExtra("WibmoTxnId", wPayInitResponse.getWibmoTxnId());
            if(wPayInitResponse.getTransactionInfo()!=null) {
                resultData.putExtra("MerTxnId", wPayInitResponse.getTransactionInfo().getMerTxnId());
            }
        }
        setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }

    private void sendFailure(String resCode, String resDesc) {
        Intent resultData = new Intent();
        resultData.putExtra("ResCode", resCode);
        resultData.putExtra("ResDesc", resDesc);

        setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }
    private void sendFailure(W2faInitResponse w2faInitResponse) {
        Intent resultData = new Intent();
        resultData.putExtra("ResCode", w2faInitResponse.getResCode());
        resultData.putExtra("ResDesc", w2faInitResponse.getResDesc());
        if(w2faInitResponse.getWibmoTxnId()!=null) {
            resultData.putExtra("WibmoTxnId", w2faInitResponse.getWibmoTxnId());
            if(w2faInitResponse.getTransactionInfo()!=null) {
                resultData.putExtra("MerTxnId", w2faInitResponse.getTransactionInfo().getMerTxnId());
            }
        }
        setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }
    private void sendFailure(WPayInitResponse wPayInitResponse) {
        Intent resultData = new Intent();
        resultData.putExtra("ResCode", wPayInitResponse.getResCode());
        resultData.putExtra("ResDesc", wPayInitResponse.getResDesc());
        if(wPayInitResponse.getWibmoTxnId()!=null) {
            resultData.putExtra("WibmoTxnId", wPayInitResponse.getWibmoTxnId());
            if(wPayInitResponse.getTransactionInfo()!=null) {
                resultData.putExtra("MerTxnId", wPayInitResponse.getTransactionInfo().getMerTxnId());
            }
        }

        setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult~ requestCode: " + requestCode + "; resultCode: " + resultCode);

        if(requestCode==REQUEST_CODE_IAP_READY) {
            if(resultCode == Activity.RESULT_OK) {
                isAppReady = true;
            } else {
                isAppReady = false;
            }
            Log.d(TAG, "isAppReady: "+isAppReady);
            if(data!=null) {
                readyPackage = data.getStringExtra("Package");
                Log.v(TAG, "readyPackage: " + readyPackage);
            }
            startIAP();

            return;
        }

        setResult(resultCode, data);
        finish();
    }

    @Override
    public void onBackPressed() {
        sendAbort();
        super.onBackPressed();
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
            startInAppFlowInApp(activity, wPayInitRequest, wPayInitResponse);
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
                "We had an error, please try after sometime",
                Toast.LENGTH_LONG);
        toast.show();
    }


    private class Init2FAReqTask extends AsyncTask<W2faInitRequest, Void, Void> {
        private boolean showError = false;

        @Override
        protected void onPreExecute() {
            Log.v(TAG, "pl wait.. start");
        }

        @Override
        protected Void doInBackground(W2faInitRequest... data) {
            try {
                w2faInitResponse = InAppHandler.init2FA(data[0]);
            } catch (Exception ex) {
                Log.e(TAG, "Error: " + ex, ex);
                showError = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.v(TAG, "pl wait.. done");

            if (showError) {
                manageError(null);
                sendFailure("051", "init failed with server error.. chk logs");
            } else {
                processInit2FARes();
            }
        }
    }

    private class InitPayReqTask extends AsyncTask<WPayInitRequest, Void, Void> {
        private boolean showError = false;

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

                //Log.v(TAG, "wPayInitResponse  "+ (new Gson()).toJson(wPayInitResponse));
            } catch (Exception ex) {
                Log.e(TAG, "Error: " + ex, ex);
                showError = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.v(TAG, "pl wait.. done");

            if (showError) {
                manageError(null);
                sendFailure("051", "init failed with server error.. chk logs");
            } else {
                processInitPayRes();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}