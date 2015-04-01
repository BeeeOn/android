package com.rehivetech.beeeon.activity.dialog;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
	private static final int BUTTON_STEP = 10;
	/**
	 * Define minimum radius in meters
	 */
	private static final int MIN_RADIUS = 10;
	Button mButtonMinus, mButtonPlus;
	EditText mName, mRadius;
	double mLong, mLat;
	private GeofenceCrateCallback mCallback;

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

		mButtonMinus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				int radius;
				try {
					radius = Integer.valueOf(mRadius.getText().toString());
				} catch (NumberFormatException e) {
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
					return;
				}
				radius = radius + BUTTON_STEP;
				mRadius.setText(String.valueOf(radius));
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

								//TODO zkontrolovat validnost vstupu

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


	public interface GeofenceCrateCallback {
		void onCreateGeofence(String name, int radius, double lat, double lon);
	}
}
