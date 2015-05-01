package com.rehivetech.beeeon.activity.dialog;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.avast.android.dialogs.core.BaseDialogBuilder;
import com.avast.android.dialogs.core.BaseDialogFragment;
import com.avast.android.dialogs.iface.INegativeButtonDialogListener;
import com.avast.android.dialogs.iface.IPositiveButtonDialogListener;
import com.rehivetech.beeeon.R;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Tomáš on 25. 4. 2015.
 */
public class LocationPickerDialogFragment extends BaseDialogFragment {
	public static String TAG = "location_picker";

	public static final String ARG_TITLE = "title";
	public static final String ARG_CITY_NAME = "city_name";
	public static final String ARG_POSITIVE_BUTTON_TEXT = "positive_button_text";
	private static final String ARG_NEGATIVE_BUTTON_TEXT = "negative_button_text";

	EditText mCityName;

	public LocationPickerDialogFragment(){}

	public static LocationPickerDialogFragment.LocationPickerDialogBuilder createBuilder(Context context, FragmentManager fragmentManager){
		return new LocationPickerDialogFragment.LocationPickerDialogBuilder(context, fragmentManager);
	}

	protected List<ILocationPickerDialogListener> getDialogListeners() {
		return this.getDialogListeners(ILocationPickerDialogListener.class);
	}

	@Override
	public Builder build(Builder builder){
		LayoutInflater inflater = builder.getLayoutInflater();
		View view = inflater.inflate(R.layout.fragment_dialog_location_picker, null, false);

		this.mCityName = (EditText) view.findViewById(R.id.dialog_location_city_name);
		this.mCityName.setText(this.getArguments().getString(ARG_CITY_NAME));

		builder
			.setTitle(this.getArguments().getString(ARG_TITLE))
			.setView(view);

		String positiveButtonText = this.getArguments().getString(ARG_POSITIVE_BUTTON_TEXT);
		if(!TextUtils.isEmpty(positiveButtonText)) {
			builder.setPositiveButton(positiveButtonText, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Iterator var2 = LocationPickerDialogFragment.this.getDialogListeners().iterator();
					while(var2.hasNext()){
						ILocationPickerDialogListener listener = (ILocationPickerDialogListener) var2.next();
						listener.onPositiveButtonClicked(LocationPickerDialogFragment.this.mRequestCode, mCityName, LocationPickerDialogFragment.this);
					}
				}
			});
		}

		String negativeButtonText = this.getArguments().getString(ARG_NEGATIVE_BUTTON_TEXT);
		if(!TextUtils.isEmpty(negativeButtonText)) {
			builder.setNegativeButton(negativeButtonText, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Iterator var2 = LocationPickerDialogFragment.this.getDialogListeners().iterator();
					while (var2.hasNext()) {
						ILocationPickerDialogListener listener = (ILocationPickerDialogListener) var2.next();
						listener.onNegativeButtonClicked(LocationPickerDialogFragment.this.mRequestCode, mCityName, LocationPickerDialogFragment.this);
					}
				}
			});
		}

		return builder;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(this.getArguments() == null){
			throw new IllegalArgumentException("use LocationPickerDialogBuilder to construct this dialog");
		}
	}

	public static class LocationPickerDialogBuilder extends BaseDialogBuilder<LocationPickerDialogFragment.LocationPickerDialogBuilder>{
		private String mCityName;
		private String mTitle;
		private String mPositiveButtonText;
		private String mNegativeButtonText;

		protected LocationPickerDialogBuilder(Context context, FragmentManager fragmentManager){
			super(context, fragmentManager, LocationPickerDialogFragment.class);
		}

		@Override
		protected LocationPickerDialogBuilder self() {
			return this;
		}

		public LocationPickerDialogFragment.LocationPickerDialogBuilder setTitle(String title){
			this.mTitle = title;
			return this;
		}

		public LocationPickerDialogFragment.LocationPickerDialogBuilder setCityName(String cityName){
			this.mCityName = cityName;
			return this;
		}

		public LocationPickerDialogFragment.LocationPickerDialogBuilder setPositiveButtonText(String text){
			this.mPositiveButtonText = text;
			return this;
		}

		public LocationPickerDialogFragment.LocationPickerDialogBuilder setNegativeButtonText(String text) {
			this.mNegativeButtonText = text;
			return this;
		}


		@Override
		protected Bundle prepareArguments() {
			Bundle args = new Bundle();
			args.putString(ARG_TITLE, this.mTitle);
			args.putString(ARG_CITY_NAME, this.mCityName);
			args.putString(ARG_POSITIVE_BUTTON_TEXT, this.mPositiveButtonText);
			args.putString(ARG_NEGATIVE_BUTTON_TEXT, this.mNegativeButtonText);
			return args;
		}
	}
}