package com.rehivetech.beeeon.gui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.avast.android.dialogs.fragment.ListDialogFragment;
import com.avast.android.dialogs.iface.IListDialogListener;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.UsersListAdapter;
import com.rehivetech.beeeon.gui.dialog.ConfirmDialog;
import com.rehivetech.beeeon.gui.view.FloatingActionButton;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.threading.CallbackTask.ICallbackTaskListener;
import com.rehivetech.beeeon.threading.task.EditUserTask;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.threading.task.RemoveUserTask;

import java.util.List;

public class GateUsersActivity extends BaseApplicationActivity implements ConfirmDialog.ConfirmDialogListener, IListDialogListener {

	public static final String EXTRA_GATE_ID = "gate_id";
	private static final int ROLE_RADIO_MARGIN = 16;

	private Gate mGate;
	private List<User> mGateUsers;

	private RadioGroup mGroup;
	private User mSelectedItem;
	private int mSelectedItemPos;

	private UsersListAdapter mUsersListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gate_users);

		setupToolbar(R.string.gate_users_title_gate_users);
		if (mActionBar != null) {
			mActionBar.setHomeButtonEnabled(true);
			mActionBar.setDisplayHomeAsUpEnabled(true);
		}

		// Get selected gate
		mGate = Controller.getInstance(this).getGatesModel().getGate(getIntent().getStringExtra(EXTRA_GATE_ID));

		initLayouts();
		// Get all users for gate
		doReloadGateUsersTask(mGate.getId(), true);
	}

	private void initLayouts() {
		// Get elements
		final ListView listActUsers = (ListView) findViewById(R.id.gate_users_list);
		mUsersListAdapter = new UsersListAdapter(this);
		listActUsers.setAdapter(mUsersListAdapter);

		listActUsers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				startSupportActionMode(new ActionModeEditModules());
				mSelectedItem = (User) listActUsers.getAdapter().getItem(position);
				mSelectedItemPos = position;
				setUserSelected();
				return true;
			}
		});

		FloatingActionButton mButton = (FloatingActionButton) findViewById(R.id.gate_users_add_user_fab);
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
	public void onResume() {
		super.onResume();
		if (mGate != null) {
			doReloadGateUsersTask(mGate.getId(), true);
		}
	}

	private void setUserSelected() {
		getViewByPosition(mSelectedItemPos, ((ListView) findViewById(R.id.gate_users_list))).findViewById(R.id.list_user_item_layout).setBackgroundColor(ContextCompat.getColor(this, R.color.gray_light));
	}

	private void setUserUnselected() {
		getViewByPosition(mSelectedItemPos, ((ListView) findViewById(R.id.gate_users_list))).findViewById(R.id.list_user_item_layout).setBackgroundColor(ContextCompat.getColor(this, R.color.white));
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
				mUsersListAdapter.updateData(mGateUsers);
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
					Toast.makeText(GateUsersActivity.this, R.string.activity_fragment_toast_delete_success, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(GateUsersActivity.this, R.string.activity_fragment_toast_delete_fail, Toast.LENGTH_SHORT).show();
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

	class ActionModeEditModules implements ActionMode.Callback {


		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.activity_gate_user_menu, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			if (item.getItemId() == R.id.gate_user_menu_del) {
				if (mSelectedItem != null) {
					User user = Controller.getInstance(GateUsersActivity.this).getUsersModel().getUser(mGate.getId(), mSelectedItem.getId());
					String userName = user.getName();
					String title = getString(R.string.activity_fragment_menu_dialog_title_remove, userName);
					String message = getString(R.string.gate_users_dialog_message_remove_user);

					ConfirmDialog.confirm(GateUsersActivity.this, title, message, R.string.activity_fragment_menu_btn_remove, ConfirmDialog.TYPE_DELETE_USER, user.getId());
				}
			} else if (item.getItemId() == R.id.gate_user_menu_edit) {
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
		int index = 0, selectedRole = 0;

		String[] userRole = new String[User.Role.values().length];
		for (User.Role role : User.Role.values()) {
			userRole[index] = getString(role.getStringResource());
			if (mSelectedItem.getRole() == role) {
				selectedRole = index;
			}
			index++;
		}

		ListDialogFragment.createBuilder(this, getSupportFragmentManager())
				.setTitle(R.string.gate_users_title_change_role)
				.setItems(userRole)
				.setSelectedItem(selectedRole)
				.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE)
				.setConfirmButtonText(R.string.activity_gate_user_setup_device_btn_save)
				.setCancelButtonText(R.string.activity_fragment_btn_cancel)
				.show();
	}


	@Override
	public void onListItemSelected(CharSequence charSequence, int position, int i1) {
		User.Role newRole = User.Role.values()[position];

		if (mSelectedItem == null || newRole == null) return;

		if (newRole == User.Role.Owner) {
			// Need confirmation for this change
			String title = getString(R.string.gate_users_dialog_title_change_ownership);
			String message = getString(R.string.gate_users_dialog_message_change_ownership);

			ConfirmDialog.confirm(GateUsersActivity.this, title, message, R.string.gate_users_btn_change_ownership, ConfirmDialog.TYPE_CHANGE_OWNERSHIP, mSelectedItem.getId());
		} else {
			mSelectedItem.setRole(newRole);
			doEditUserTask(mSelectedItem);
		}
	}

	@Override
	public void onConfirm(int confirmType, String dataId) {
		User user = Controller.getInstance(this).getUsersModel().getUser(mGate.getId(), dataId);
		if (user == null)
			return;

		if (confirmType == ConfirmDialog.TYPE_CHANGE_OWNERSHIP) {
			user.setRole(User.Role.Owner);
			doEditUserTask(user);
		} else if (confirmType == ConfirmDialog.TYPE_DELETE_USER) {
			doRemoveUserTask(user);
		}
	}
}