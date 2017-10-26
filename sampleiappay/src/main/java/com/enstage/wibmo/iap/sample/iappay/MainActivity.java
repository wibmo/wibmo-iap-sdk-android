package com.enstage.wibmo.iap.sample.iappay;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.enstage.wibmo.sdk.WibmoSDK;

import com.enstage.wibmo.sdk.WibmoSDKConfig;
import com.enstage.wibmo.sdk.inapp.pojo.CustomerInfo;
import com.enstage.wibmo.sdk.inapp.pojo.MerchantInfo;
import com.enstage.wibmo.sdk.inapp.pojo.TransactionInfo;
import com.enstage.wibmo.sdk.inapp.pojo.WPayInitRequest;
import com.enstage.wibmo.sdk.inapp.pojo.WPayResponse;


public class MainActivity extends ActionBarActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private WPayInitRequest wPayInitRequest = null;

    private Activity activity = null;
    private TextView outputView =null;

    //test data
    private long amount = 100;

    private long startTime;
    private long endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;

        outputView = (TextView) findViewById(R.id.result);

        TextView amountView = (TextView) findViewById(R.id.title_3);
        amountView.setText(""+(amount/100.00));//convert to decimals

        View payWithWibmoButton = (View) findViewById(R.id.pww_button);
        payWithWibmoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processPayWithWibmo();
            }
        });

        final Context context = getApplicationContext();
        Thread t = new Thread() {
            public void run() {
                //uncomment next two statement for staging setup
                //WibmoSDK.setWibmoIntentActionPackage("com.enstage.wibmo.sdk.inapp.staging");
                //WibmoSDKConfig.setWibmoDomain("https://api.pc.enstage-sas.com");

                WibmoSDK.init(context);
            }
        };
        t.start();

        logOutput("PID: "+android.os.Process.myPid());
    }

    private void logOutput(String s) {
        outputView.setText(outputView.getText().toString().isEmpty()?s:outputView.getText()+"\n"+s);
    }

    private void processPayWithWibmo() {

        /*
        //Prod - Main Test
        String merID = "MYMERCHANTID"; //"MYMERCHANTID";//change me
        String merAppID = "MYAPPID"; //"MYAPPID";//change me
        String merMerCountryCode = "IN";//change me if req
        MerchantHandler.setMerchantDomain("myprodserver.com"); //"myprodserver.com"
        //-
        */

        /*
        //Prod Main 2nd Test
        String merID = "MYMERCHANTID"; //"MYMERCHANTID";//change me
        String merAppID = "MYAPPID"; //"MYAPPID";//change me
        String merMerCountryCode = "IN";//change me if req
        MerchantHandler.setMerchantDomain("myprodserver.com"); //"myprodserver.com"
        //-
        */

        /**/
        //staging
        String merID = "MYMERCHANTID"; //"MYMERCHANTID";//change me
        String merAppID = "MYAPPID"; //"MYAPPID";//change me
        String merMerCountryCode = "IN";//change me if req
        MerchantHandler.setMerchantDomain("mytestserver.com"); //"mytestserver.com"
        //-
        /**/

        wPayInitRequest = new WPayInitRequest();

        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.setTxnAmount("" + amount);//implied decimals Rs1=100
        transactionInfo.setTxnCurrency("356");//356 for INR
        transactionInfo.setSupportedPaymentType(new String[]{
                "*"});
        //"*", "w.ds.pt.card_visa", "w.ds.pt.card_mastercard" or * or "w.ds.pt.card_wallet"
        //transactionInfo.setRestrictedPaymentType(new String[]{WibmoSDK.PAYMENT_TYPE_WALLET_CARD});

        transactionInfo.setTxnDesc("merchant txn desc");//change me
        transactionInfo.setMerAppData("This is some merchant data");//change me
        transactionInfo.setMerDataField("This is for recon");//change me
        transactionInfo.setChargeLater(false);
        transactionInfo.setTxnAmtKnown(true);

        MerchantInfo merchantInfo = new MerchantInfo();
        merchantInfo.setMerAppId(merAppID);
        merchantInfo.setMerCountryCode(merMerCountryCode);
        merchantInfo.setMerId(merID);

        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setCustEmail("customer@somemail.com");//change me [set if available for better ux]
        customerInfo.setCustName("Customer Name");//change me [set if available for better ux]
        customerInfo.setCustDob("20011231");//change me [set if available for better ux]
        customerInfo.setCustMobile("9123412347");//change me [set if available for better ux]

        /*
        //..pass card saved at merchant..
        CardInfo cardInfo = new CardInfo();
        cardInfo.setCardnumber("4111111111111111");
        cardInfo.setExpiryMM("12");
        cardInfo.setExpiryYYYY("2015");
        cardInfo.setNameOnCard("Name on Card");

        wPayInitRequest.setCardInfo(cardInfo);
        //..pass card saved at merchant..
        */

        wPayInitRequest.setTransactionInfo(transactionInfo);
        wPayInitRequest.setMerchantInfo(merchantInfo);
        wPayInitRequest.setCustomerInfo(customerInfo);

        /*
        //get this data from your server
        wPayInitRequest.setMsgHash("message hash");
        wPayInitRequest.getTransactionInfo().setMerTxnId("merchant txn id");

        //call API
        WibmoSDK.startForInApp(activity, wPayInitRequest);
        */

        new MakeMessageHashTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WibmoSDK.REQUEST_CODE_IAP_PAY) {
            endTime = System.currentTimeMillis();

            long timeDiff = endTime - startTime;

            StringBuilder sb = new StringBuilder();
            if (resultCode == RESULT_OK) {

                WPayResponse res = WibmoSDK.processInAppResponseWPay(data);
                Log.i(TAG, "resCode: " + res.getResCode()+
                        "; ResDesc: "+res.getResDesc());

                sb.append("resCode: " + res.getResCode()).append("\n");
                sb.append("resDesc: "+res.getResDesc()).append("\n");

                //success;
                String wPayTxnId = res.getWibmoTxnId();
                sb.append("wPayTxnId: "+wPayTxnId).append("\n");

                String merAppData = res.getMerAppData();
                sb.append("merAppData: "+merAppData).append("\n");

                String merTxnId = res.getMerTxnId();
                sb.append("merTxnId: "+merTxnId).append("\n");

                String msgHash = res.getMsgHash();
                sb.append("msgHash: "+msgHash).append("\n");

                String dataPickUpCode = res.getDataPickUpCode();
                sb.append("dataPickUpCode: "+dataPickUpCode).append("\n");
            } else {
                Log.i(TAG, "requestCode: not ok");
                if(data!=null) {
                    String resCode = data.getStringExtra("ResCode");
                    String resDesc = data.getStringExtra("ResDesc");
                    Log.i(TAG, "resCode: " + resCode+"; ResDesc: "+resDesc);
                    //failed .. more data at resDesc

                    sb.append("resCode: " + resCode).append("\n");
                    sb.append("resDesc: "+resDesc).append("\n");
                } else {
                    //failed
                }
            }//result not ok

            //sb.append("\nTime: "+timeDiff).append(" ms").append("\n");
            sb.append("\nTime: "+timeDiff/1000).append(" sec").append("\n");

            outputView.setText(sb.toString());
        }// requestCode
    }//onActivityResult

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_shell) {
            Intent intent = new Intent(this, WebShellActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class MakeMessageHashTask extends AsyncTask<Void, Void, Void> {
        private boolean showError = false;
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(activity, "", getString(R.string.lable_please_wait), true);
            dialog.setInverseBackgroundForced(true);
            dialog.setProgressStyle(android.R.attr.progressBarStyleInverse);
        }

        @Override
        protected Void doInBackground(Void... data) {
            try {

                if(wPayInitRequest!=null) {
                    wPayInitRequest = MerchantHandler.generateMessageHash(wPayInitRequest);
                }
            } catch (Exception ex) {
                Log.e(TAG, "Error: " + ex, ex);
                showError = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();

            if (showError) {
                Toast toast = Toast.makeText(activity,
                        activity.getString(R.string.error_generic_try_after_st),
                        Toast.LENGTH_LONG);
                toast.show();
            } else {
                if(wPayInitRequest!=null) {
                    startTime = System.currentTimeMillis();
                    WibmoSDK.startForInApp(activity, wPayInitRequest);
                }
            }
        }
    }
}
