package com.rehivetech.beeeon.gui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.OverviewGraphItem;
import com.rehivetech.beeeon.gui.view.FloatingActionButton;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;

import static com.rehivetech.beeeon.gui.adapter.dashboard.DashboardModuleSelectAdapter.ModuleItem;

/**
 * Created by martin on 9.2.16.
 */
public class AddDashboardOverviewGraphItemFragment extends BaseAddDashBoardItemFragment {

	private static final String ARG_SELECTED_MODULE = "selected_module";

	ModuleItem mModuleItem;

	public static AddDashboardOverviewGraphItemFragment newInstance(String gateId, ModuleItem moduleItem) {

		Bundle args = new Bundle();
		args.putString(ARG_GATE_ID, gateId);
		args.putParcelable(ARG_SELECTED_MODULE, moduleItem);
		AddDashboardOverviewGraphItemFragment fragment = new AddDashboardOverviewGraphItemFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			mModuleItem = args.getParcelable(ARG_SELECTED_MODULE);
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		FrameLayout rootView = (FrameLayout) inflater.inflate(R.layout.fragment_add_dashboard_graph_item, container, false);

		View view;
		if (mModuleItem == null) {
			view = LayoutInflater.from(mActivity).inflate(R.layout.add_dashboard_recyclerview_item_layout1, null);
		} else {
			view = LayoutInflater.from(mActivity).inflate(R.layout.add_dashboard_graph_overview_item_layout2, null);
		}

		rootView.addView(view, 0);

		return rootView;
	}


	@Override
	public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
		if (mModuleItem == null) {
			super.onViewCreated(view, savedInstanceState);
			TextView textView = (TextView) view.findViewById(R.id.fragment_add_dashboard_item_title);
			textView.setText(R.string.dashboard_add_graph_week_module_label);

			fillAdapter(false, null);
			mAdapter.selectFirstModuleItem();
			mButtonDone.setImageResource(R.drawable.arrow_right_bold);
			mButtonDone.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int selectedItem = mAdapter.getFirstSelectedItem();
					ModuleItem moduleItem = (ModuleItem) mAdapter.getItem(selectedItem);
					Fragment fragment = AddDashboardOverviewGraphItemFragment.newInstance(mGateId, moduleItem);
					mActivity.replaceFragment(getTag(), fragment);
				}
			});
		} else {
			mButtonDone = (FloatingActionButton) view.findViewById(R.id.fragment_add_dashboard_item_button_done);
			final EditText editText = (EditText) view.findViewById(R.id.fragment_add_dashboard_item_name_edit);

			Controller controller = Controller.getInstance(mActivity);

			Module module = controller.getDevicesModel().getModule(mGateId, mModuleItem.getAbsoluteId());

			if (module != null) {
				editText.setText(module.getName(mActivity, true));
			} else {
				mActivity.finish();
			}

			mButtonDone.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.fragment_add_dashboard_item_graph_types);

					ModuleLog.DataType dataType = getDataTypeBySelectedItem(radioGroup);

					OverviewGraphItem item = new OverviewGraphItem(editText.getText().toString(), mGateId, mModuleItem.getAbsoluteId(), dataType);

					Intent data = new Intent();
					data.putExtra(DashboardFragment.EXTRA_ADD_ITEM, item);
					mActivity.setResult(10, data);
					mActivity.finish();
				}
			});

		}
	}


	private ModuleLog.DataType getDataTypeBySelectedItem(RadioGroup radioGroup) {

		switch (radioGroup.getCheckedRadioButtonId()) {
			case R.id.fragment_add_dashboard_radio_btn_min:
				return ModuleLog.DataType.MINIMUM;
			case R.id.fragment_add_dashboard_radio_btn_avg:
				return ModuleLog.DataType.AVERAGE;
			case R.id.fragment_add_dashboard_radio_btn_max:
				return ModuleLog.DataType.MAXIMUM;
		}
		return null;
	}
}
