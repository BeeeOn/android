package com.rehivetech.beeeon.gui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.view.FloatingActionButton;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.util.ChartHelper;

import java.util.List;

/**
 *  Created by martin on 7.2.16.
 */
public abstract class BaseAddDashBoardItemFragment extends BaseApplicationFragment{

	protected static final String ARG_GATE_ID = "gate_id";

	protected String mGateId;

	protected TextInputLayout mTextInputLayout;
	protected EditText mItemNameEditText;

	protected FloatingActionButton mButtonDone;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();

		if (args != null) {
			mGateId = args.getString(ARG_GATE_ID);
		}
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mTextInputLayout = (TextInputLayout) view.findViewById(R.id.fragment_add_dashboard_item_text_input);
		mItemNameEditText = (EditText) view.findViewById(R.id.fragment_add_dashboard_item_name_edit_text);
		mButtonDone = (FloatingActionButton) view.findViewById(R.id.fragment_add_dashboard_item_button_done);
	}

	/**
	 * Create adapter with all modules from devices
	 * @param context app context
	 * @param layout layout resource id for item
	 * @param devicesList list of all devices
	 * @param addEmptyFirst if true, empty item will be added
	 * @return adapter
	 */
	public ArrayAdapter<SpinnerHolder> createModulesAdapter(Context context, @LayoutRes int layout, List<Device> devicesList, boolean addEmptyFirst) {
		ArrayAdapter<SpinnerHolder> adapter = new ArrayAdapter<>(context, layout);

		if (addEmptyFirst) {
			adapter.add(new SpinnerHolder(null,null));
		}

		for (Device device : devicesList) {
			for (Module module : device.getAllModules(false)) {
				adapter.add(new SpinnerHolder(device, module));
			}
		}
		return adapter;
	}

	/**
	 * Create adapter with graph ranges
	 * @param context app context
	 * @param layout layout resource id for item
	 * @return adapter
	 */
	public ArrayAdapter<String> createGraphRangeAdapter(Context context, @LayoutRes int layout) {
		ArrayAdapter<String> graphRangeAdapter = new ArrayAdapter<>(context, layout);
		for (int range : ChartHelper.ALL_RANGES) {
			graphRangeAdapter.add(getString(ChartHelper.getIntervalString(range)));
		}
		return graphRangeAdapter;
	}

	/**
	 * Holder for spinner module item
	 */
	protected final class SpinnerHolder {

		private Device mDevice;
		private Module mModule;

		public SpinnerHolder(@Nullable Device device, @Nullable Module module) {
			mDevice = device;
			mModule = module;
		}

		public Device getDevice() {
			return mDevice;
		}

		public Module getModule() {
			return mModule;
		}

		@Override
		public String toString() {
			if (mDevice == null || mModule == null) {
				return "";
			}
			return String.format("%s - %s", mDevice.getName(mActivity), mModule.getName(mActivity));
		}
	}
}