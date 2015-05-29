package com.rehivetech.beeeon.gui.activity;

import android.app.Activity;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.rehivetech.beeeon.R;

/*
 Převzato z https://github.com/AndroidDeveloperLB/ActionBarPreferenceActivity
 */
public abstract class ActionBarPreferenceActivity extends PreferenceActivity {
	private Toolbar _toolbar;
	private View _shadowView;

	protected abstract int getPreferencesXmlId();

	public Toolbar getToolbar() {
		return _toolbar;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.abp__activity_preference);
		_toolbar = (Toolbar) findViewById(R.id.abp__toolbar);
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			_shadowView = findViewById(R.id.abp__shadowView);
			final ViewGroup parent = (ViewGroup) _shadowView.getParent();
			parent.removeView(_shadowView);
			_shadowView = null;
		}
		addPreferencesFromResource(getPreferencesXmlId());
		_toolbar.setClickable(true);
		_toolbar.setNavigationIcon(getResIdFromAttribute(this, R.attr.homeAsUpIndicator));
		_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				finish();
			}
		});
	}

	private static int getResIdFromAttribute(final Activity activity, final int attr) {
		if (attr == 0)
			return 0;
		final TypedValue typedvalueattr = new TypedValue();
		activity.getTheme().resolveAttribute(attr, typedvalueattr, true);
		return typedvalueattr.resourceId;
	}

	protected void setEnabledActionBarShadow(final boolean enable) {
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP)
			ViewCompat.setElevation(_toolbar, enable ? 4 : 0);
		else {
			if (_shadowView == null)
				_shadowView = findViewById(R.id.abp__shadowView);
			_shadowView.setVisibility(enable ? View.VISIBLE : View.GONE);
		}
	}
}
