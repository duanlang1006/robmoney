package com.mit.money.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mit.mitanalytics.UmengAgentActivity;
import com.mit.money.utils.SpUtil;
import com.umeng.onlineconfig.OnlineConfigAgent;
import com.mit.money.R;

/**
 * Created by langduan on 16/3/29.
 */
public class QrCodeWebActivity extends UmengAgentActivity {


    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrcode_web_layout);

        String url = OnlineConfigAgent.getInstance().getConfigParams(this,
                SpUtil.ONLINE_KEY_ENTER_RUL);
        if (url == null || url.isEmpty() || url.equals("null")) {
            url = "http://www.fuli365.net/intelligent_red_package";
        }

        WebView webview = (WebView) this.findViewById(R.id.webView);
        WebSettings webSettings = webview.getSettings();
        webSettings.setBuiltInZoomControls(true);
        webview.loadUrl(url + "/qrcode.html");
        webview.setWebViewClient(new webViewClient());
    }

    private class webViewClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
