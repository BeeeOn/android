package com.rehivetech.beeeon.gui.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.DashboardDetailActivity;
import com.rehivetech.beeeon.gui.activity.ModuleGraphActivity;
import com.rehivetech.beeeon.gui.adapter.RecyclerViewSelectableAdapter;
import com.rehivetech.beeeon.gui.adapter.dashboard.DashboardAdapter;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.ActualValueItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.BaseItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.GraphItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.OverviewGraphItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.VentilationItem;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.util.Utils;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author martin
 * @since 15.11.15
 */
public class DashboardFragment extends BaseApplicationFragment implements RecyclerViewSelectableAdapter.IItemClickListener, DashboardAdapter.ActionModeCallback {

	private static final String TAG = DashboardFragment.class.getSimpleName();

	private static final String KEY_GATE_ID = "gate_id";
	private static final String KEY_INDEX = "index";

	private String mGateId;
	private int mPageIndex;
	private DashboardAdapter mAdapter;
	private ActionMode mActionMode;

	@BindInt(R.integer.dashboard_span_count)
	public int mGridSpanCount;

	@BindView(R.id.dashboard_recyclerview)
	RecyclerView mRecyclerView;

	@BindView(R.id.dashboard_empty_text)
	TextView mEmptyText;

	private boolean mItemMoved = false;

	CallbackTask<VentilationItem> mReloadWeatherTask;

	/**
	 * Custom constructor with specified parametery
	 *
	 * @param index
	 * @param gateId
	 * @return
	 */
	public static DashboardFragment newInstance(int index, String gateId) {
		Bundle args = new Bundle();
		args.putInt(KEY_INDEX, index);
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
			mPageIndex = args.getInt(KEY_INDEX);
			mGateId = args.getString(KEY_GATE_ID);
		}
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mReloadWeatherTask = new CallbackTask<VentilationItem>(getActivity()) {
			@Override
			protected Boolean doInBackground(VentilationItem param) {
				return Controller.getInstance(mActivity).getWeatherModel().reloadWeather(mActivity, param.getGateId(), param.getLatitude(), param.getLongitude());
			}
		};

		mReloadWeatherTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) updateDashboard();
			}
		});
	}

	/**
	 * Binds rootview + prepares recyclerviev with adapter
	 *
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 * @return
	 */
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
		mUnbinder = ButterKnife.bind(this, view);

		mAdapter = new DashboardAdapter(mActivity, this, this);
		mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(mGridSpanCount, StaggeredGridLayoutManager.VERTICAL));
		mRecyclerView.setAdapter(mAdapter);

		ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
				ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0) {

			@Override
			public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
				mAdapter.moveItem(viewHolder.getAdapterPosition(), target.getAdapterPosition());
				mItemMoved = true;
				Controller.getInstance(mActivity).saveDashboardItems(mPageIndex, mGateId, mAdapter.getItems());
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

		itemTouchHelper.attachToRecyclerView(mRecyclerView);

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		updateDashboard();
	}

	/**
	 * Clicking on dashboard item
	 *
	 * @param position of item
	 * @param viewType dashboard type
	 */
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
				break;
			}
			case DashboardAdapter.VIEW_TYPE_GRAPH: {
				GraphItem item = (GraphItem) mAdapter.getItem(position);
				Intent intent = DashboardDetailActivity.getActivityIntent(mActivity, item);
				mActivity.startActivity(intent);
				break;
			}
		}
	}

	/**
	 * Long click on dashboard item
	 *
	 * @param position of item
	 * @param viewType dashboard type
	 * @return
	 */
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

	/**
	 * Updates adapter with fresh data
	 */
	public void updateDashboard() {

		Controller controller = Controller.getInstance(mActivity);
		List<BaseItem> items = controller.getDashboardItems(mPageIndex, mGateId);
		if (items == null) return;

		for (BaseItem item : items) {
			if (item instanceof VentilationItem && controller.getWeatherModel().getWeather(mGateId) == null) {
				mActivity.callbackTaskManager.executeTask(mReloadWeatherTask, (VentilationItem) item);
				return;
			}
		}

		mAdapter.setItems(items);
		handleEmptyViewVisibility();
	}


	public void addItem(BaseItem item) {
		mAdapter.addItem(item);
		handleEmptyViewVisibility();
		Controller.getInstance(mActivity).saveDashboardItems(mPageIndex, mGateId, mAdapter.getItems());
	}

	/**
	 * Shows/hides empty view
	 * TODO sometimes crashes here
	 */
	private void handleEmptyViewVisibility() {
		mEmptyText.setVisibility(mAdapter.getItems().size() == 0 ? View.VISIBLE : View.GONE);
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

				DashboardPagerFragment fragment = (DashboardPagerFragment) getParentFragment();

				View.OnClickListener listener = new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						for (Map.Entry<Integer, BaseItem> entry : tempSelectedItems.entrySet()) {
							mAdapter.addItem(entry.getKey(), entry.getValue());
						}
					}
				};

				fragment.showSnackbar(getResources().getQuantityString(R.plurals.dashboard_delete_snackbar, selectedItems.size()), listener);

				Controller.getInstance(mActivity).saveDashboardItems(mPageIndex, mGateId, mAdapter.getItems());
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
