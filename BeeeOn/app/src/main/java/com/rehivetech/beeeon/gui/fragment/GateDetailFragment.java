package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;
import com.rehivetech.beeeon.gui.activity.GateDetailActivity;
import com.rehivetech.beeeon.gui.activity.GateUsersActivity;
import com.rehivetech.beeeon.gui.activity.MainActivity;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.util.Log;

import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by david on 23.6.15.
 */
public class GateDetailFragment extends Fragment {
	private static final String TAG = GateDetailActivity.class.getSimpleName();
	private String mGateId;
	private static final String GATE_ID = "GATE_ID";
	private OnGateDetailsButtonsClickedListener mCallback;
	private GateDetailsAdapter mGateDetailsAdapter;
	private ArrayList<Item> mItemList;
	private SwipeRefreshLayout mSwipeRefreshLayout;

	public static GateDetailFragment newInstance(String gateId) {
		GateDetailFragment gateDetailFragment = new GateDetailFragment();
		Bundle args = new Bundle();
		args.putString(GATE_ID, gateId);
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
		mGateId = getArguments().getString(GATE_ID);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_gate_detail, container, false);
		Gate gate = Controller.getInstance(getActivity()).getGatesModel().getGate(mGateId);
		((TextView) view.findViewById(R.id.gate_detail_fragment_title)).setText(gate.getName());
		mItemList = new ArrayList<>();
		mItemList.add(new Item(R.drawable.dev_noise, R.string.fragment_gate_details_gate_id, gate.getId()));
		mItemList.add(new Item(R.drawable.dev_illumination, R.string.gate_detail_your_role, gate.getRole().toString()));
		mItemList.add(new Item(R.drawable.dev_pressure, R.string.time_zone, DateTimeZone.forOffsetMillis(gate.getUtcOffsetMillis()).toTimeZone().getDisplayName()));
		mItemList.add(new Item(R.drawable.dev_state_closed, R.string.gate_detail_num_of_users, getActivity().getString(R.string.loading_data)));
		mItemList.add(new Item(R.drawable.dev_state_open, R.string.gate_detail_num_of_devices, getActivity().getString(R.string.loading_data)));
		mGateDetailsAdapter = new GateDetailsAdapter(getActivity(), mItemList);

		ListView listView = (ListView) view.findViewById(R.id.gate_detail_listview);
		listView.setAdapter(mGateDetailsAdapter);

		mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
		if (mSwipeRefreshLayout == null) {

		}
		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

			@Override
			public void onRefresh() {
				Log.d(TAG, "Refreshing list of sensors");
				Gate gate = Controller.getInstance(getActivity()).getActiveGate();
				if (gate == null) {
					mSwipeRefreshLayout.setRefreshing(false);
					return;
				}
				doReloadGatesAndActiveGateTask(gate.getId(), true);
			}
		});
		mSwipeRefreshLayout.setColorSchemeColors(R.color.beeeon_primary_cyan, R.color.beeeon_text_color, R.color.beeeon_secundary_pink);

		doReloadGatesAndActiveGateTask(mGateId, true);

		return view;
	}


	private void doReloadGatesAndActiveGateTask(final String gateId, boolean forceReload) {
		ReloadGateDataTask reloadGateDataTask = new ReloadGateDataTask(getActivity(), forceReload, EnumSet.of(
				ReloadGateDataTask.ReloadWhat.GATES,
				ReloadGateDataTask.ReloadWhat.DEVICES,
				ReloadGateDataTask.ReloadWhat.USERS
		));

		reloadGateDataTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					mSwipeRefreshLayout.setRefreshing(false);
					Controller controller = Controller.getInstance(getActivity());
					Gate gate = controller.getGatesModel().getGate(mGateId);
					View view = getView();
					if (view != null) {
						if(gate.hasName())
							((TextView) view.findViewById(R.id.gate_detail_fragment_title)).setText(gate.getName());
						else
							((TextView) view.findViewById(R.id.gate_detail_fragment_title)).setText(R.string.gate_no_name);
						mItemList.get(0).Text = gate.getId();
						mItemList.get(1).Text = gate.getRole().toString();
						mItemList.get(2).Text = DateTimeZone.forOffsetMillis(gate.getUtcOffsetMillis()).toTimeZone().getDisplayName();

						List<User> gateUsers = controller.getUsersModel().getUsersByGate(mGateId);
						List<Device> gateDevices = controller.getDevicesModel().getDevicesByGate(mGateId);
						if (gateUsers.isEmpty() || gateDevices.isEmpty()) {
							Toast.makeText(getActivity(), R.string.toast_not_users_found, Toast.LENGTH_SHORT).show();
						} else {
							mItemList.get(3).Text = (String.format("%d", gateUsers.size()));

							View itemOfListView = ((ListView) view.findViewById(R.id.gate_detail_listview)).getChildAt(3);

							if (itemOfListView != null) {
								ImageButton imageButton = (ImageButton) itemOfListView.findViewById(R.id.simple_list_layout_user_list_btn);
								imageButton.setVisibility(View.VISIBLE);
								imageButton.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										mCallback.onDetailsButtonClicked(GateUsersActivity.class);
									}
								});
							}

							mItemList.get(4).Text = (String.format("%d", gateDevices.size()));

							itemOfListView = ((ListView) view.findViewById(R.id.gate_detail_listview)).getChildAt(4);

							if (itemOfListView != null) {
								ImageButton imageButton = (ImageButton) itemOfListView.findViewById(R.id.simple_list_layout_user_list_btn);
								imageButton.setVisibility(View.VISIBLE);
								imageButton.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										mCallback.onDetailsButtonClicked(MainActivity.class);
									}
								});
							}
						}
						GateDetailFragment.this.mGateDetailsAdapter.notifyDataSetChanged();
					}
				}
			}
		});
		// Execute and remember task so it can be stopped automatically
		((BaseApplicationActivity) getActivity()).callbackTaskManager.executeTask(reloadGateDataTask, gateId, CallbackTaskManager.ProgressIndicator.PROGRESS_ICON);
	}

	public class Item {
		public int ImageRes;
		public int TitleRes;
		public String Text;

		public Item(int image, int title, String text) {
			this.ImageRes = image;
			this.TitleRes = title;
			this.Text = text;
		}
	}

	public class GateDetailsAdapter extends ArrayAdapter<Item> {
		public GateDetailsAdapter(Context context, ArrayList<Item> items) {
			super(context, 0, items);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Get the data item for this position
			Item item = getItem(position);
			// Check if an existing view is being reused, otherwise inflate the view
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.simple_list_item_layout, parent, false);
			}
			// Lookup view for data population
			ImageView image = (ImageView) convertView.findViewById(R.id.simple_list_layout_icon);
			TextView text = (TextView) convertView.findViewById(R.id.simple_list_layout_text);
			TextView title = (TextView) convertView.findViewById(R.id.simple_list_layout_title);
			// Populate the data into the template view using the data object
			image.setImageResource(item.ImageRes);
			text.setText(item.Text);
			title.setText(item.TitleRes);
			// Return the completed view to render on screen
			return convertView;
		}
	}

	public void reloadData() {
		doReloadGatesAndActiveGateTask(mGateId, true);
	}

	public interface OnGateDetailsButtonsClickedListener {
		void onDetailsButtonClicked(Class newClass);
	}
}
