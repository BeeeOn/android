package com.rehivetech.beeeon.gui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.Utils;

/**
 * Created by martin on 7.11.15.
 */
public class DeviceFeatureView extends LinearLayout{

	private Context mContext;

	private TextView mValue;
	private TextView mCaption;


	public DeviceFeatureView(Context context) {
		super(context);
		init(context);
	}

	public DeviceFeatureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public DeviceFeatureView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public DeviceFeatureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	private void init(Context context) {
		mContext = context;

		View view = LayoutInflater.from(context).inflate(R.layout.device_feature_item, this, true);

		mValue = (TextView) view.findViewById(R.id.device_feature_item_value);
		mCaption = (TextView) view.findViewById(R.id.device_feature_item_caption);
		setOrientation(VERTICAL);

		int padding = Utils.convertDpToPixel(16f);
		setPadding(0, 0, padding, 0);
		setVisibility(INVISIBLE);
	}

	public void setValue(String value) {
		if (value.length() > 0) {
			setVisibility(VISIBLE);
			mValue.setText(value);
		}
	}

	public void setCaption(String caption) {
		mCaption.setText(caption);
	}

	public void setIcon(@DrawableRes int iconRes) {
		Drawable icon = ContextCompat.getDrawable(mContext, iconRes);
		int size = Utils.convertDpToPixel(24);
		icon.setBounds(0, 0, size, size);
		mValue.setCompoundDrawables(icon, null, null, null);
	}
}
