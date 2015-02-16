package cz.vutbr.fit.iha.activity;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.view.MenuItem;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.LinearLayout.LayoutParams;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.arrayadapter.UsersListAdapter;
import cz.vutbr.fit.iha.base.BaseApplicationActivity;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.household.User;

public class AdapterUsersActivity extends BaseApplicationActivity {
	
	private Controller mController;
	
	private Activity mActivity;
	
	private Adapter mAdapter;
	
	private List<User> mAdapterUsers;
	
	private ListView mListActUsers;
	private ListView mListPenUsers;
	
	private static final int NAME_ITEM_HEIGHT = 66;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adapter_users);
		
		// Get controller
		mController = Controller.getInstance(this);
		// Get actual activity
		mActivity = this;
		
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher_null);
		
		// Get selected adapter
		mAdapter = mController.getAdapter(getIntent().getStringExtra(Constants.GUI_SELECTED_ADAPTER_ID));
		
		// Get all users for adapter
		//mAdapterUsers = mController.getUsers(mAdapter.getId()); // -> ZATIM NEFUNKCNI 
		mAdapterUsers = new ArrayList<User>();
		mAdapterUsers.add(new User("Test 1","pokus@email.com",User.Role.Superuser,User.Gender.Male));
		mAdapterUsers.add(new User("Test 2","test@email.com",User.Role.Admin,User.Gender.Male));
		initGUI();
	}
	

	private void initGUI() {
		// Get elements
		mListActUsers = (ListView) findViewById(R.id.adapter_users_list);
		//mListPenUsers = (ListView) findViewById(R.id.adapter_users_pending_list);
		
		mListActUsers.setAdapter(new UsersListAdapter(mActivity,mAdapterUsers,null));
		// Set listview height, for all 
		float scale = mActivity.getResources().getDisplayMetrics().density;
		mListActUsers.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) (scale*NAME_ITEM_HEIGHT*mAdapterUsers.size())));
		
		Button mButton = (Button) findViewById(R.id.add_users_adapter);
		mButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Go to add new user 
			}
		});
	}


	@Override
	protected void onAppResume() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onAppPause() {
		// TODO Auto-generated method stub

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
