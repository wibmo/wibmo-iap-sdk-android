package com.enstage.wibmo.iap.sample.iappayv2.taxi;

import android.util.Log;

import com.enstage.wibmo.sdk.inapp.pojo.WPayInitRequest;
import com.enstage.wibmo.util.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * Created by Akshathkumar Shetty on 16/02/16.
 */
public class MerchantHandler {
    private static final String TAG = MerchantHandler.class.getSimpleName();

    private static String merchantDomain = "www.wibmo.com";
    //private static String merchantDomain = "wallet.pcdev.enstage-sas.com";

    private static String txnDateYYYYMMDD;

    public static WPayInitRequest generateMessageHash(WPayInitRequest req) throws Exception {
        String posturl = "https://"+ getMerchantDomain() +"/sampleMerchant/taxi/generateInitReqMessageHash.jsp";
        //String posturl = "https://localhost/app/generatewPayMessageHash";

        StringBuilder postsb = new StringBuilder();

        postsb.append(URLEncoder.encode("merAppData", "UTF-8"));
        postsb.append("=");
        postsb.append(URLEncoder.encode(req.getTransactionInfo().getMerAppData(), "UTF-8"));
        postsb.append("&");

        postsb.append(URLEncoder.encode("txnAmount", "UTF-8"));
        postsb.append("=");
        postsb.append(URLEncoder.encode(req.getTransactionInfo().getTxnAmount(), "UTF-8"));
        postsb.append("&");

        postsb.append(URLEncoder.encode("txnType", "UTF-8"));
        postsb.append("=");
        postsb.append(URLEncoder.encode(req.getTxnType(), "UTF-8"));
        postsb.append("&");


        byte postData[] = postsb.toString().getBytes("utf-8");

        String rawres = HttpUtil.postData(posturl, postData, false, HttpUtil.WWW_FORM);
        if(rawres==null) {
            throw new IOException("Unable to authenticate!");
        }

        Log.d(TAG, "rawres: " + rawres);
        //JSONObject resObj = null;
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();//new Gson();
        JsonParser jsonParser = new JsonParser();
        com.google.gson.JsonObject resObj = null;

        //Map<String,String> resMap = null;

        try {
            resObj = (com.google.gson.JsonObject) jsonParser.parse(rawres).getAsJsonObject();
        } catch(Exception e) {
            throw new IOException("Unable to get reply from server! Bad response from server!"+e);
        }
        Log.d(TAG, "resObj: " + resObj);

        String msgHash = gson.fromJson(resObj.get("msgHash"), String.class);
        String merTxnId = gson.fromJson(resObj.get("merTxnId"), String.class);
        txnDateYYYYMMDD = gson.fromJson(resObj.get("txnDate"), String.class);

        req.setMsgHash(msgHash);
        req.getTransactionInfo().setMerTxnId(merTxnId);

        return req;
    }

    public static String doStatusCheck(WPayInitRequest req, String wibmoTxnId) throws Exception {
        String posturl = "https://"+ getMerchantDomain() +"/sampleMerchant/taxi/txnStatus.jsp";

        StringBuilder postsb = new StringBuilder();
        postsb.append("merTxnId=").append(URLEncoder.encode(req.getTransactionInfo().getMerTxnId(), "utf-8"));
        postsb.append("&txnAmount=").append(URLEncoder.encode(req.getTransactionInfo().getTxnAmount(), "utf-8"));
        if(txnDateYYYYMMDD!=null) {
            postsb.append("&txnDate=").append(URLEncoder.encode(txnDateYYYYMMDD, "utf-8"));
        } else {
            System.out.println("You dont have txnDateYYYYMMDD!!");
        }
        if(wibmoTxnId!=null) {
            postsb.append("&wibmoTxnId=").append(URLEncoder.encode(wibmoTxnId, "utf-8"));
        }
        postsb.append("&");

        postsb.append(URLEncoder.encode("txnType", "UTF-8"));
        postsb.append("=");
        postsb.append(URLEncoder.encode(req.getTxnType(), "UTF-8"));
        postsb.append("&");

        Log.d(TAG, "postData: " + postsb.toString());
        byte postData[] = postsb.toString().getBytes("utf-8");

        String rawres = HttpUtil.postData(posturl, postData, false, HttpUtil.WWW_FORM);
        if (rawres == null) {
            throw new IOException("Unable to check status!");
        }

        rawres = rawres.trim();
        Log.d(TAG, "rawres: " + rawres);

        return rawres;
    }

    public static String doCharge(WPayInitRequest req, String wibmoTxnId) throws Exception {
        String posturl = "https://"+ getMerchantDomain() +"/sampleMerchant/taxi/charge.jsp";

        StringBuilder postsb = new StringBuilder();
        postsb.append("merTxnId=").append(URLEncoder.encode(req.getTransactionInfo().getMerTxnId(), "utf-8"));
        postsb.append("&txnAmount=").append(URLEncoder.encode(req.getTransactionInfo().getTxnAmount(), "utf-8"));
        if(txnDateYYYYMMDD!=null) {
            postsb.append("&txnDate=").append(URLEncoder.encode(txnDateYYYYMMDD, "utf-8"));
        } else {
            System.out.println("You dont have txnDateYYYYMMDD!!");
        }
        if(wibmoTxnId!=null) {
            postsb.append("&wibmoTxnId=").append(URLEncoder.encode(wibmoTxnId, "utf-8"));
        }
        postsb.append("&");

        postsb.append(URLEncoder.encode("txnType", "UTF-8"));
        postsb.append("=");
        postsb.append(URLEncoder.encode(req.getTxnType(), "UTF-8"));
        postsb.append("&");

        Log.d(TAG, "postData: " + postsb.toString());
        byte postData[] = postsb.toString().getBytes("utf-8");

        String rawres = HttpUtil.postData(posturl, postData, false, HttpUtil.WWW_FORM);
        if (rawres == null) {
            throw new IOException("Unable to check status!");
        }

        rawres = rawres.trim();
        Log.d(TAG, "rawres: " + rawres);

        return rawres;
    }

    public static String getMerchantDomain() {
        return merchantDomain;
    }

    public static void setMerchantDomain(String merchantDomain) {
        MerchantHandler.merchantDomain = merchantDomain;
    }
}
