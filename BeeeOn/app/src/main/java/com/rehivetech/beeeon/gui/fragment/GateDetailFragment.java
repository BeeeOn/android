package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.rehivetech.beeeon.gui.activity.GateUsersActivity;
import com.rehivetech.beeeon.gui.activity.MainActivity;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;

import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 23.6.15.
 */
public class GateDetailFragment extends Fragment {
	private String mGateId;
	private static final String GATE_ID = "GATE_ID";
	private OnGateDetailsButtonsClickedListener mCallback;
	private GateDetailsAdapter mGateDetailsAdapter;
	private ArrayList<Item> mItemList;

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
		mItemList.add(new Item(R.drawable.dev_noise, R.string.gate_id, gate.getId()));
		mItemList.add(new Item(R.drawable.dev_illumination, R.string.gate_detail_your_role, gate.getRole().toString()));
		mItemList.add(new Item(R.drawable.dev_pressure, R.string.time_zone, DateTimeZone.forOffsetMillis(gate.getUtcOffsetMillis()).toTimeZone().getDisplayName()));
		mItemList.add(new Item(R.drawable.dev_state_closed, R.string.gate_detail_num_of_users, getActivity().getString(R.string.loading_data)));
		mItemList.add(new Item(R.drawable.dev_state_open, R.string.gate_detail_num_of_devices, getActivity().getString(R.string.loading_data)));
		mGateDetailsAdapter = new GateDetailsAdapter(getActivity(), mItemList);

		ListView listView = (ListView) view.findViewById(R.id.gate_detail_listview);
		listView.setAdapter(mGateDetailsAdapter);

		// Load all users and devices
		doReloadGateUsersTask(mGateId, true);
		doReloadGateDevicesTask(mGateId, true);

		return view;
	}

	private void doReloadGateDevicesTask(final String gateId, boolean forceReload) {
		ReloadGateDataTask reloadGateDevicesTask = new ReloadGateDataTask(getActivity(), forceReload, ReloadGateDataTask.ReloadWhat.DEVICES);

		reloadGateDevicesTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					List<Device> gateDevices = Controller.getInstance(getActivity()).getDevicesModel().getDevicesByGate(mGateId);
					mItemList.get(4).Text = (String.format("%d", gateDevices.size()));
					View view = getView();
					if (view != null)
						view = ((ListView) view.findViewById(R.id.gate_detail_listview)).getChildAt(4);
					if (view != null) {
						ImageButton imageButton = (ImageButton) view.findViewById(R.id.simple_list_layout_user_list_btn);
						imageButton.setVisibility(View.VISIBLE);
						imageButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								mCallback.onDetailsButtonClicked(MainActivity.class);
							}
						});
					}
					GateDetailFragment.this.mGateDetailsAdapter.notifyDataSetChanged();
				}
			}
		});
		((BaseApplicationActivity) getActivity()).callbackTaskManager.executeTask(reloadGateDevicesTask, gateId, CallbackTaskManager.ProgressIndicator.PROGRESS_ICON);
	}

	private void doReloadGateUsersTask(final String gateId, boolean forceReload) {
		ReloadGateDataTask reloadUsersTask = new ReloadGateDataTask(getActivity(), forceReload, ReloadGateDataTask.ReloadWhat.USERS);

		reloadUsersTask.setListener(new CallbackTask.ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (success) {
					List<User> gateUsers = Controller.getInstance(getActivity()).getUsersModel().getUsersByGate(mGateId);
					if (gateUsers.isEmpty()) {
						Toast.makeText(getActivity(), R.string.toast_not_users_found, Toast.LENGTH_SHORT).show();
						return;
					}
					mItemList.get(3).Text = (String.format("%d", gateUsers.size()));

					View view = getView();
					if (view != null)
						view = ((ListView) view.findViewById(R.id.gate_detail_listview)).getChildAt(3);
					if (view != null) {
						ImageButton imageButton = (ImageButton) view.findViewById(R.id.simple_list_layout_user_list_btn);
						imageButton.setVisibility(View.VISIBLE);
						imageButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								mCallback.onDetailsButtonClicked(GateUsersActivity.class);
							}
						});
					}
					GateDetailFragment.this.mGateDetailsAdapter.notifyDataSetChanged();
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		((BaseApplicationActivity) getActivity()).callbackTaskManager.executeTask(reloadUsersTask, gateId, CallbackTaskManager.ProgressIndicator.PROGRESS_ICON);
	}

	private void doReloadGatesAndActiveGateTask(final String gateId, boolean forceReload) {
		ReloadGateDataTask reloadGateDataTask = new ReloadGateDataTask(getActivity(), forceReload, ReloadGateDataTask.RELOAD_GATES_AND_ACTIVE_GATE_DEVICES);

		reloadGateDataTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					Gate gate = Controller.getInstance(getActivity()).getGatesModel().getGate(mGateId);
					View view = getView();
					if (view != null) {
						((TextView) view.findViewById(R.id.gate_detail_fragment_title)).setText(gate.getName());
						mItemList.get(0).Text = gate.getName();
						mItemList.get(1).Text = gate.getRole().toString();
						mItemList.get(2).Text = DateTimeZone.forOffsetMillis(gate.getUtcOffsetMillis()).toTimeZone().getDisplayName();
						GateDetailFragment.this.mGateDetailsAdapter.notifyDataSetChanged();
					}
				} else
					Toast.makeText(getActivity(), "Reloading gate not successful", Toast.LENGTH_SHORT).show();
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
			ImageView image = (ImageView) convertView.findViewById(R.id.icon);
			TextView text = (TextView) convertView.findViewById(R.id.gate_detail_text);
			TextView title = (TextView) convertView.findViewById(R.id.gate_detail_title);
			// Populate the data into the template view using the data object
			image.setImageResource(item.ImageRes);
			text.setText(item.Text);
			title.setText(item.TitleRes);
			// Return the completed view to render on screen
			return convertView;
		}
	}

	public void reloadData() {
		doReloadGateDevicesTask(mGateId, true);
		doReloadGateUsersTask(mGateId, true);
		doReloadGatesAndActiveGateTask(mGateId, true);
	}

	public interface OnGateDetailsButtonsClickedListener {
		void onDetailsButtonClicked(Class newClass);
	}
}
