package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.gate.GateInfo;
import com.rehivetech.beeeon.util.TimezoneWrapper;

import java.util.ArrayList;

/**
 * Created by david on 23.6.15.
 */
public class GateDetailFragment extends BaseApplicationFragment {
	private static final String KEY_GATE_ID = "gate_id";

	private String mGateId;

	private OnGateDetailsButtonsClickedListener mCallback;
	private GateDetailsAdapter mGateDetailsAdapter;
	private ArrayList<DetailsItem> mDetailsItemList;
	private SwipeRefreshLayout mSwipeRefreshLayout;

	private ListView mDetailsListView;
	private TextView mTitleText;

	public static GateDetailFragment newInstance(String gateId) {
		GateDetailFragment gateDetailFragment = new GateDetailFragment();
		Bundle args = new Bundle();
		args.putString(KEY_GATE_ID, gateId);
		gateDetailFragment.setArguments(args);
		return gateDetailFragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (OnGateDetailsButtonsClickedListener) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(String.format("%s must implement onGateDetailsButtonsClickedListener", activity.toString()));
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGateId = getArguments().getString(KEY_GATE_ID);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_gate_detail, container, false);

		mDetailsListView = (ListView) view.findViewById(R.id.gate_detail_listview);
		mTitleText = (TextView) view.findViewById(R.id.gate_detail_title);

		String loadingText = getString(R.string.gate_detail_loading_data);

		mDetailsItemList = new ArrayList<>();
		mDetailsItemList.add(new DetailsItem(R.drawable.ic_info_gray_24dp, R.string.gate_detail_gate_id, loadingText));
		mDetailsItemList.add(new DetailsItem(R.drawable.ic_person_gray_24dp, R.string.gate_detail_your_role, loadingText));
		mDetailsItemList.add(new DetailsItem(R.drawable.ic_language_gray_24dp, R.string.gate_detail_timezone, loadingText));
		mDetailsItemList.add(new DetailsItem(R.drawable.ic_supervisor_account_gray_24dp, R.string.gate_detail_num_of_users, loadingText, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallback.onGateUsersClicked();
			}
		}));
		mDetailsItemList.add(new DetailsItem(R.drawable.ic_router_gray_24dp, R.string.gate_detail_num_of_devices, loadingText, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallback.onGateDevicesClicked();
			}
		}));

		mDetailsItemList.add(new DetailsItem(0, R.string.gate_detail_ip, loadingText));
		mDetailsItemList.add(new DetailsItem(0, R.string.gate_detail_version, loadingText));

		mGateDetailsAdapter = new GateDetailsAdapter(getActivity(), mDetailsItemList);
		mDetailsListView.setAdapter(mGateDetailsAdapter);

		mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.gate_detail_swipe_layout);
		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				mCallback.onForceReloadData();
				mSwipeRefreshLayout.setRefreshing(false);
			}
		});
		mSwipeRefreshLayout.setColorSchemeColors(R.color.beeeon_primary, R.color.beeeon_primary_text, R.color.beeeon_accent);

		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		fillData();
	}

	public void fillData() {
		Controller controller = Controller.getInstance(getActivity());

		GateInfo gateInfo = controller.getGatesModel().getGateInfo(mGateId);
		if (gateInfo == null)
			return;

		if (gateInfo.hasName())
			mTitleText.setText(gateInfo.getName());
		else
			mTitleText.setText(R.string.gate_detail_text_gate_no_name);

		int offsetInMillis = gateInfo.getUtcOffset() * 60 * 1000;

		mDetailsItemList.get(0).text = gateInfo.getId();
		mDetailsItemList.get(1).text = getString(gateInfo.getRole().getStringResource());
		mDetailsItemList.get(2).text = TimezoneWrapper.getZoneByOffset(offsetInMillis).toString();

		int usersCount = gateInfo.getUsersCount();
		DetailsItem usersDetailsItem = mDetailsItemList.get(3);
		usersDetailsItem.buttonEnabled = usersCount > 0;
		usersDetailsItem.text = (String.format("%d", usersCount));

		int devicesCount = gateInfo.getDevicesCount();
		DetailsItem devicesDetailsItem = mDetailsItemList.get(4);
		devicesDetailsItem.buttonEnabled = devicesCount > 0;
		devicesDetailsItem.text = (String.format("%d", devicesCount));

		mDetailsItemList.get(5).text = gateInfo.getIp();
		mDetailsItemList.get(6).text = gateInfo.getVersion();

		mGateDetailsAdapter.notifyDataSetChanged();
	}

	public class DetailsItem {
		public int imageRes;
		public int titleRes;
		public String text;
		public View.OnClickListener buttonClickListener;
		public boolean buttonEnabled;

		public DetailsItem(int image, int title, String text) {
			this(image, title, text, null);
		}

		/**
		 * @param image
		 * @param title
		 * @param text
		 * @param buttonClickListener When no listener is set, then no button is showed.
		 */
		public DetailsItem(int image, int title, String text, View.OnClickListener buttonClickListener) {
			this.imageRes = image;
			this.titleRes = title;
			this.text = text;
			this.buttonClickListener = buttonClickListener;
		}
	}

	public class GateDetailsAdapter extends ArrayAdapter<DetailsItem> {
		public GateDetailsAdapter(Context context, ArrayList<DetailsItem> detailsItems) {
			super(context, 0, detailsItems);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Get the data item for this position
			DetailsItem detailsItem = getItem(position);

			// Check if an existing view is being reused, otherwise inflate the view
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_gate_detail_list_item_layout, parent, false);
			}

			// Lookup view for data population
			ImageView image = (ImageView) convertView.findViewById(R.id.gate_detail_list_item_icon);
			TextView text = (TextView) convertView.findViewById(R.id.gate_detail_list_item_text);
			TextView title = (TextView) convertView.findViewById(R.id.gate_detail_list_title);
			Button button = (Button) convertView.findViewById(R.id.gate_detail_list_item_button_details);

			// Populate the data into the template view using the data object
			if (detailsItem.imageRes > 0)
				image.setImageResource(detailsItem.imageRes);
			text.setText(detailsItem.text);
			title.setText(detailsItem.titleRes);

			button.setVisibility(detailsItem.buttonClickListener != null ? View.VISIBLE : View.INVISIBLE);
			button.setEnabled(detailsItem.buttonEnabled);
			button.setOnClickListener(detailsItem.buttonClickListener);

			// Return the completed view to render on screen
			return convertView;
		}
	}

	public interface OnGateDetailsButtonsClickedListener {
		void onGateUsersClicked();

		void onGateDevicesClicked();

		void onForceReloadData();
	}
}
