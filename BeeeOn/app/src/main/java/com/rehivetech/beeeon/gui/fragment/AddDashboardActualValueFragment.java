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
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.dashboard.DashboardModuleSelectAdapter;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.ActualValueItem;
import com.rehivetech.beeeon.gui.view.FloatingActionButton;
import com.rehivetech.beeeon.household.device.Module;

/**
 * Created by martin on 7.2.16.
 */
public class AddDashboardActualValueFragment extends BaseAddDashBoardItemFragment {

	private static final String ARG_MODULE_ITEM = "module_item";

	private DashboardModuleSelectAdapter.ModuleItem mModuleItem;

	public static AddDashboardActualValueFragment newInstance(String gateId, DashboardModuleSelectAdapter.ModuleItem moduleItem) {
		Bundle args = new Bundle();
		args.putString(ARG_GATE_ID, gateId);
		args.putParcelable(ARG_MODULE_ITEM, moduleItem);
		AddDashboardActualValueFragment fragment = new AddDashboardActualValueFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();

		if (args != null) {
			mModuleItem = args.getParcelable(ARG_MODULE_ITEM);
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		FrameLayout rootView = (FrameLayout) inflater.inflate(R.layout.fragment_add_dashboard_actual_value_item, container, false);
		View childView;
		if (mModuleItem == null) {
			childView = LayoutInflater.from(mActivity).inflate(R.layout.add_dashboard_recyclerview_item_layout1, null);
		} else {
			childView = LayoutInflater.from(mActivity).inflate(R.layout.add_dashboard_actual_value_layout2, null);
		}

		rootView.addView(childView, 0);

		return rootView;

	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

		if (mModuleItem == null) {
			super.onViewCreated(view, savedInstanceState);
			fillAdapter(true, null);
			mAdapter.selectFirstModuleItem();

			TextView textView = (TextView) view.findViewById(R.id.fragment_add_dashboard_item_title);
			textView.setText(R.string.dashboard_add_actual_value_label);

			mButtonDone.setImageResource(R.drawable.arrow_right_bold);
			mButtonDone.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					int selectedItem = mAdapter.getFirstSelectedItem();
					DashboardModuleSelectAdapter.ModuleItem moduleItem = (DashboardModuleSelectAdapter.ModuleItem) mAdapter.getItem(selectedItem);

					Fragment fragment = AddDashboardActualValueFragment.newInstance(mGateId, moduleItem);
					mActivity.replaceFragment(getTag(), fragment);
				}
			});
		} else {
			final EditText editText = (EditText) view.findViewById(R.id.fragment_add_dashboard_item_name_edit);
			mButtonDone = (FloatingActionButton) view.findViewById(R.id.fragment_add_dashboard_item_button_done);

			Controller controller = Controller.getInstance(mActivity);
			Module module = controller.getDevicesModel().getModule(mGateId, mModuleItem.getAbsoluteId());
			if (module != null) {
				String name = module.getName(mActivity, true);
				editText.setText(name);

			} else {
				mActivity.finish();
			}

			mButtonDone.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ActualValueItem item = new ActualValueItem(editText.getText().toString(), mGateId, mModuleItem.getAbsoluteId());
					Intent data = new Intent();
					data.putExtra(DashboardFragment.EXTRA_ADD_ITEM, item);
					mActivity.setResult(10, data);
					mActivity.finish();
				}
			});
		}

	}


}
