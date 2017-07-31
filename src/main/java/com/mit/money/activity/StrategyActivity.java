package com.mit.money.activity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mit.mitanalytics.UmengAgentActivity;
import com.mit.money.R;
import com.mit.money.view.ProgressWebView;

/**
 * Created by android on 3/2/16.
 */
public class StrategyActivity extends UmengAgentActivity {
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.robpacket_strategy);
        init();
    }

    private String getUrl() {
        return "http://baidu.com";
    }

    private void init() {
        url = getUrl();
        ProgressWebView webview = (ProgressWebView) findViewById(R.id.webView);
        if (url != null) {
            webview.loadUrl(url);
        }
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }
}
