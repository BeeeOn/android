package com.rehivetech.beeeon.activity.dialog;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rehivetech.beeeon.R;

/**
 * Created by Martin on 1. 4. 2015.
 */
public class GeofenceDialogFragment extends DialogFragment {

	private static final String KEY_LONG = "long";
	private static final String KEY_LAT = "lat";
	/**
	 * Define how many meters is add/removed from geofence when tap on plus/minus button
	 */
	private static final int BUTTON_STEP = 20;
	/**
	 * Define minimum radius in meters
	 */
	private static final int MIN_RADIUS = 50;

	/**
	 * Define default radius in meters
	 */
	private static final int DEFAULT_RADIUS = 100;

	private Button mButtonMinus, mButtonPlus;
	private EditText mName, mRadius;
	private TextView mRadiusTitle;
	private double mLong, mLat;
	private GeofenceCrateCallback mCallback;
	private Button mPositiveButton;

	public GeofenceDialogFragment() {
		// Empty constructor required for DialogFragment
	}

	public static GeofenceDialogFragment newInstance(double lat, double lon) {
		GeofenceDialogFragment f = new GeofenceDialogFragment();

		Bundle args = new Bundle();
		args.putDouble(KEY_LAT, lat);
		args.putDouble(KEY_LONG, lon);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLong = getArguments().getDouble(KEY_LONG);
		mLat = getArguments().getDouble(KEY_LAT);
	}

	// make sure the Activity implemented it
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			this.mCallback = (GeofenceCrateCallback) activity;
		} catch (final ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement GeofenceCrateCallback");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_geofence, null);

		mButtonMinus = (Button) view.findViewById(R.id.geofence_radius_minus);
		mButtonPlus = (Button) view.findViewById(R.id.geofence_radius_plus);

		mName = (EditText) view.findViewById(R.id.geofence_name);
		mRadius = (EditText) view.findViewById(R.id.geofence_radius_input);

		mRadiusTitle = (TextView) view.findViewById(R.id.geofence_radius_title);
		mRadiusTitle.setText(getString(R.string.radius) + " (" + getString(R.string.unit_meter_short) + ")");

		mButtonMinus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				int radius;
				try {
					radius = Integer.valueOf(mRadius.getText().toString());
				} catch (NumberFormatException e) {
					mPositiveButton.setEnabled(false);
					return;
				}
				if (radius >= MIN_RADIUS + BUTTON_STEP) {
					radius = radius - BUTTON_STEP;
				}
				mRadius.setText(String.valueOf(radius));
			}
		});

		mButtonPlus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				int radius;
				try {
					radius = Integer.valueOf(mRadius.getText().toString());
				} catch (NumberFormatException e) {
					mPositiveButton.setEnabled(false);
					return;
				}
				radius = radius + BUTTON_STEP;
				mRadius.setText(String.valueOf(radius));
			}
		});

		mName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
				if(charSequence.length() > 0) {
					mPositiveButton.setEnabled(true);
				} else {
					mPositiveButton.setEnabled(false);
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});

		mRadius.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
				int number;
				try {
					number = Integer.valueOf(charSequence.toString());
				} catch (NumberFormatException e) {
					mPositiveButton.setEnabled(false);
					return;
				}

				if (number >= MIN_RADIUS) {
					mPositiveButton.setEnabled(true);
				} else {
					mPositiveButton.setEnabled(false);
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});

		return builder
				.setView(view)
				.setTitle(R.string.add_new_geofence_area)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								int radius;
								try {
									radius = Integer.valueOf(mRadius.getText().toString());
								} catch (NumberFormatException e) {
									return;
								}
								String name = mName.getText().toString();

								mCallback.onCreateGeofence(name, radius, mLat, mLong);
							}
						}
				)
				.setNegativeButton(R.string.notification_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								// nothing to do
							}
						}
				)
				.create();
	}

	@Override
	public void onStart() {
		super.onStart();
		AlertDialog dialog = (AlertDialog) getDialog();
		if (dialog != null) {
			mPositiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
			mPositiveButton.setEnabled(false);
		}

	}

	public interface GeofenceCrateCallback {
		void onCreateGeofence(String name, int radius, double lat, double lon);
	}
}
