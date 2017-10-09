package com.rehivetech.beeeon.gui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.gui.activity.WebAuthActivity;

public class OAuthRedirectActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startActivity(WebAuthActivity.createResponseHandlingIntent(
                this, getIntent().getData()));
        finish();
    }
}