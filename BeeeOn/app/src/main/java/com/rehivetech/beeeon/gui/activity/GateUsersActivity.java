package com.rehivetech.beeeon.gui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
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
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.UsersListAdapter;
import com.rehivetech.beeeon.gui.dialog.ConfirmDialog;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.threading.CallbackTask.ICallbackTaskListener;
import com.rehivetech.beeeon.threading.task.EditUserTask;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.threading.task.RemoveUserTask;
import com.rehivetech.beeeon.util.Utils;

import java.util.List;

public class GateUsersActivity extends BaseApplicationActivity implements ConfirmDialog.ConfirmDialogListener {

	public static final String EXTRA_GATE_ID = "gate_id";

	private Gate mGate;

	private List<User> mGateUsers;

	private static final int NAME_ITEM_HEIGHT = 74;
	private static final int ROLE_RADIO_MARGIN = 16;

	private RadioGroup mGroup;
	private User mSelectedItem;
	private int mSelectedItemPos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gate_users);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			toolbar.setTitle(R.string.title_activity_gate_users);
			setSupportActionBar(toolbar);
		}

		ActionBar actionBar = getSupportActionBar();

		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		// Get selected gate
		mGate = Controller.getInstance(this).getGatesModel().getGate(getIntent().getStringExtra(EXTRA_GATE_ID));

		// Get all users for gate
		doReloadGateUsersTask(mGate.getId(), true);
	}

	private void initLayouts() {
		// Get elements
		final ListView listActUsers = (ListView) findViewById(R.id.gate_users_list);
		//mListPenUsers = (ListView) findViewById(R.id.adapter_users_pending_list);

		listActUsers.setAdapter(new UsersListAdapter(this, mGateUsers, null));

		listActUsers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				startSupportActionMode(new ActionModeEditSensors());
				mSelectedItem = (User) listActUsers.getAdapter().getItem(position);
				mSelectedItemPos = position;
				setUserSelected();
				return true;
			}
		});

		// Set listview height, for all 
		int heightPx = Utils.convertDpToPixel(NAME_ITEM_HEIGHT * mGateUsers.size());
		listActUsers.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, heightPx));

		FloatingActionButton mButton = (FloatingActionButton) findViewById(R.id.fab_add_user);
		mButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Go to add new user 
				Intent intent = new Intent(GateUsersActivity.this, AddGateUserActivity.class);
				intent.putExtra(AddGateUserActivity.EXTRA_GATE_ID, mGate.getId());
				startActivity(intent);
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
		getViewByPosition(mSelectedItemPos, ((ListView) findViewById(R.id.gate_users_list))).findViewById(R.id.layoutofsensor).setBackgroundColor(getResources().getColor(R.color.gray_light));
	}

	private void setUserUnselected() {
		getViewByPosition(mSelectedItemPos, ((ListView) findViewById(R.id.gate_users_list))).findViewById(R.id.layoutofsensor).setBackgroundColor(getResources().getColor(R.color.white));
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
					Toast.makeText(GateUsersActivity.this, R.string.toast_delete_success, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(GateUsersActivity.this, R.string.toast_delete_fail, Toast.LENGTH_SHORT).show();
				}
				mSelectedItem = null;
				mSelectedItemPos = 0;
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
				if (mSelectedItem != null) {
					User user = Controller.getInstance(GateUsersActivity.this).getUsersModel().getUser(mGate.getId(), mSelectedItem.getId());
					String userName = user.getName();
					String title = getString(R.string.confirm_remove_user_title, userName);
					String message = getString(R.string.confirm_remove_user_message);

					ConfirmDialog.confirm(GateUsersActivity.this, title, message, R.string.button_remove, ConfirmDialog.TYPE_DELETE_USER, user.getId());
				}
			} else if (item.getItemId() == R.id.adausr_menu_edit) {
				changeUserRole();
			}
			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			setUserUnselected();
		}
	}

	private void changeUserRole() {
		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final ScrollView layoutDialog = (ScrollView) inflater.inflate(R.layout.beeeon_checkbox, null);

		ViewGroup checkboxContainer = (ViewGroup) layoutDialog.findViewById(R.id.checkbox_container);

		mGroup = new RadioGroup(this);
		int marginPx = Utils.convertDpToPixel(ROLE_RADIO_MARGIN);

		for (User.Role role : User.Role.values()) {
			RadioButton item = new RadioButton(this);
			item.setText(getString(role.getStringResource()));
			item.setPadding(marginPx, marginPx, marginPx, marginPx);

			mGroup.addView(item);
			if (role == mSelectedItem.getRole())
				mGroup.check(item.getId());
		}
		checkboxContainer.addView(mGroup);


		builder = new AlertDialog.Builder(this);
		builder.setView(layoutDialog)
				.setTitle(getString(R.string.gate_user_title_change_role));

		builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User clicked OK button
				//mSelectedItem.setRole(User.Role.fromString(((RadioButton)layoutDialog.findViewById(mGroup.getCheckedRadioButtonId())).getText().toString()));

				User.Role newRole = null;

				for (User.Role role : User.Role.values()) {
					if (getString(role.getStringResource()).equals(((RadioButton) layoutDialog.findViewById(mGroup.getCheckedRadioButtonId())).getText().toString()))
						newRole = role;
				}

				if (mSelectedItem == null || newRole == null)
					return;

				if (newRole == User.Role.Superuser) {
					// Need confirmation for this change
					String title = getString(R.string.confirm_change_ownership_title);
					String message = getString(R.string.confirm_change_ownership_message);

					ConfirmDialog.confirm(GateUsersActivity.this, title, message, R.string.button_change_ownership, ConfirmDialog.TYPE_CHANGE_OWNERSHIP, mSelectedItem.getId());
				} else {
					mSelectedItem.setRole(newRole);
					doEditUserTask(mSelectedItem);
				}
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

	@Override
	public void onConfirm(int confirmType, String dataId) {
		User user = Controller.getInstance(this).getUsersModel().getUser(mGate.getId(), dataId);
		if (user == null)
			return;

		if (confirmType == ConfirmDialog.TYPE_CHANGE_OWNERSHIP) {
			user.setRole(User.Role.Superuser);
			doEditUserTask(user);
		} else if (confirmType == ConfirmDialog.TYPE_DELETE_USER) {
			doRemoveUserTask(user);
		}
	}
}