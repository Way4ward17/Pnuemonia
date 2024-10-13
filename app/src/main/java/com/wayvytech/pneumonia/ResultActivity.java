package com.samueldeveloper.dogbreed120;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class ResultActivity extends AppCompatActivity {

    WebView webpay;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        webpay = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);
        Intent intent = getIntent();
        String str = intent.getStringExtra("dog_name");
        loadDog(str);
    }

    private void loadDog(String name) {
        webpay.loadUrl("https://en.wikipedia.org/wiki/"+name);
        WebSettings webSettings = webpay.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(false);

        webpay.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                progressBar.setVisibility(View.GONE);
                webpay.loadUrl("https://en.wikipedia.org/wiki/"+name);
            }

            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);


            }
        });
    }

    public void back(View view) {
        finish();
    }
}