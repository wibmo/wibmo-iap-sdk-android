package com.enstage.wibmo.iap.sample.iappayv2.taxi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.enstage.wibmo.sdk.WibmoSDK;
import com.enstage.wibmo.sdk.WibmoSDKConfig;
import com.enstage.wibmo.sdk.inapp.InAppTxnIdCallback;
import com.enstage.wibmo.sdk.inapp.pojo.CustomerInfo;
import com.enstage.wibmo.sdk.inapp.pojo.MerchantInfo;
import com.enstage.wibmo.sdk.inapp.pojo.TransactionInfo;
import com.enstage.wibmo.sdk.inapp.pojo.WPayInitRequest;
import com.enstage.wibmo.sdk.inapp.pojo.WPayResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private WPayInitRequest wPayInitRequest;
    private WPayResponse wPayResponse;

    //test data for txn
    private long amount = 100;
    private String lastWibmoTxnId;

    private Activity activity;

    private TextView outputTaxiMain;
    private TextView outputTaxiStatus;
    private TextView outputDriver;
    private Button buttonBookTaxi;
    private Button buttonCheckStatus;
    private Button buttonEndTrip;

    private long startTime;
    private long endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        activity = this;

        outputTaxiMain = (TextView) findViewById(R.id.output_booking);
        outputTaxiStatus = (TextView) findViewById(R.id.output_status);
        outputDriver = (TextView) findViewById(R.id.output_driver);

        buttonBookTaxi = (Button) findViewById(R.id.button_book);
        buttonCheckStatus = (Button) findViewById(R.id.button_check_status);
        buttonEndTrip = (Button) findViewById(R.id.button_end_trip);
        buttonEndTrip.setVisibility(View.GONE);

        buttonBookTaxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //reset
                wPayInitRequest = null;
                wPayResponse = null;

                outputTaxiMain.setText("");
                outputTaxiStatus.setText("");
                outputDriver.setText("Waiting For Customer..");
                buttonEndTrip.setVisibility(View.GONE);

                processPayWithWibmo();
            }
        });

        buttonCheckStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doStatusCheck();
            }
        });

        buttonEndTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doChargeCard();
            }
        });

        final Context context = getApplicationContext();
        Thread t = new Thread() {
            public void run() {
                //comment next two statement for prod setup
                WibmoSDK.setWibmoIntentActionPackage("com.enstage.wibmo.sdk.inapp.staging");
                WibmoSDKConfig.setWibmoDomain("https://wallet.pc.enstage-sas.com");

                WibmoSDK.init(context);
            }
        };
        t.start();
    }

    private void processPayWithWibmo() {
        /*
        //Prod
        String merID = "MYMERCHANTID"; //"MYMERCHANTID";//change me
        String merAppID = "MYAPPID"; //"MYAPPID";//change me
        String merMerCountryCode = "IN";//change me if req
        MerchantHandler.setMerchantDomain("myprodserver.com"); //"myprodserver.com"
        //-
        /**/

        /**/
        //staging
        String merID = "MYMERCHANTID"; //"MYMERCHANTID";//change me
        String merAppID = "MYAPPID"; //"MYAPPID";//change me
        String merMerCountryCode = "IN";//change me if req
        MerchantHandler.setMerchantDomain("mytestserver.com"); //"mytestserver.com"
        //-
        /**/

        wPayInitRequest = new WPayInitRequest();
        wPayInitRequest.setTxnType(WibmoSDK.TRANSACTION_TYPE_WPAY);

        TransactionInfo transactionInfo = new TransactionInfo();

        //-- when amount is not known; use Rs1=100 (actual amount will be passed in charge req
        transactionInfo.setTxnAmount("" + amount);//implied decimals Rs1=100
        transactionInfo.setTxnCurrency("356");//356 for INR
        transactionInfo.setSupportedPaymentType(new String[]{WibmoSDK.PAYMENT_TYPE_ALL});//WibmoSDK.PAYMENT_TYPE_ALL or WibmoSDK.PAYMENT_TYPE_WALLET_CARD
        transactionInfo.setTxnAmtKnown(false);
        transactionInfo.setChargeLater(true);

        transactionInfo.setTxnDesc("merchant txn desc"); //change this to something meaning full to customer
        transactionInfo.setMerAppData("This is some app data");  //change this to something meaning for your app
        transactionInfo.setMerDataField("This is for recon");   //change this to something meaning for your recon



        MerchantInfo merchantInfo = new MerchantInfo();
        merchantInfo.setMerAppId(merAppID);
        merchantInfo.setMerCountryCode(merMerCountryCode);
        merchantInfo.setMerId(merID);

        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setCustEmail("customer@somemail.com");//change this to user's values if available for better ux
        customerInfo.setCustName("Customer Name");//change this to user's values if available for better ux
        customerInfo.setCustDob("20011231");//change this to user's values if available for better ux
        customerInfo.setCustMobile("9123412346");//change this to user's values if available for better ux

        /*
        //..pass card saved if available
        CardInfo cardInfo = new CardInfo();
        cardInfo.setCardnumber("4111111111111111");
        cardInfo.setExpiryMM("12");
        cardInfo.setExpiryYYYY("2015");
        cardInfo.setNameOnCard("Name on Card");

        wPayInitRequest.setCardInfo(cardInfo);
        //..pass card saved if available
        */

        wPayInitRequest.setTransactionInfo(transactionInfo);
        wPayInitRequest.setMerchantInfo(merchantInfo);
        wPayInitRequest.setCustomerInfo(customerInfo);

        InAppTxnIdCallback myCallBack = new InAppTxnIdCallback() {
            @Override
            public boolean recordInit(Context context, String wibmoTxnId, String merTxnId) {
                Log.i(TAG, "We have init done: wibmoTxnId: " + wibmoTxnId + "; merTxnId: " + merTxnId);
                lastWibmoTxnId = wibmoTxnId;
                //TODO save this to DB; send to your server via Intent Service; you will need this for charge or status check req.
                return true;
            }
        };
        WibmoSDK.setInAppTxnIdCallback(myCallBack);

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

                wPayResponse = WibmoSDK.processInAppResponseWPay(data);
                Log.i(TAG, "resCode: " + wPayResponse.getResCode()+
                        "; ResDesc: "+wPayResponse.getResDesc());

                sb.append("resCode: " + wPayResponse.getResCode()).append("\n");
                sb.append("resDesc: "+wPayResponse.getResDesc()).append("\n");

                //success;
                String wPayTxnId = wPayResponse.getWibmoTxnId();
                sb.append("wPayTxnId: "+wPayTxnId).append("\n");

                String msgHash = wPayResponse.getMsgHash();
                sb.append("msgHash: "+msgHash).append("\n");

                //outputTaxiStatus.setText(sb.toString());
                outputTaxiMain.setText("Booking Confirmed.\nYour Trip is active now..");
                outputDriver.setText("Trip Active..");
                buttonBookTaxi.setVisibility(View.GONE);

                buttonEndTrip.setVisibility(View.VISIBLE);
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

                //outputTaxiStatus.setText(sb.toString());
                outputTaxiMain.setText("Booking Failed. You may try again..");
                buttonBookTaxi.setVisibility(View.VISIBLE);

                buttonEndTrip.setVisibility(View.GONE);
                outputDriver.setText("Waiting For Customer..");
            }//result not ok

            Log.i(TAG, "Time: " + timeDiff / 1000 + " sec");
            //sb.append("\nTime: "+timeDiff/1000).append(" sec").append("\n");

        }// requestCode
    }//onActivityResult

    private class MakeMessageHashTask extends AsyncTask<Void, Void, Void> {
        private boolean showError = false;
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(activity, "", "Please wait", true);
            dialog.setInverseBackgroundForced(true);
            dialog.setProgressStyle(android.R.attr.progressBarStyleInverse);
        }

        @Override
        protected Void doInBackground(Void... data) {
            try {

                if(wPayInitRequest!=null) {
                    wPayInitRequest = MerchantHandler.generateMessageHash(wPayInitRequest);//wPayInitRequest.setMsgHash("test");
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
                        "We had an error, please try after sometime",
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

    public void doStatusCheck() {
        Log.v(TAG, "doStatusCheck");

        if(wPayInitRequest==null) {
            Toast toast = Toast.makeText(activity,
                    "Your txn should be started; before you can do status check!",
                    Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        AsyncTask<Void, Void, Void> task = new StatusCheckTask();
        task.execute();
    }

    private class StatusCheckTask extends AsyncTask<Void, Void, Void> {
        private boolean showError = false;
        private ProgressDialog dialog;

        private String output;

        @Override
        protected void onPreExecute() {
            outputTaxiStatus.setText("...");

            dialog = ProgressDialog.show(activity, "", "Please wait", true);
            dialog.setInverseBackgroundForced(true);
            dialog.setCancelable(false);
            dialog.setProgressStyle(android.R.attr.progressBarStyleInverse);
        }

        @Override
        protected Void doInBackground(Void... data) {
            try {
                String wibmoTxnIdToPass = null;
                if(wPayResponse!=null) {
                    //use txn id from pay res..
                    wibmoTxnIdToPass = wPayResponse.getWibmoTxnId();
                } else {
                    //use txn id from init res..
                    wibmoTxnIdToPass = lastWibmoTxnId;
                }
                output = MerchantHandler.doStatusCheck(wPayInitRequest, wibmoTxnIdToPass);
            } catch (Throwable ex) {
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
                        "We had an error, please try after sometime",
                        Toast.LENGTH_LONG);
                toast.show();
            } else {
                outputTaxiStatus.setText(output);
            }
        }
    }

    public void doChargeCard() {
        Log.v(TAG, "doChargeCard");

        if(wPayInitRequest==null) {
            Toast toast = Toast.makeText(activity,
                    "Your txn should be started; before you can do charge card!",
                    Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        AsyncTask<Void, Void, Void> task = new ChargeCardTask();
        if(wPayInitRequest.getTransactionInfo().isTxnAmtKnown()==false) {
            askAmount(wPayInitRequest.getTransactionInfo(), task);
            return;
        }

        task.execute();
    }

    public void askAmount(final TransactionInfo transactionInfo, final AsyncTask<Void, Void, Void> task) {
        Log.v(TAG, "askAmount");

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Amount");
        alert.setMessage("Enter amount to charge (in implied decimals)");
        alert.setCancelable(false);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alert.setView(input);

        alert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString().trim();
                        Log.i(TAG, "amount: " + value);

                        transactionInfo.setTxnAmount(value);
                        task.execute();
                    }
                });

        alert.setNegativeButton(getString(R.string.label_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        alert.show();
    }

    private class ChargeCardTask extends AsyncTask<Void, Void, Void> {
        private boolean showError = false;
        private ProgressDialog dialog;

        private String output;

        @Override
        protected void onPreExecute() {
            outputDriver.setText("...");

            dialog = ProgressDialog.show(activity, "", "Please wait", true);
            dialog.setInverseBackgroundForced(true);
            dialog.setCancelable(false);
            dialog.setProgressStyle(android.R.attr.progressBarStyleInverse);
        }

        @Override
        protected Void doInBackground(Void... data) {
            try {
                String wibmoTxnIdToPass = null;
                if(wPayResponse!=null) {
                    //use txn id from pay res..
                    wibmoTxnIdToPass = wPayResponse.getWibmoTxnId();
                } else {
                    //use txn id from init res..
                    wibmoTxnIdToPass = lastWibmoTxnId;
                }
                output = MerchantHandler.doCharge(wPayInitRequest, wibmoTxnIdToPass);
            } catch (Throwable ex) {
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
                        "We had an error, please try after sometime",
                        Toast.LENGTH_LONG);
                toast.show();
            } else {
                outputDriver.setText(output);

                if(output!=null) {
                    outputTaxiMain.setText("Trip has ended");
                    buttonBookTaxi.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    //--- helper stuff

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
