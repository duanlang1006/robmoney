package com.mit.money.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.DownloadListener;
import android.webkit.WebView;

import com.mit.mitanalytics.UmengAgentActivity;

import com.mit.money.R;

/**
 * Created by langduan on 16/3/29.
 */
public class WebViewActivity extends UmengAgentActivity {

    public static Intent getIntent(Context context, String url, String name) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("name", name);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_layout);
        WebView webview = (WebView) findViewById(R.id.webview);

        String url = getIntent().getStringExtra("url");
        String name = getIntent().getStringExtra("name");

        webview.getSettings().setJavaScriptEnabled(true);
        webview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                if (url != null && url.startsWith("http://"))
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        });
        webview.loadUrl(url);
    }

}
