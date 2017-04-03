package com.rehivetech.beeeon.network.authentication;

import android.content.Context;
import android.content.Intent;

import com.rehivetech.beeeon.BuildConfig;
import com.rehivetech.beeeon.gui.activity.LoginActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author martin
 * @since 20/02/2017.
 */

public class PresentationAuthProvider implements IAuthProvider {

    public static final int PROVIDER_ID = 203;

    private HashMap<String, String> mParams = new HashMap<>();
    private String mProvider;

    public PresentationAuthProvider(Context context) {
        if (BuildConfig.BUILD_TYPE.equals("presentation")) {

            try {
                InputStream inputStream = context.getAssets().open("auth.properties");
                Properties properties = new Properties();
                properties.load(inputStream);
                mParams.put("email", properties.getProperty("login"));
                mParams.put("password", properties.getProperty("password"));

                mProvider = properties.getProperty("provider");

            } catch (IOException e) {
                throw new RuntimeException("auth.properties file not found");
            }
        }
    }

    @Override
    public boolean isDemo() {
        return false;
    }

    @Override
    public String getProviderName() {
        return mProvider;
    }

    @Override
    public Map<String, String> getParameters() {
        return mParams;
    }

    @Override
    public void setTokenParameter(String tokenParameter) {

    }

    @Override
    public boolean loadAuthIntent(Intent data) {
        return true;
    }

    @Override
    public void prepareAuth(LoginActivity activity) {
        activity.onActivityResult(PROVIDER_ID, IAuthProvider.RESULT_AUTH, null);
    }
}
