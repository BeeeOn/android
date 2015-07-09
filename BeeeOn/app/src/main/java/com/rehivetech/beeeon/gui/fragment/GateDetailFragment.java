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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.GateDetailActivity;
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
	private GateDetailActivity mActivity;
	private GateDetailsAdapter mGateDetailsAdapter;

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
			mActivity = (GateDetailActivity) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(String.format("%s must be created by GateDetailActivity", activity.toString()));
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
		Gate gate = Controller.getInstance(mActivity).getGatesModel().getGate(mGateId);
		ArrayList<Item> itemList = new ArrayList<>();
		itemList.add(new Item(R.drawable.dev_humidity, R.string.gate_name, gate.getName()));
		itemList.add(new Item(R.drawable.dev_noise, R.string.gate_id, gate.getId()));
		itemList.add(new Item(R.drawable.dev_illumination, R.string.gate_detail_your_role, gate.getRole().toString()));
		itemList.add(new Item(R.drawable.dev_pressure, R.string.time_zone, DateTimeZone.forOffsetMillis(gate.getUtcOffsetMillis()).toTimeZone().getDisplayName()));
		itemList.add(new Item(R.drawable.dev_state_closed, R.string.gate_detail_num_of_users, mActivity.getString(R.string.loading_data)));
		itemList.add(new Item(R.drawable.dev_state_open,R.string.gate_detail_num_of_devices,mActivity.getString(R.string.loading_data)));
		mGateDetailsAdapter = new GateDetailsAdapter(mActivity, itemList);

		ListView listView = (ListView) view.findViewById(R.id.gate_detail_listview);
		listView.setAdapter(mGateDetailsAdapter);

		// Load all users
		doReloadGateUsersTask(mGateId, true);

		return view;
	}

	private void doReloadGateUsersTask(final String gateId, boolean forceReload) {
		ReloadGateDataTask reloadUsersTask = new ReloadGateDataTask(mActivity, forceReload, ReloadGateDataTask.ReloadWhat.USERS);

		reloadUsersTask.setListener(new CallbackTask.ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				List<User> gateUsers = Controller.getInstance(mActivity).getUsersModel().getUsersByGate(mGateId);
				if (gateUsers.isEmpty()) {
					Toast.makeText(mActivity, R.string.toast_not_users_found, Toast.LENGTH_SHORT).show();
					return;
				}
				List<String> usersList = new ArrayList<>();
				for (int i = 0; i < gateUsers.size(); i++) {
					usersList.add(gateUsers.get(i).getName());
				}
				GateDetailFragment.this.mGateDetailsAdapter.usersList = usersList;
				((ListView) getView().findViewById(R.id.gate_detail_listview)).getChildAt(4).findViewById(R.id.simple_list_layout_user_list_btn).setVisibility(View.VISIBLE);

				List<Device> gateDevices = Controller.getInstance(mActivity).getDevicesModel().getDevicesByGate(mGateId);

				List<String> devicesList = new ArrayList<>();
				for (int i = 0; i < gateDevices.size(); i++) {
					devicesList.add(gateDevices.get(i).toString());
				}
				GateDetailFragment.this.mGateDetailsAdapter.devicesList = devicesList;
				((ListView) getView().findViewById(R.id.gate_detail_listview)).getChildAt(5).findViewById(R.id.simple_list_layout_user_list_btn).setVisibility(View.VISIBLE);
				GateDetailFragment.this.mGateDetailsAdapter.notifyDataSetChanged();
			}

		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(reloadUsersTask, gateId, CallbackTaskManager.ProgressIndicator.PROGRESS_ICON);
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
		public List<String> usersList = null;
		public List<String> devicesList = null;

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
			if (position == 4)
				if(usersList != null)
					text.setText(String.format("%d", usersList.size()));
				else
					text.setText(item.Text);
			else if(position == 5)
				if(devicesList != null)
					text.setText(String.format("%d", devicesList.size()));
				else
					text.setText(item.Text);
			else
				text.setText(item.Text);
			title.setText(item.TitleRes);
			// Return the completed view to render on screen
			return convertView;
		}
	}
}
