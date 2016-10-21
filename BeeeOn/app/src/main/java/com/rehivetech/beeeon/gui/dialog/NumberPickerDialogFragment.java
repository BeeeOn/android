package com.rehivetech.beeeon.gui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.avast.android.dialogs.core.BaseDialogBuilder;
import com.avast.android.dialogs.core.BaseDialogFragment;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.units.BaseUnit;
import com.rehivetech.beeeon.household.device.values.BaseValue;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


/**
 * @author martin
 * @since 31.10.15.
 */
public class NumberPickerDialogFragment extends BaseDialogFragment {

	private static final String ARG_TITLE = "title";
	private static final String ARG_POSITIVE_BUTTON_TEXT = "positive_button_text";
	private static final String ARG_NEGATIVE_BUTTON_TEXT = "negative_button_text";
	private static final String ARG_MIN_VALUE = "min_value";
	private static final String ARG_MAX_VALUE = "max_value";
	private static final String ARG_GRANULARITY = "granularity";
	private static final String ARG_ACTUAL_VALUE = "actual_value";
	private static final String ARG_VALUES_UNIT = "values_unit";
	private static final String ARG_MODULE_ID = "module_id";


	public static void showNumberPickerDialog(AppCompatActivity context, Module module, SharedPreferences prefs, Fragment targetFragment) {
		BaseUnit.Item item = module.getValue().getUnit().fromSettings(prefs);
		BaseValue.Constraints constraints = module.getValue().getConstraints();

		if (constraints == null) {
			return;
		}

		BaseUnit unit = module.getValue().getUnit();
		Double minValue = unit.convertValue(item, constraints.getMin());
		Double maxValue = unit.convertValue(item, constraints.getMax());
		Double actaulValue = unit.convertValue(item, module.getValue().getDoubleValue());

		NumberPickerDialogFragment.createBuilder(context, context.getSupportFragmentManager())
				.setTitle(module.getName(context))
				.setMinValue(minValue)
				.setMaxValue(maxValue)
				.setValueGranularity(constraints.getGranularity())
				.setPositiveButtonText(context.getString(R.string.activity_fragment_btn_set))
				.setNegativeButtonText(context.getString(R.string.activity_fragment_btn_cancel))
				.setActualValue(actaulValue)
				.setValuesUnit(item)
				.setModuleId(module.getId())
				.setTargetFragment(targetFragment, 0)
				.show();
	}

	private static NumberPickerDialogBuilder createBuilder(Context context, FragmentManager fragmentManager) {
		return new NumberPickerDialogBuilder(context, fragmentManager);
	}

	@Override
	public void onPause() {
		super.onPause();
		dismiss();
	}

	@Override
	@SuppressLint("InflateParams")
	protected Builder build(Builder builder) {
		LayoutInflater inflater = builder.getLayoutInflater();
		final View view = inflater.inflate(R.layout.fragment_dialog_number_picker, null, false);

		final NumberPicker numberPickerWhole = (NumberPicker) view.findViewById(R.id.dialog_number_picker_numberpicker_whole);
		final NumberPicker numberPickerDecimal = (NumberPicker) view.findViewById(R.id.dialog_number_picker_numberpicker_decimal);
		final TextView unitTextView = (TextView) view.findViewById(R.id.dialog_number_picker_unit);
		final TextView decimalPoint = (TextView) view.findViewById(R.id.dialog_number_picker_decimal_point);

		Bundle args = getArguments();

		final String moduleId = args.getString(ARG_MODULE_ID);

		final BaseUnit.Item unitItem = args.getParcelable(ARG_VALUES_UNIT);

		if (unitItem != null) {
			unitTextView.setText(unitItem.getStringUnit(getContext()));
		}

		builder.setTitle(args.getString(ARG_TITLE));

		String positiveButtonText = args.getString(ARG_POSITIVE_BUTTON_TEXT);

		final List<String> wholeSteps = new ArrayList<>();
		final List<String> decimalSteps = new ArrayList<>();

		if (!TextUtils.isEmpty(positiveButtonText)) {

			builder.setPositiveButton(positiveButtonText, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					for (SetNewValueListener listener : NumberPickerDialogFragment.this.getListeners()) {
						String actualWholeVal = wholeSteps.get(numberPickerWhole.getValue());
						String actualDecimalVal = "";
						if (numberPickerDecimal.getVisibility() == View.VISIBLE) {
							actualDecimalVal = decimalSteps.get(numberPickerDecimal.getValue());
						}
						String actualValue;
						if (actualDecimalVal.isEmpty()) {
							actualValue = actualWholeVal;
						} else {
							actualValue = String.format("%s.%s", actualWholeVal, actualDecimalVal);
						}

						listener.onSetNewValue(moduleId, actualValue, unitItem);
						dismiss();
					}
				}
			});
		}

		String negativeButtonText = args.getString(ARG_NEGATIVE_BUTTON_TEXT);
		if (!TextUtils.isEmpty(negativeButtonText)) {

			builder.setNegativeButton(negativeButtonText, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		}

		Double minValue = args.getDouble(ARG_MIN_VALUE);
		Double maxValue = args.getDouble(ARG_MAX_VALUE);
		Double granularity = args.getDouble(ARG_GRANULARITY);
		Double actualValue = args.getDouble(ARG_ACTUAL_VALUE);

		if (granularity > 1) {

			for (double i = minValue; i < maxValue; i += granularity) {
				wholeSteps.add(String.valueOf(i));
			}
		} else {

			for (int i = minValue.intValue(); i < maxValue.intValue(); i += Math.ceil(granularity)) {
				wholeSteps.add(String.valueOf(i));
			}
		}

		numberPickerWhole.setDisplayedValues(wholeSteps.toArray(new String[wholeSteps.size()]));
		numberPickerWhole.setMinValue(0);
		numberPickerWhole.setMaxValue(wholeSteps.size() - 1);
		numberPickerWhole.setValue(wholeSteps.indexOf(String.valueOf(actualValue.intValue())));

		if (granularity != Math.floor(granularity) && granularity <= 1) {
			Timber.d("granularity is with decimal part");

			numberPickerDecimal.setVisibility(View.VISIBLE);
			decimalPoint.setVisibility(View.VISIBLE);

			double granularityDecimalPart = granularity - granularity.intValue();

			for (double i = 0; i < 1; i += granularityDecimalPart) {
				decimalSteps.add(String.valueOf(i).substring(2));
			}

			numberPickerDecimal.setDisplayedValues(decimalSteps.toArray(new String[decimalSteps.size()]));
			numberPickerDecimal.setMinValue(0);
			numberPickerDecimal.setMaxValue(decimalSteps.size() - 1);

			double actualValueDecimalPart = actualValue - actualValue.intValue();
			numberPickerDecimal.setValue(decimalSteps.indexOf(String.valueOf(actualValueDecimalPart)));
		}

		builder.setView(view);

		//disable show keyboard by default
		getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		return builder;
	}

	public static class NumberPickerDialogBuilder extends BaseDialogBuilder<NumberPickerDialogBuilder> {

		private String mTitle;
		private String mPositiveButtonText;
		private String mNegativeButtonText;

		private Double mModuleMinValue;
		private Double mModuleMaxValue;
		private Double mModuleGranularity;
		private Double mActualValue;
		private BaseUnit.Item mUnit;
		private String mModuleId;

		public NumberPickerDialogBuilder(Context context, FragmentManager fragmentManager) {
			super(context, fragmentManager, NumberPickerDialogFragment.class);
		}

		@Override
		protected NumberPickerDialogBuilder self() {
			return this;
		}

		public NumberPickerDialogBuilder setTitle(String title) {
			mTitle = title;
			return this;
		}

		public NumberPickerDialogBuilder setPositiveButtonText(String text) {
			mPositiveButtonText = text;

			return this;
		}

		public NumberPickerDialogBuilder setNegativeButtonText(String text) {
			mNegativeButtonText = text;

			return this;
		}

		public NumberPickerDialogBuilder setMinValue(Double minValue) {
			mModuleMinValue = minValue;
			return this;
		}

		public NumberPickerDialogBuilder setMaxValue(Double maxValue) {
			mModuleMaxValue = maxValue;
			return this;
		}

		public NumberPickerDialogBuilder setValueGranularity(Double granularity) {
			mModuleGranularity = granularity;
			return this;
		}

		public NumberPickerDialogBuilder setActualValue(Double value) {
			mActualValue = value;

			return this;
		}

		public NumberPickerDialogBuilder setValuesUnit(BaseUnit.Item unit) {
			mUnit = unit;

			return this;
		}

		public NumberPickerDialogBuilder setModuleId(String moduleId) {
			mModuleId = moduleId;

			return this;
		}

		@Override
		protected Bundle prepareArguments() {
			Bundle args = new Bundle();

			args.putString(ARG_TITLE, mTitle);
			args.putString(ARG_POSITIVE_BUTTON_TEXT, mPositiveButtonText);
			args.putString(ARG_NEGATIVE_BUTTON_TEXT, mNegativeButtonText);
			args.putDouble(ARG_MIN_VALUE, mModuleMinValue);
			args.putDouble(ARG_MAX_VALUE, mModuleMaxValue);
			args.putDouble(ARG_GRANULARITY, mModuleGranularity);
			args.putDouble(ARG_ACTUAL_VALUE, mActualValue);
			args.putParcelable(ARG_VALUES_UNIT, mUnit);
			args.putString(ARG_MODULE_ID, mModuleId);

			return args;
		}
	}

	protected List<SetNewValueListener> getListeners() {
		return this.getDialogListeners(SetNewValueListener.class);
	}

	public interface SetNewValueListener {
		void onSetNewValue(String moduleId, String actualValue, BaseUnit.Item unit);
	}
}
