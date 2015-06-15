package com.rehivetech.beeeon.gui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.UsersListAdapter;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.threading.CallbackTask.ICallbackTaskListener;
import com.rehivetech.beeeon.threading.task.EditUserTask;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.threading.task.RemoveUserTask;

import java.util.List;

public class GateUsersActivity extends BaseApplicationActivity {

	private GateUsersActivity mActivity;

	private Gate mGate;

	private List<User> mGateUsers;

	private ListView mListActUsers;
	private ListView mListPenUsers;

	private static final int NAME_ITEM_HEIGHT = 74;
	private Toolbar mToolbar;
	private ActionMode mMode;

	private RadioGroup mGroup;
	private ScrollView mLayoutDialog;
	private User mSelectedItem;
	private int mSelectedItemPos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gate_users);

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if (mToolbar != null) {
			mToolbar.setTitle(R.string.title_activity_gate_users);
			setSupportActionBar(mToolbar);
		}

		// Get actual activity
		mActivity = this;

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Get selected gate
		mGate = Controller.getInstance(this).getGatesModel().getGate(getIntent().getStringExtra(Constants.GUI_SELECTED_GATE_ID));

		// Get all users for gate
		doReloadGateUsersTask(mGate.getId(), true);
	}

	private void initLayouts() {
		// Get elements
		mListActUsers = (ListView) findViewById(R.id.gate_users_list);
		//mListPenUsers = (ListView) findViewById(R.id.adapter_users_pending_list);

		mListActUsers.setAdapter(new UsersListAdapter(mActivity, mGateUsers, null));

		mListActUsers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				mMode = startSupportActionMode(new ActionModeEditSensors());
				mSelectedItem = (User) mListActUsers.getAdapter().getItem(position);
				mSelectedItemPos = position;
				setUserSelected();
				return true;
			}
		});

		// Set listview height, for all 
		float scale = mActivity.getResources().getDisplayMetrics().density;
		mListActUsers.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) (scale * NAME_ITEM_HEIGHT * mGateUsers.size())));

		FloatingActionButton mButton = (FloatingActionButton) findViewById(R.id.fab_add_user);
		mButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Go to add new user 
				Intent intent = new Intent(mActivity, AddGateUserActivity.class);
				intent.putExtra(Constants.GUI_SELECTED_GATE_ID, mGate.getId());
				mActivity.startActivity(intent);
			}
		});
	}


	@Override
	protected void onAppResume() {
		if (mGate != null)
			doReloadGateUsersTask(mGate.getId(), true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:

				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}


	private void setUserSelected() {
		getViewByPosition(mSelectedItemPos, mListActUsers).findViewById(R.id.layoutofsensor).setBackgroundColor(mActivity.getResources().getColor(R.color.light_gray));
	}

	private void setUserUnselected() {
		getViewByPosition(mSelectedItemPos, mListActUsers).findViewById(R.id.layoutofsensor).setBackgroundColor(mActivity.getResources().getColor(R.color.white));
	}


	public View getViewByPosition(int pos, ListView listView) {
		final int firstListItemPosition = listView.getFirstVisiblePosition();
		final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

		if (pos < firstListItemPosition || pos > lastListItemPosition) {
			return listView.getAdapter().getView(pos, null, listView);
		} else {
			final int childIndex = pos - firstListItemPosition;
			return listView.getChildAt(childIndex);
		}
	}

	private void doReloadGateUsersTask(final String gateId, boolean forceReload) {
		ReloadGateDataTask reloadUsersTask = new ReloadGateDataTask(this, forceReload, ReloadGateDataTask.ReloadWhat.USERS);

		reloadUsersTask.setListener(new ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				mGateUsers = Controller.getInstance(GateUsersActivity.this).getUsersModel().getUsersByGate(gateId);

				initLayouts();
			}

		});

		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(reloadUsersTask, gateId);
	}


	private void doRemoveUserTask(User user) {
		RemoveUserTask removeUserTask = new RemoveUserTask(this);
		User.DataPair pair = new User.DataPair(user, mGate.getId());

		removeUserTask.setListener(new ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				// Get all users for gate
				doReloadGateUsersTask(mGate.getId(), true);
				if (success) {
					// Hlaska o uspechu
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(removeUserTask, pair);
	}

	private void doEditUserTask(User user) {
		EditUserTask editUserTask = new EditUserTask(this);
		User.DataPair pair = new User.DataPair(user, mGate.getId());

		editUserTask.setListener(new ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				// Get all users for gate
				doReloadGateUsersTask(mGate.getId(), true);
				if (success) {
					// Hlaska o uspechu
				}
				mSelectedItem = null;
				mSelectedItemPos = 0;
			}
		});

		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(editUserTask, pair);
	}


	class ActionModeEditSensors implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.gateuser_menu, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			if (item.getItemId() == R.id.adausr_menu_del) {
				doRemoveUserTask(mSelectedItem);
			} else if (item.getItemId() == R.id.adausr_menu_edit) {
				changeUserRole();
			}

			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			setUserUnselected();
			mMode = null;
		}
	}

	private void changeUserRole() {
		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(LAYOUT_INFLATER_SERVICE);
		mLayoutDialog = (ScrollView) inflater.inflate(R.layout.beeeon_checkbox, null);

		ViewGroup checkboxContainer = (ViewGroup) mLayoutDialog.findViewById(R.id.checkbox_container);

		mGroup = new RadioGroup(mActivity);
		int margin = ((Number) (getResources().getDisplayMetrics().density * 16)).intValue();

		for (User.Role role : User.Role.values()) {
			RadioButton item = new RadioButton(this);
			item.setText(getString(role.getStringResource()));
			item.setPadding(margin, margin, margin, margin);

			mGroup.addView(item);
			if (role == mSelectedItem.getRole())
				mGroup.check(item.getId());
		}
		checkboxContainer.addView(mGroup);


		builder = new AlertDialog.Builder(mActivity);
		builder.setView(mLayoutDialog)
				.setTitle(getString(R.string.gate_user_title_change_role));

		builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User clicked OK button
				//mSelectedItem.setRole(User.Role.fromString(((RadioButton)mLayoutDialog.findViewById(mGroup.getCheckedRadioButtonId())).getText().toString()));

				for (User.Role role : User.Role.values()) {
					if (getString(role.getStringResource()).equals(((RadioButton) mLayoutDialog.findViewById(mGroup.getCheckedRadioButtonId())).getText().toString()))
						mSelectedItem.setRole(role);
				}
				doEditUserTask(mSelectedItem);
			}
		});
		builder.setNegativeButton(R.string.notification_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog
				mSelectedItem = null;
				mSelectedItemPos = 0;
			}
		});

		alertDialog = builder.create();
		alertDialog.show();
	}

}