package com.rehivetech.beeeon.gui.activity;


import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.avast.android.dialogs.iface.IPositiveButtonDialogListener;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.threading.CallbackTask.ICallbackTaskListener;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.AddUserTask;
import com.rehivetech.beeeon.util.Utils;

public class AddGateUserActivity extends BaseApplicationActivity implements IPositiveButtonDialogListener {

	public static final String EXTRA_GATE_ID = "gate_id";

	protected static final String TAG = "AddGateUserActivity";

	private Gate mGate;
	private User mNewUser;
	private User.Role mNewRole;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gate_user_add);

		setupToolbar(R.string.app_name);
		if (mActionBar != null) {
			mActionBar.setHomeButtonEnabled(true);
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setHomeAsUpIndicator(R.drawable.ic_action_cancel);
		}

		// Get selected gate
		mGate = Controller.getInstance(this).getGatesModel().getGate(getIntent().getStringExtra(EXTRA_GATE_ID));

		initLayout();
	}

	private void initLayout() {
		final EditText email = (EditText) findViewById(R.id.gate_user_add_user_email);
		final Spinner role = (Spinner) findViewById(R.id.gate_user_add_user_role);
		Button button = (Button) findViewById(R.id.gate_user_add_user_gate_save_button);

		ArrayAdapter adapter = new ArrayAdapter<User.Role>(this, android.R.layout.simple_spinner_item, User.Role.values()){
			private View changeText(View view, int position) {
				User.Role item = getItem(position);
				int nameResourceRole = item.getStringResource();
				String name = getString(nameResourceRole);
				((TextView) view).setText(name);
				return view;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				return changeText(super.getView(position, convertView, parent), position);
			}

			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent) {
				return changeText(super.getDropDownView(position, convertView, parent), position);
			}
		};



		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the gate to the spinner
		role.setAdapter(adapter);

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// check if email is set && valid
				if (!Utils.validateInput(AddGateUserActivity.this, email, Utils.ValidationType.EMAIL)) {
					return;
				}

				// create temporary user with email
				mNewUser = new User();
				mNewUser.setEmail(email.getText().toString());

				// get user's role
				mNewRole = User.Role.values()[role.getSelectedItemPosition()];

				// if superuser -- need to show dialog to confirm
				if (mNewRole == User.Role.Owner) {
					SimpleDialogFragment
							.createBuilder(AddGateUserActivity.this, getSupportFragmentManager())
							.setTitle(R.string.gate_user_add_dialog_title_add_owner)
							.setMessage(R.string.gate_user_add_dialog_message_add_owner, email.getText())
							.setPositiveButtonText(R.string.gate_user_add_btn_add_user)
							.setNegativeButtonText(R.string.activity_fragment_btn_cancel)
							.show();
				} else {
					mNewUser.setRole(mNewRole);
					doAddGateUserTask(mNewUser);
					mNewUser = null;
					mNewRole = null;
				}
			}
		});
	}

	protected void doAddGateUserTask(User user) {
		AddUserTask addUserTask = new AddUserTask(this);

		addUserTask.setListener(new ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				finish();
			}

		});

		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(addUserTask, new User.DataPair(user, mGate.getId()), CallbackTaskManager.ProgressIndicator.PROGRESS_DIALOG);
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

	@Override
	public void onPositiveButtonClicked(int i) {
		mNewUser.setRole(User.Role.Owner);
		doAddGateUserTask(mNewUser);
		mNewUser = null;
		mNewRole = null;
	}
}
