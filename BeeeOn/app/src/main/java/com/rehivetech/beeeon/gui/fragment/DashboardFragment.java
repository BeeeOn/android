package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.adapter.DashboardAdapter;
import com.rehivetech.beeeon.util.ChartHelper;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by martin on 15.11.15.
 */
public class DashboardFragment extends BaseApplicationFragment {

	private static final String TAG = DashboardFragment.class.getSimpleName();

	private static final String KEY_GATE_ID = "gate_id";

	private DashboardAdapter mAdapter;

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
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.dashboard_recyclerview);

		int spanCount = getResources().getInteger(R.integer.dashboard_span_count);
		recyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));

		mAdapter = new DashboardAdapter(mActivity);
		recyclerView.setAdapter(mAdapter);

		//TODO temp
		DashboardAdapter.GraphItem graphItem = new DashboardAdapter.GraphItem("Graph 1", "64206", "1001", new ArrayList<>(Arrays.asList("0", "2")), ChartHelper.RANGE_DAY);
		DashboardAdapter.ActualValueItem actualValueItem = new DashboardAdapter.ActualValueItem("Indoor temperature", "64206", "1001", "0");
		mAdapter.addItem((graphItem));
		mAdapter.addItem(actualValueItem);

		return view;
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
	}
}
