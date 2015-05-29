package com.rehivetech.beeeon.gui.dialog;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.avast.android.dialogs.core.BaseDialogFragment;
import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.fragment.SensorDetailFragment;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.UnitsHelper;

import net.simonvt.numberpicker.NumberPicker;

import java.util.ArrayList;

/**
 * Created by leo on 24.4.15.
 */
public class NumberPickerDialogFragment extends SimpleDialogFragment {

	public static String TAG = "jayne";

	private static Module sMModule;
	private static FragmentActivity mActivity;
	private static Fragment mFragment;

	public static void show(FragmentActivity activity, Module module, Fragment frg) {
		sMModule = module;
		mActivity = activity;
		mFragment = frg;
		new NumberPickerDialogFragment().show(activity.getSupportFragmentManager(), TAG);
	}

	@Override
	public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
		builder.setTitle(getString(R.string.dialog_title_set_temperature));
		LayoutInflater li = LayoutInflater.from(getActivity());
		View view = li.inflate(R.layout.beeeon_dialog_numberpicker, null);

		final NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.beeeon_numberPicker);
		TextView unitView = (TextView) view.findViewById(R.id.beeeon_numberpicker_unit);
		double value = sMModule.getValue().getDoubleValue();
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

		UnitsHelper mUnitsHelper = (prefs == null) ? null : new UnitsHelper(prefs, mActivity);
		unitView.setText(mUnitsHelper.getStringUnit(sMModule.getValue()));

		builder.setView(view);
		builder.setPositiveButton(getString(R.string.dialog_set_boiler_setaction), new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//
				Log.d(TAG, "NUMBER PICKER selected index" + numberPicker.getValue() + " value " + numberPicker.getDisplayedValues()[numberPicker.getValue()]);
				((SensorDetailFragment) mFragment).onSetTemperatureClick(Double.parseDouble(numberPicker.getDisplayedValues()[numberPicker.getValue()]));
				dismiss();
			}
		});
		builder.setNegativeButton(getString(R.string.notification_cancel), new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		return builder;
	}
}
