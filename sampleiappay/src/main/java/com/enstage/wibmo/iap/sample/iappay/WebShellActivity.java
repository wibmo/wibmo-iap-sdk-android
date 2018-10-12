package com.enstage.wibmo.iap.sample.iappay;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.enstage.wibmo.sdk.WibmoSDK;
import com.enstage.wibmo.sdk.inapp.InAppShellHepler;


public class WebShellActivity extends AppCompatActivity {
    private static final String TAG = WebShellActivity.class.getSimpleName();

    private WebView webView;

    private InAppShellHepler inAppShellHepler;


    //config - staging
    //String shellMerchantUrl = "https://wallet.pc.enstage-sas.com/testWebMerchant";
    //String iapPackage = "com.enstage.wibmo.sdk.inapp.staging";
    //String iapDomainToPost = "https://wallet.pc.enstage-sas.com";

    //config - prod
    String shellMerchantUrl = "https://www.wibmo.com/testWebMerchant";
    String iapPackage = "com.enstage.wibmo.sdk.inapp.main";
    String iapDomainToPost = "https://www.wibmo.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_shell);


        webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);

        //-- wibmo iap sdk code ---
        inAppShellHepler = new InAppShellHepler();
        inAppShellHepler.setActivity(this);
        inAppShellHepler.setWebView(webView);

        inAppShellHepler.injectIAP();

        inAppShellHepler.initSDK(
                iapPackage,
                iapDomainToPost);
        //-- wibmo iap sdk code ---

        webView.loadUrl(shellMerchantUrl);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WibmoSDK.REQUEST_CODE_IAP_PAY) {
            inAppShellHepler.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_web_shell, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_browser) {
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(shellMerchantUrl));
            marketIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            startActivity(marketIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
