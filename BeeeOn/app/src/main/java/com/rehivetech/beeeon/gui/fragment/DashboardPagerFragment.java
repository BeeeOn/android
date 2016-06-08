package com.rehivetech.beeeon.gui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
import com.rehivetech.beeeon.gui.adapter.dashboard.items.BaseItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.VentilationItem;
import com.rehivetech.beeeon.gui.dialog.ConfirmDialog;
import com.rehivetech.beeeon.gui.view.FloatingActionMenu;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.ReloadDashboardDataTask;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by martin on 23.4.16.
 */
public class DashboardPagerFragment extends BaseApplicationFragment implements ConfirmDialog.ConfirmDialogListener {


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

	DashboardPagerAdapter mAdapter;

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
		setupViewpager();
		return view;
	}

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

	@Override
	public void onResume() {
		super.onResume();
		doReloadDevicesTask(false);
		GoogleAnalyticsManager.getInstance().logScreen(GoogleAnalyticsManager.DASHBOARD_SCREEN);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
			case R.id.dashboard_delete_tab:
				ConfirmDialog.confirm(this, mActivity.getString(R.string.dashboard_delete_view_x, mViewPager.getCurrentItem() + 1), mActivity.getString(R.string.dashboard_delete_view_message),
						R.string.activity_fragment_menu_btn_remove, ConfirmDialog.TYPE_DELETE_DASHBOARD_VIEW, "");

				break;
		}
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == 10) {

			BaseItem item = data.getParcelableExtra(EXTRA_ADD_ITEM);
			int index = data.getIntExtra(EXTRA_INDEX, 0);

			DashboardFragment fragment = (DashboardFragment) mAdapter.getItem(index);

			if (fragment != null) {
				fragment.addItem(item);
			}
		}
	}

	private void setupViewpager() {
		if (mAdapter == null) {
			mAdapter = new DashboardPagerAdapter(getChildFragmentManager());
		}

		Controller controller = Controller.getInstance(mActivity);
		String userId = controller.getActualUser().getId();
		int numOfItems = controller.getNumberOfDashboardTabs(userId, mGateId);

		numOfItems = numOfItems == 0 ? 1 : numOfItems;

		for (int i = 1; i <= numOfItems; i++) {
			mAdapter.addFragment(DashboardFragment.newInstance(i - 1, mGateId), mActivity.getString(R.string.dashboard_view, i));

		}
		mViewPager.setAdapter(mAdapter);
		mTabLayout.setupWithViewPager(mViewPager);
	}

	private void updateViewPager() {
		for (int i = 0; i < mAdapter.getCount(); i++) {
			DashboardFragment fragment = (DashboardFragment) mAdapter.getItem(i);
			fragment.updateDashboard();
		}
	}

	@OnClick(R.id.dashboard_add_item_fab)
	@SuppressWarnings("unused")
	public void onFloatingActionButtonClicked() {
		Intent intent = AddDashboardItemActivity.getADdDashBoardActivityIntent(mActivity, mViewPager.getCurrentItem(), mGateId);
		startActivityForResult(intent, 0);
	}

	@OnClick(R.id.dashboard_add_view_fab)
	@SuppressWarnings("unused")
	public void onAddViewFloatingActionButtonClicked() {
		DashboardFragment fragment = DashboardFragment.newInstance(mAdapter.getCount(), mGateId);
		mAdapter.addFragment(fragment, mActivity.getString(R.string.dashboard_view, mAdapter.getCount() + 1));
		mAdapter.notifyDataSetChanged();

		mFloatingActionMenu.close(true);
		mViewPager.setCurrentItem(mAdapter.getCount() - 1, true);

		Controller controller = Controller.getInstance(mActivity);
		String userId = controller.getActualUser().getId();
		controller.saveNumberOfDashboardTabs(userId, mGateId, mAdapter.getCount());
	}

	public void showSnackbar(String text, View.OnClickListener clickListener) {
		Snackbar.make(mRootLayout, text, Snackbar.LENGTH_LONG).setAction(R.string.dashboard_undo, clickListener).show();
	}

	/**
	 * Async task for refreshing data
	 *
	 * @param forceReload
	 */
	private void doReloadDevicesTask(boolean forceReload) {
		VentilationItem ventilationItem = null;

		Controller controller = Controller.getInstance(mActivity);
		String userId = controller.getActualUser().getId();
		int numOfItems = controller.getNumberOfDashboardTabs(userId, mGateId) + 1;

		for (int i = 0; i < numOfItems; i++) {

			List<BaseItem> items = Controller.getInstance(mActivity).getDashboardItems(i, mGateId);

			if (items != null) {

				for (BaseItem item : items) {
					if (item instanceof VentilationItem) {
						ventilationItem = (VentilationItem) item;
						break;
					}
				}
			}
		}

		if (ventilationItem != null && ventilationItem.getOutsideAbsoluteModuleId() == null) {
			mActivity.callbackTaskManager.executeTask(createReloadDevicesTask(forceReload), mGateId, ventilationItem.getLatitiude(), ventilationItem.getLongitiude());
		} else {
			mActivity.callbackTaskManager.executeTask(createReloadDevicesTask(forceReload), mGateId);
		}
	}


	private CallbackTask createReloadDevicesTask(final boolean forceReload) {
		if (getActivity() == null)
			return null;

		ReloadDashboardDataTask reloadDashboardDataTask = new ReloadDashboardDataTask(
				getActivity(),
				forceReload,
				mGateId == null
						? ReloadGateDataTask.RELOAD_GATES_AND_ACTIVE_GATE_DEVICES
						: EnumSet.of(ReloadGateDataTask.ReloadWhat.DEVICES));

		reloadDashboardDataTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (!success || !forceReload)
					return;

				updateViewPager();
			}
		});
		return reloadDashboardDataTask;
	}

	@Override
	public void onConfirm(int confirmType, String dataId) {

		if (confirmType == ConfirmDialog.TYPE_DELETE_DASHBOARD_VIEW) {
			Controller controller = Controller.getInstance(mActivity);
			String userId = controller.getActualUser().getId();

			controller.removeDashboardView(mViewPager.getCurrentItem(), mGateId);

			mAdapter.removeFragment(mAdapter.getCount() - 1);
			controller.saveNumberOfDashboardTabs(userId, mGateId, mAdapter.getCount());

			mAdapter.removeAll();
			mAdapter.notifyDataSetChanged();
			setupViewpager();
			mAdapter.notifyDataSetChanged();
			Snackbar.make(mRootLayout, R.string.activity_fragment_toast_delete_success, Snackbar.LENGTH_SHORT).show();
		}
	}


	static class DashboardPagerAdapter extends FragmentPagerAdapter {
		private final List<Fragment> mFragments = new ArrayList<>();
		private final List<String> mFragmentTitles = new ArrayList<>();
		private final FragmentManager mFragmentManager;

		public DashboardPagerAdapter(FragmentManager fm) {
			super(fm);
			mFragmentManager = fm;
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mFragmentTitles.get(position);
		}

		public Fragment getActiveFragment(ViewPager container, int position) {
			String name = makeFragmentName(container.getId(), position);
			return mFragmentManager.findFragmentByTag(name);
		}

		private static String makeFragmentName(int viewId, int index) {
			return "android:switcher:" + viewId + ":" + index;
		}

		public void addFragment(Fragment fragment, String title) {
			mFragments.add(fragment);
			mFragmentTitles.add(title);
		}

		public void removeFragment(int index) {
			mFragments.remove(index);
			mFragmentTitles.remove(index);
		}

		public void removeAll() {
			mFragments.clear();
			mFragmentTitles.clear();
			notifyDataSetChanged();
		}
	}


}
