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
import com.rehivetech.beeeon.gui.adapter.dashboard.AddDashboardCardAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by martin on 25.2.16.
 */
public class AddDashboardItemFragment extends BaseApplicationFragment implements AddDashboardCardAdapter.ItemClickListener {

	private static final String TAG = AddDashboardItemFragment.class.getSimpleName();

	private static final String ARG_GATE_ID = "gate_id";

	private String mGateId;

	public static AddDashboardItemFragment newInstance(String gateId) {

		Bundle args = new Bundle();
		args.putString(ARG_GATE_ID, gateId);
		AddDashboardItemFragment fragment = new AddDashboardItemFragment();
		fragment.setArguments(args);
		return fragment;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		mGateId = args.getString(ARG_GATE_ID);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_add_dashboard_item, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.fragment_add_dashboard_item_cards_recyclerview);
		recyclerView.setLayoutManager(new GridLayoutManager(mActivity, 2));
		recyclerView.setHasFixedSize(true);
		AddDashboardCardAdapter adapter = new AddDashboardCardAdapter(this);
		recyclerView.setAdapter(adapter);
		fillAdapter(adapter);
	}


	private void fillAdapter(AddDashboardCardAdapter addDashboardCardAdapter) {
		List<AddDashboardCardAdapter.CardItem> items = new ArrayList<>();

		items.add(new AddDashboardCardAdapter.CardItem(AddDashboardCardAdapter.CardItem.CARD_ACTUAL_VALUE,
				R.drawable.dashboard_act_value_preview, R.string.dashboard_fab_add_module));
		items.add(new AddDashboardCardAdapter.CardItem(AddDashboardCardAdapter.CardItem.CARD_LINE_GRAPH,
				R.drawable.dashboard_line_graph_preview, R.string.dashboard_fab_add_graph));
		items.add(new AddDashboardCardAdapter.CardItem(AddDashboardCardAdapter.CardItem.CARD_BAR_GRAPH,
				R.drawable.dashboard_week_bar_graph_preview, R.string.dashboard_fab_add_week_bar_graph));

		addDashboardCardAdapter.setItems(items);
	}

	@Override
	public void onItemClick(@AddDashboardCardAdapter.CardItem.CardType int type) {

		Fragment fragment = null;
		switch (type) {
			case AddDashboardCardAdapter.CardItem.CARD_ACTUAL_VALUE:
				fragment = AddDashboardActualValueFragment.newInstance(mGateId);
				break;
			case AddDashboardCardAdapter.CardItem.CARD_LINE_GRAPH:
				fragment = AddDashboardGraphItemFragment.newInstance(mGateId, null);
				break;
			case AddDashboardCardAdapter.CardItem.CARD_BAR_GRAPH:
				fragment = AddDashboardOverviewGraphItemFragment.newInstance(mGateId);
				break;
			case AddDashboardCardAdapter.CardItem.CARD_PIE_GRAPH:
				break;
		}

		mActivity.replaceFragment(getTag(), fragment);
	}
}
