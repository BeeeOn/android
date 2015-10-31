package com.rehivetech.beeeon.gui.dialog;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.avast.android.dialogs.core.BaseDialogFragment;
import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.fragment.DeviceDetailFragment;
import com.rehivetech.beeeon.gui.fragment.DeviceDetailGroupModuleFragment;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.util.UnitsHelper;

import java.util.ArrayList;

import eu.rekisoft.android.numberpicker.NumberPicker;

/**
 * Created by leo on 24.4.15.
 */
public class NumberPickerDialogFragment extends SimpleDialogFragment {

	public static String TAG = "jayne";

	private static Module sModule;
	private static FragmentActivity mActivity;
	private static Fragment mFragment;

	public static void show(FragmentActivity activity, Module module, Fragment frg) {
		sModule = module;
		mActivity = activity;
		mFragment = frg;
		new NumberPickerDialogFragment().show(activity.getSupportFragmentManager(), TAG);
	}

	@Override
	public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
		builder.setTitle(sModule.getName(mActivity));
		LayoutInflater li = LayoutInflater.from(getActivity());
		View view = li.inflate(R.layout.fragment_dialog_number_picker, null);

		final NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.dialog_number_picker_numberpicker);
		TextView unitView = (TextView) view.findViewById(R.id.dialog_number_picker_unit);
		double value = sModule.getValue().getDoubleValue();
		final ArrayList<String> tmp = new ArrayList<>();
		for (double i = value - 40.0; i < value + 40.0; i += 0.5) {
			tmp.add(String.valueOf(i));
		}

		numberPicker.setDisplayedValues(tmp.toArray(new String[tmp.size()]));
		numberPicker.setMinValue(0);
		numberPicker.setMaxValue(159);
		numberPicker.setValue(80);

		// UserSettings can be null when user is not logged in!
		SharedPreferences prefs = Controller.getInstance(mActivity).getUserSettings();
		if (prefs != null) {
			unitView.setText(new UnitsHelper(prefs, mActivity).getStringUnit(sModule.getValue()));
		}

		builder.setView(view);
		builder.setPositiveButton(getString(R.string.activity_fragment_btn_set), new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//
				Log.d(TAG, "NUMBER PICKER selected index" + numberPicker.getValue() + " value " + numberPicker.getDisplayedValues()[numberPicker.getValue()]);
				if (mFragment instanceof DeviceDetailFragment) {
					((DeviceDetailFragment) mFragment).onSetTemperatureClick(
							Double.parseDouble(numberPicker.getDisplayedValues()[numberPicker.getValue()]),
							sModule.getId());
				} else {
					((DeviceDetailGroupModuleFragment) mFragment).onSetTemperatureClick(
							Double.parseDouble(numberPicker.getDisplayedValues()[numberPicker.getValue()]),
							sModule.getId());
				}
				dismiss();
			}
		});
		builder.setNegativeButton(getString(R.string.activity_fragment_btn_cancel), new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		return builder;
	}
}
