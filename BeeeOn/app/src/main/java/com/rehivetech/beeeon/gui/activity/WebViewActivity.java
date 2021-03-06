package com.rehivetech.beeeon.gui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.rehivetech.beeeon.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author mlyko
 * @since 13.04.2016
 */
public class WebViewActivity extends BaseApplicationActivity {

	public static final String EXTRA_URL_ADDRESS = "url_address";
	@BindView(R.id.webview_progressbar) ProgressBar mWebviewProgressbar;
	@BindView(R.id.webview_container) WebView mWebView;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_view);
		setupToolbar("", INDICATOR_DISCARD);
		ButterKnife.bind(this);

		String urlAddress = getIntent().getStringExtra(EXTRA_URL_ADDRESS);
		if (urlAddress == null) {
			Toast.makeText(this, R.string.error_no_url, Toast.LENGTH_LONG).show();
			finish();
		}

		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setNetworkAvailable(true);
		mWebView.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				mWebviewProgressbar.setVisibility(View.GONE);
			}
		});
		mWebView.loadUrl(urlAddress);
	}
}
