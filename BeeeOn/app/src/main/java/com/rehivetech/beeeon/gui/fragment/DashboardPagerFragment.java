package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gcm.analytics.GoogleAnalyticsManager;
import com.rehivetech.beeeon.gui.activity.AddDashboardItemActivity;
import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;
import com.rehivetech.beeeon.gui.adapter.ViewPagerAdapter;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.BaseItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.VentilationItem;
import com.rehivetech.beeeon.gui.dialog.ConfirmDialog;
import com.rehivetech.beeeon.gui.view.FloatingActionMenu;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.ReloadDashboardDataTask;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.State;

/**
 * @author martin
 * @since 23.4.16
 */
public class DashboardPagerFragment extends BaseApplicationFragment implements ConfirmDialog.ConfirmDialogListener {

	public static final int REQUEST_CODE_ADD_ITEM = 10;

	private static final String KEY_GATE_ID = "gate_id";

	public static final String EXTRA_ADD_ITEM = "add_item";
	public static final String EXTRA_INDEX = "index";

	private String mGateId;

	@BindView(R.id.dashboard_pager_root_layout)
	CoordinatorLayout mRootLayout;
	@BindView(R.id.dashboard_tab_layout)
	TabLayout mTabLayout;
	@BindView(R.id.dashboard_viewpager)
	ViewPager mViewPager;
	@BindView(R.id.dashboard_fab_menu)
	FloatingActionMenu mFloatingActionMenu;

	ViewPagerAdapter mViewsAdapter;
	@State int mSelectedViewIndex = 0;

	/**
	 * Proper constructor
	 *
	 * @param gateId for this dashboard
	 * @return this fragment
	 */
	public static DashboardPagerFragment newInstance(String gateId) {
		Bundle args = new Bundle();
		args.putString(KEY_GATE_ID, gateId);

		DashboardPagerFragment fragment = new DashboardPagerFragment();
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
		setHasOptionsMenu(true);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_pager_dashboard, container, false);
		mUnbinder = ButterKnife.bind(this, view);
		mFloatingActionMenu.setClosedOnTouchOutside(true);
		mViewsAdapter = new ViewPagerAdapter(getChildFragmentManager());
		mViewPager.setAdapter(mViewsAdapter);
		mTabLayout.setupWithViewPager(mViewPager);

		Controller controller = Controller.getInstance(mActivity);
		List<List<BaseItem>> dashboardViews = controller.getDashboardViews(mGateId);
		// if no dashboard views, set to one at least
		int viewsCount = (dashboardViews == null || dashboardViews.isEmpty()) ? 1 : dashboardViews.size();
		setupViewpager(viewsCount);
		return view;
	}

	/**
	 * Setups toolbar + refresh icon
	 *
	 * @param savedInstanceState fragment's state
	 */
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Toolbar toolbar = mActivity.setupToolbar(R.string.nav_drawer_menu_menu_household, BaseApplicationActivity.INDICATOR_MENU);
		AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
		layoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
		toolbar.setLayoutParams(layoutParams);
		mActivity.setupRefreshIcon(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doReloadDevicesTask(true);
			}
		});
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_dashboard, menu);
	}

	/**
	 * Handles deleting view
	 *
	 * @param item selected
	 * @return if consumed here
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.dashboard_delete_tab:
				int index = mViewPager.getCurrentItem();
				ConfirmDialog.confirm(
						this,
						mActivity.getString(R.string.dashboard_delete_view_x, index + 1),
						mActivity.getString(R.string.dashboard_delete_view_message),
						R.string.activity_fragment_menu_btn_remove, ConfirmDialog.TYPE_DELETE_DASHBOARD_VIEW,
						String.valueOf(index)
				);

				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		GoogleAnalyticsManager.getInstance().logScreen(GoogleAnalyticsManager.DASHBOARD_SCREEN);
	}

	@Override
	public void onStart() {
		super.onStart();
		doReloadDevicesTask(false);
		mViewPager.setCurrentItem(mSelectedViewIndex);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		mSelectedViewIndex = mViewPager.getCurrentItem();
		super.onSaveInstanceState(outState);
	}

	/**
	 * Handles result of adding item -> Called before onResume()
	 *
	 * @param requestCode accepting only {@link #REQUEST_CODE_ADD_ITEM}
	 * @param resultCode  if was ok or canceled etc.
	 * @param data        expecting EXTRA_ADD_ITEM and EXTRA_INDEX
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case REQUEST_CODE_ADD_ITEM:
				if (resultCode == Activity.RESULT_OK) {
					BaseItem item = data.getParcelableExtra(EXTRA_ADD_ITEM);
					int index = data.getIntExtra(EXTRA_INDEX, 0);
					// fragment's UI is update through it's method onStart() which is called after this (onActivityResult)
					Controller.getInstance(mActivity).addDashboardItem(index, mGateId, item);
				}
				break;

		}
	}

	/**
	 * Async task for refreshing data
	 *
	 * @param forceReload forcing reload
	 */
	private void doReloadDevicesTask(boolean forceReload) {
		VentilationItem ventilationItem = getVentilationItem();
		CallbackTask reloadDeviceTask = createReloadDevicesTask(forceReload);
		// if outside temperature from provider, reload weather
		if (ventilationItem != null && ventilationItem.getOutsideAbsoluteModuleId() == null) {
			mActivity.callbackTaskManager.executeTask(reloadDeviceTask, mGateId, ventilationItem.getLatitude(), ventilationItem.getLongitude());
		} else {
			mActivity.callbackTaskManager.executeTask(reloadDeviceTask, mGateId);
		}
	}

	/**
	 * Gets ventilation item from any dashboard view so that it can be inserted only once
	 *
	 * @return ventilation item
	 */
	@Nullable
	private VentilationItem getVentilationItem() {
		List<List<BaseItem>> views = Controller.getInstance(mActivity).getDashboardViews(mGateId);
		if (views == null) return null;

		for (List<BaseItem> items : views) {
			for (BaseItem item : items) {
				if (item instanceof VentilationItem) {
					return (VentilationItem) item;
				}
			}
		}

		return null;
	}

	/**
	 * Task for reloading devices in
	 *
	 * @param forceReload if should reload
	 * @return task
	 */
	private CallbackTask createReloadDevicesTask(final boolean forceReload) {
		ReloadDashboardDataTask reloadDashboardDataTask = new ReloadDashboardDataTask(
				mActivity,
				forceReload,
				ReloadGateDataTask.ReloadWhat.DEVICES
		);

		reloadDashboardDataTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (!success || !forceReload) return;
				updateActiveFragment();
			}
		});
		return reloadDashboardDataTask;
	}


	/**
	 * Prepares viewpager with different dashboard views
	 */
	private void setupViewpager(int viewsCount) {
		for (int index = 0; index < viewsCount; index++) {
			Fragment fragment = DashboardFragment.newInstance(index, mGateId);
			String title = mActivity.getString(R.string.dashboard_view, index + 1);
			mViewsAdapter.addFragment(fragment, title);
		}
	}

	/**
	 * Update active view with fresh data
	 */
	private void updateActiveFragment() {
		DashboardFragment fragment = (DashboardFragment) mViewsAdapter.getActiveFragment(mViewPager);
		if (fragment != null) {
			fragment.updateDashboard();
		}
	}

	/**
	 * Clicking on add card button (from FAmenu)
	 */
	@OnClick(R.id.dashboard_add_item_fab)
	public void onFloatingActionButtonClicked() {
		Intent intent = AddDashboardItemActivity.getAddDashBoardActivityIntent(mActivity, mViewPager.getCurrentItem(), mGateId);
		mFloatingActionMenu.close(true);
		startActivityForResult(intent, REQUEST_CODE_ADD_ITEM);
	}

	/**
	 * Clicking on add view button (from FAmenu)
	 */
	@OnClick(R.id.dashboard_add_view_fab)
	public void onAddViewFloatingActionButtonClicked() {
		DashboardFragment fragment = DashboardFragment.newInstance(mViewsAdapter.getCount(), mGateId);
		String title = mActivity.getString(R.string.dashboard_view, mViewsAdapter.getCount() + 1);
		mViewsAdapter.addFragment(fragment, title);
		mViewPager.setCurrentItem(mViewsAdapter.getCount() - 1, true);
		Controller.getInstance(mActivity).addDashboardView(mGateId);
		mFloatingActionMenu.close(true);
	}

	/**
	 * Confirming "confirm" dialog
	 *
	 * @param confirmType who sent request (submitted)
	 * @param dataId      view index
	 */
	@Override
	public void onConfirm(int confirmType, String dataId) {
		if (confirmType != ConfirmDialog.TYPE_DELETE_DASHBOARD_VIEW) return;

		int index = Utils.parseIntSafely(dataId, 0);
		Controller controller = Controller.getInstance(mActivity);

		controller.removeDashboardView(index, mGateId);
		mViewsAdapter.removeFragment(index);
		fixViewsAfterRemove();

		Snackbar.make(mRootLayout, R.string.activity_fragment_toast_delete_success, Snackbar.LENGTH_SHORT).show();
	}

	/**
	 * After deleting page, fixes titles so that it shows correct numbers from 1
	 */
	public void fixViewsAfterRemove() {
		int viewsCount = mViewsAdapter.getCount();
		if (viewsCount == 0) {
			setupViewpager(1);
			return;
		}

		List<String> fixedTitles = new ArrayList<>();
		for (int index = 0; index < viewsCount; index++) {
			fixedTitles.add(index, mActivity.getString(R.string.dashboard_view, index + 1));
		}
		mViewsAdapter.setFragmentTitles(fixedTitles);
	}
}
