package com.rehivetech.beeeon.gui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.rehivetech.beeeon.network.authentication.GoogleAuthProvider;

import java.util.Locale;

public class WebAuthActivity extends AppCompatActivity {

	public static final String EXTRA_PROVIDER_ID = "provider_id";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Init views and layout
		WebView webView = initWebView();
		ProgressBar progressBar = initProgressBar();

		FrameLayout layout = new FrameLayout(this);
		layout.addView(progressBar);
		layout.addView(webView);
		setContentView(layout);

		// Set canceled result in case of back press
		setResult(RESULT_CANCELED);

		// Prepare working from authProvider
		Intent data = getIntent();
		if (data == null || !data.hasExtra(EXTRA_PROVIDER_ID)) {
			throw new IllegalStateException("Activity must be called with EXTRA_PROVIDER_ID intent data.");
		}

		final int providerId = data.getIntExtra(EXTRA_PROVIDER_ID, 0);
		switch (providerId) {
			case GoogleAuthProvider.PROVIDER_ID: {
				GoogleAuthProvider.GoogleWebViewClient webViewClient = new GoogleAuthProvider.GoogleWebViewClient(this);
				webView.setWebViewClient(webViewClient);
				webView.loadUrl(webViewClient.getLoadUrl());
				break;
			}
			default: {
				throw new IllegalStateException(String.format(Locale.getDefault(), "Unknown provider (%d)", providerId));
			}
		}
	}

	private ProgressBar initProgressBar() {
		ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER);

		progressBar.setLayoutParams(params);
		progressBar.setIndeterminate(true);
		progressBar.setVisibility(View.VISIBLE);

		return progressBar;
	}

	@SuppressLint("setJavascriptEnabled")
	private WebView initWebView() {
		WebView webView = new WebView(this);

		webView.setVisibility(View.INVISIBLE);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setNetworkAvailable(true);

		return webView;
	}
}
