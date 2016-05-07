package com.rehivetech.beeeon.gui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gcm.analytics.GoogleAnalyticsManager;
import com.rehivetech.beeeon.gui.adapter.dashboard.AddDashboardCardAdapter;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.BaseItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.VentilationItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by martin on 25.2.16.
 */
public class AddDashboardItemFragment extends BaseApplicationFragment implements AddDashboardCardAdapter.ItemClickListener {

	private static final String TAG = AddDashboardItemFragment.class.getSimpleName();

	private static final String ARG_GATE_ID = "gate_id";
	private static final String ARG_INDEX = "index";

	private String mGateId;

	@Bind(R.id.fragment_add_dashboard_item_cards_recyclerview)
	RecyclerView mRecyclerView;

	private AddDashboardCardAdapter mAdapter;
	private int mIndex;

	public static AddDashboardItemFragment newInstance(int index, String gateId) {

		Bundle args = new Bundle();
		args.putInt(ARG_INDEX, index);
		args.putString(ARG_GATE_ID, gateId);
		AddDashboardItemFragment fragment = new AddDashboardItemFragment();
		fragment.setArguments(args);
		return fragment;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		mIndex = args.getInt(ARG_INDEX);
		mGateId = args.getString(ARG_GATE_ID);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_add_dashboard_item, container, false);
		ButterKnife.bind(this, view);

		mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, 2));
		mRecyclerView.setHasFixedSize(true);
		mAdapter = new AddDashboardCardAdapter(this);
		mRecyclerView.setAdapter(mAdapter);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		fillAdapter();
		GoogleAnalyticsManager.getInstance().logScreen(GoogleAnalyticsManager.ADD_DASHBOARD_ITEM_SCREEN);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.unbind(this);
	}

	private void fillAdapter() {
		List<AddDashboardCardAdapter.CardItem> items = new ArrayList<>();

		items.add(new AddDashboardCardAdapter.CardItem(AddDashboardCardAdapter.CardItem.CARD_ACTUAL_VALUE,
				R.drawable.dashboard_act_value_preview, R.string.dashboard_fab_add_module));
		items.add(new AddDashboardCardAdapter.CardItem(AddDashboardCardAdapter.CardItem.CARD_LINE_GRAPH,
				R.drawable.dashboard_line_graph_preview, R.string.dashboard_fab_add_graph));
		items.add(new AddDashboardCardAdapter.CardItem(AddDashboardCardAdapter.CardItem.CARD_BAR_GRAPH,
				R.drawable.dashboard_week_bar_graph_preview, R.string.dashboard_fab_add_week_bar_graph));

		List<BaseItem> dashboardItems = Controller.getInstance(mActivity).getDashboardItems(0, mGateId);
		VentilationItem ventilationItem = null;
		if (dashboardItems != null) {

			for (BaseItem item : dashboardItems) {
				if (item instanceof VentilationItem) {
					ventilationItem = (VentilationItem) item;
					break;
				}
			}
		}
		if (ventilationItem == null) {
			items.add(new AddDashboardCardAdapter.CardItem(AddDashboardCardAdapter.CardItem.CARD_VENTILATION, R.drawable.dashboard_ventilation_preview, R.string.dashboard_add_ventilation_card));
		}
		mAdapter.setItems(items);
	}

	@Override
	public void onItemClick(@AddDashboardCardAdapter.CardItem.CardType int type) {

		String analyticsItemName = null;
		Fragment fragment = null;
		switch (type) {
			case AddDashboardCardAdapter.CardItem.CARD_ACTUAL_VALUE:
				fragment = AddDashboardActualValueFragment.newInstance(mIndex, mGateId, null);
				analyticsItemName = GoogleAnalyticsManager.DASHBOARD_ADD_ACTUAL_VALUE_ITEM;
				break;
			case AddDashboardCardAdapter.CardItem.CARD_LINE_GRAPH:
				fragment = AddDashboardGraphItemFragment.newInstance(mIndex, mGateId, null, null);
				analyticsItemName = GoogleAnalyticsManager.DASHBOARD_ADD_GRAPH_ITEM;
				break;
			case AddDashboardCardAdapter.CardItem.CARD_BAR_GRAPH:
				fragment = AddDashboardOverviewGraphItemFragment.newInstance(mIndex,mGateId, null);
				analyticsItemName = GoogleAnalyticsManager.DASHBOARD_ADD_GRAPH_OVERVIEW_ITEM;
				break;
			case AddDashboardCardAdapter.CardItem.CARD_PIE_GRAPH:
				break;
			case AddDashboardCardAdapter.CardItem.CARD_VENTILATION:
				fragment = AddDashboardVentilationItemFragment.newInstance(mIndex, mGateId, null, null, null, null);
				analyticsItemName = GoogleAnalyticsManager.DASHBOARD_ADD_VENTILATION_ITEM;
		}

		GoogleAnalyticsManager.getInstance().logEvent(GoogleAnalyticsManager.EVENT_CATEGORY_DASHBOARD, GoogleAnalyticsManager.EVENT_ACTION_ADD_ITEM, analyticsItemName);
		mActivity.replaceFragment(getTag(), fragment);
	}
}
