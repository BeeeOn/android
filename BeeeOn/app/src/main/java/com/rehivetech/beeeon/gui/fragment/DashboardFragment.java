package com.rehivetech.beeeon.gui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.AddDashboardItemActivity;
import com.rehivetech.beeeon.gui.activity.DashboardDetailActivity;
import com.rehivetech.beeeon.gui.activity.ModuleGraphActivity;
import com.rehivetech.beeeon.gui.adapter.RecyclerViewSelectableAdapter;
import com.rehivetech.beeeon.gui.adapter.dashboard.DashboardAdapter;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.ActualValueItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.BaseItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.OverviewGraphItem;
import com.rehivetech.beeeon.gui.view.FloatingActionButton;
import com.rehivetech.beeeon.util.Utils;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by martin on 15.11.15.
 */
public class DashboardFragment extends BaseApplicationFragment implements RecyclerViewSelectableAdapter.IItemClickListener, DashboardAdapter.ActionModeCallback {

	private static final String TAG = DashboardFragment.class.getSimpleName();

	private static final String KEY_GATE_ID = "gate_id";

	public static final String EXTRA_ADD_ITEM = "add_item";

	private String mGateId;
	private DashboardAdapter mAdapter;
	private ActionMode mActionMode;
	private CoordinatorLayout mRootLayout;

	private ItemTouchHelper mItemTouchHelper;
	private boolean mItemMoved = false;

	public static DashboardFragment newInstance(String gateId) {

		Bundle args = new Bundle();
		args.putString(KEY_GATE_ID, gateId);

		DashboardFragment fragment = new DashboardFragment();
		fragment.setArguments(args);
		return fragment;
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
		mRootLayout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_dashboard, container, false);
		RecyclerView recyclerView = (RecyclerView) mRootLayout.findViewById(R.id.dashboard_recyclerview);

		int spanCount = getResources().getInteger(R.integer.dashboard_span_count);
		recyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));

		mAdapter = new DashboardAdapter(mActivity, this, this);
		recyclerView.setAdapter(mAdapter);

		mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
				ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0) {

			@Override
			public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
				mAdapter.moveItem(viewHolder.getAdapterPosition(), target.getAdapterPosition());
				mItemMoved = true;
				return true;
			}

			@Override
			public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
			}

			@Override
			public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
				super.onSelectedChanged(viewHolder, actionState);

				if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {

					if (mActionMode != null && mItemMoved) {
						mItemMoved = false;
						mAdapter.clearSelection();
						mActionMode.finish();
					}
				}
			}
		});

		mItemTouchHelper.attachToRecyclerView(recyclerView);

		FloatingActionButton fab = (FloatingActionButton) mRootLayout.findViewById(R.id.dashboard_fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = AddDashboardItemActivity.getADdDashBoardActivityIntent(mActivity, mGateId);
				startActivityForResult(intent, 0);
			}
		});
		return mRootLayout;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		List<BaseItem> items  = Controller.getInstance(mActivity).getDashboardItems(mGateId);
		if (items != null) {
			mAdapter.setItems(items);
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == 10) {

			BaseItem item = data.getParcelableExtra(EXTRA_ADD_ITEM);

			mAdapter.addItem(item);
			Controller.getInstance(mActivity).saveDashboardItems(mGateId, mAdapter.getItems());
		}
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onPause() {
		super.onPause();
		Controller.getInstance(mActivity).saveDashboardItems(mGateId, mAdapter.getItems());
	}

	@Override
	public void onRecyclerViewItemClick(int position, int viewType) {
		switch (viewType) {
			case DashboardAdapter.VIEW_TYPE_ACT_VALUE: {
				ActualValueItem item = (ActualValueItem) mAdapter.getItem(position);
				String[] ids = Utils.parseAbsoluteModuleId(item.getAbsoluteModuleId());
				Intent intent = ModuleGraphActivity.getActivityIntent(mActivity, item.getGateId(), ids[0], ids[1]);
				mActivity.startActivity(intent);
				break;
			}
			case DashboardAdapter.VIEW_TYPE_GRAPH_OVERVIEW: {
				OverviewGraphItem item = (OverviewGraphItem) mAdapter.getItem(position);
				Intent intent = DashboardDetailActivity.getActivityIntent(mActivity, item);
				mActivity.startActivity(intent);
			}
		}
	}

	@Override
	public boolean onRecyclerViewItemLongClick(int position, int viewType) {
		if (mActionMode == null) {
			mActionMode = mActivity.startSupportActionMode(new ActionModeDashboard());
		}
		mAdapter.clearSelection();
		mAdapter.toggleSelection(position);
		return true;
	}

	@Override
	public void finishActionMode() {
		if (mActionMode != null) {
			mActionMode.finish();
		}
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
				final List selectedItems = mAdapter.getSelectedItems();

				if (mActionMode != null) {
					mActionMode.finish();
				}

				//save selected items to undo
				final Map<Integer, BaseItem> tempSelectedItems = new TreeMap<>();
				for (Object itemPosition : selectedItems) {
					Integer position = (Integer) itemPosition;
					tempSelectedItems.put(position, mAdapter.getItem(position));
				}

				//remove all selected items
				for (Map.Entry<Integer, BaseItem> entry : tempSelectedItems.entrySet()) {
					mAdapter.deleteItem(entry.getValue());
				}

				Snackbar.make(mRootLayout, getResources().getQuantityString(R.plurals.dashboard_delete_snackbar, selectedItems.size()), Snackbar.LENGTH_LONG)
						.setAction(R.string.dashboard_undo, new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								for (Map.Entry<Integer, BaseItem> entry : tempSelectedItems.entrySet()) {
									mAdapter.addItem(entry.getKey(), entry.getValue());
								}
							}
						})
						.show();


			}
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mAdapter.clearSelection();
			mActionMode = null;
		}
	}
}
