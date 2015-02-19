package cz.vutbr.fit.iha.activity;

import com.actionbarsherlock.view.MenuItem;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.asynctask.AddAdapterUserTask;
import cz.vutbr.fit.iha.asynctask.GetAdapterUsersTask;
import cz.vutbr.fit.iha.asynctask.CallbackTask.CallbackTaskListener;
import cz.vutbr.fit.iha.base.BaseApplicationActivity;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.household.User;
import cz.vutbr.fit.iha.pair.AddUserPair;

public class AddAdapterUserActivity extends BaseApplicationActivity {
	
	protected static final String TAG = "AddAdapterUserActivity";

	private Controller mController;
	
	private Activity mActivity;
	
	private Adapter mAdapter;
	
	/* GUI elements */
	private Spinner mRole;
	private EditText mEmail;
	private Button mBtn;

	private AddAdapterUserTask mAddAdapterUserTask;
	
	private ProgressDialog mProgress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_adapter_user);
		
		// Get controller
		mController = Controller.getInstance(this);
		// Get actual activity
		mActivity = this;
		
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher_null);
		
		// Prepare progress dialog
		mProgress = new ProgressDialog(this);
		mProgress.setMessage(getString(R.string.progress_saving_data));
		mProgress.setCancelable(false);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		
		// Get selected adapter
		mAdapter = mController.getAdapter(getIntent().getStringExtra(Constants.GUI_SELECTED_ADAPTER_ID));
		
		initLayout();
	}

	private void initLayout() {
		mEmail = (EditText) findViewById(R.id.add_user_email);
		mRole = (Spinner) findViewById(R.id.add_user_role);
		mBtn = (Button) findViewById(R.id.add_user_adapter_save);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.user_role, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		mRole.setAdapter(adapter);
		
		mBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!(mEmail.getText().length()>0)) {
					// Please fill email
					Log.d(TAG, "empty email");
					return;
				}
				if(!isEmailValid(mEmail.getText())){
					// NON valid email 
					Log.d(TAG, "non valid email");
					return;
				}
				mProgress.show();
				User newUser = new User( mEmail.getText().toString(), User.Role.fromString(mRole.getSelectedItem().toString()));
				AddUserPair pair  = new AddUserPair(mAdapter, newUser);
				
				doAddAdapterUserTask(pair);
			}
		});
	}
	
	protected void doAddAdapterUserTask(AddUserPair pair) {
		mAddAdapterUserTask = new AddAdapterUserTask(mActivity, true);

		mAddAdapterUserTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				mProgress.hide();
				finish();
			}

		});

		mAddAdapterUserTask.execute(pair);
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

	@Override
	protected void onAppResume() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onAppPause() {
		// TODO Auto-generated method stub

	}

}