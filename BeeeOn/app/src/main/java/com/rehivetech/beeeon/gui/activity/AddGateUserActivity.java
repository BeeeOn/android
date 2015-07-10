package com.rehivetech.beeeon.gui.activity;


import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.dialog.ConfirmDialog;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.threading.CallbackTask.ICallbackTaskListener;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.AddUserTask;

import java.util.ArrayList;
import java.util.List;

public class AddGateUserActivity extends BaseApplicationActivity {

	public static final String EXTRA_GATE_ID = "gate_id";

	protected static final String TAG = "AddGateUserActivity";

	private Gate mGate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_gate_user);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			toolbar.setTitle(R.string.app_name);
			setSupportActionBar(toolbar);
		}

		ActionBar actionBar = getSupportActionBar();

		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		// Get selected gate
		mGate = Controller.getInstance(this).getGatesModel().getGate(getIntent().getStringExtra(EXTRA_GATE_ID));

		initLayout();
	}

	private void initLayout() {
		final EditText email = (EditText) findViewById(R.id.add_user_email);
		final Spinner role = (Spinner) findViewById(R.id.add_user_role);
		Button button = (Button) findViewById(R.id.add_user_gate_save);

		List<CharSequence> roles = new ArrayList<>();
		for (User.Role tmpRole : User.Role.values()) {
			roles.add(getString(tmpRole.getStringResource()));
		}

		ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, roles);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the gate to the spinner
		role.setAdapter(adapter);

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!(email.getText().length() > 0)) {
					// Please fill email
					Log.d(TAG, "empty email");
					return;
				}
				if (!isEmailValid(email.getText())) {
					// NON valid email 
					Log.d(TAG, "non valid email");
					return;
				}

				User newUser = new User();
				newUser.setEmail(email.getText().toString());
				newUser.setRole(User.Role.values()[role.getSelectedItemPosition()]);

				User.DataPair pair = new User.DataPair(newUser, mGate.getId());

				if (role.getSelectedItemPosition() == 3) {
					final User.DataPair pairInner = pair;
					String title = getString(R.string.confirm_add_owner_title);
					String message = getString(R.string.confirm_add_owner_message, email.getText());
					ConfirmDialog.confirm(AddGateUserActivity.this, title, message, R.string.button_add_user_confirm, new ConfirmDialog.ConfirmDialogListener() {
						@Override
						public void onConfirm() {
							doAddGateUserTask(pairInner);
						}
					});
				} else {
					doAddGateUserTask(pair);
				}


			}
		});
	}

	protected void doAddGateUserTask(User.DataPair pair) {
		AddUserTask addUserTask = new AddUserTask(this);

		addUserTask.setListener(new ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				finish();
			}

		});

		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(addUserTask, pair, CallbackTaskManager.ProgressIndicator.PROGRESS_DIALOG);
	}

	/*
	 * Email validation
	 */
	boolean isEmailValid(CharSequence email) {
		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
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

}
