package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.avast.android.dialogs.iface.IPositiveButtonDialogListener;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.AddDashboardItemActivity;
import com.rehivetech.beeeon.gui.adapter.DeviceRecycleAdapter;
import com.rehivetech.beeeon.gui.adapter.RecyclerViewSelectableAdapter;
import com.rehivetech.beeeon.gui.adapter.dashboard.DashboardAdapter;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.BaseItem;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Created by martin on 15.11.15.
 */
public class DashboardFragment extends BaseApplicationFragment implements RecyclerViewSelectableAdapter.IItemClickListener{

	private static final String TAG = DashboardFragment.class.getSimpleName();

	private static final String KEY_GATE_ID = "gate_id";

	public static final String EXTRA_ADD_ITEM = "add_item";

	private String mGateId;
	private DashboardAdapter mAdapter;
	private ActionMode mActionMode;

	public static DashboardFragment newInstance(String gateId) {

		Bundle args = new Bundle();
		args.putString(KEY_GATE_ID, gateId);

		DashboardFragment fragment = new DashboardFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			mGateId = args.getString(KEY_GATE_ID);
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.dashboard_recyclerview);

		int spanCount = getResources().getInteger(R.integer.dashboard_span_count);
		recyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));

		mAdapter = new DashboardAdapter(mActivity, this);
		recyclerView.setAdapter(mAdapter);

		FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.dashboard_add_graph);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mActivity, AddDashboardItemActivity.class);
				intent.putExtra(AddDashboardItemActivity.ARG_GATE_ID, mGateId);
				intent.putExtra(AddDashboardItemActivity.ARG_ITEM_TYPE, AddDashboardItemActivity.KEY_VALUE_TYPE_GRAPH_ITEM);
				startActivityForResult(intent, 0);
			}
		});
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Controller controller = Controller.getInstance(mActivity);
		String userId = controller.getActualUser().getId();

		List<BaseItem> items = controller.getDashboardItems(userId);
		if (items != null) {
			mAdapter.setItems(items);
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == 10) {

			BaseItem item = data.getParcelableExtra(EXTRA_ADD_ITEM);

			Controller controller = Controller.getInstance(mActivity);

			mAdapter.addItem(item);
			controller.saveDashboardItems(controller.getActualUser().getId(), mAdapter.getItems());
		}
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Controller controller = Controller.getInstance(mActivity);
		controller.saveDashboardItems(controller.getActualUser().getId(), mAdapter.getItems());
	}

	@Override
	public void onRecyclerViewItemClick(int position, int viewType) {

	}

	@Override
	public boolean onRecyclerViewItemLongClick(int position, int viewType) {
		if (mActionMode == null) {
			mActionMode = mActivity.startSupportActionMode(new ActionModeDashboard());
			mActivity.setStatusBarColor(Color.GRAY);
		}
		mAdapter.toggleSelection(position);
		return true;
	}

	private class ActionModeDashboard implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.actionmode_delete, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			if (item.getItemId() == R.id.action_delete) {
				final int firstSelected = mAdapter.getFirstSelectedItem();

				if (mActionMode != null) {
					mActionMode.finish();
				}
				if (mAdapter.getItemViewType(firstSelected) == DeviceRecycleAdapter.TYPE_DEVICE) {
					final BaseItem dashboardItem = mAdapter.getItem(firstSelected);
					mAdapter.deleteItem(firstSelected);
					Snackbar.make(getView(), R.string.dashboard_delete_snackbar, Snackbar.LENGTH_LONG)
							.setAction(R.string.dashboard_undo, new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									mAdapter.addItem(firstSelected, dashboardItem);
								}
							})
					.show();
				}
			}
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mAdapter.clearSelection();
			mActionMode = null;
			mActivity.setStatusBarColor(ContextCompat.getColor(mActivity, R.color.beeeon_primary_dark));
		}
	}
}
