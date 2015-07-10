package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.household.watchdog.Watchdog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UsersListAdapter extends BaseAdapter {

	// Declare Variables
	private Context mContext;

	private List<User> mUsers;
	LayoutInflater mInflater;

	public UsersListAdapter(Context context) {
		mContext = context;
		mUsers = new ArrayList<>();
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mUsers.size();
	}

	@Override
	public Object getItem(int position) {
		return mUsers.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if(convertView == null){
			convertView = mInflater.inflate(R.layout.user_listview_item, parent, false);
			holder = new ViewHolder();

			holder.UserName = (TextView) convertView.findViewById(R.id.adapter_user_name);
			holder.UserEmail = (TextView) convertView.findViewById(R.id.adapter_user_email);
			holder.UserRole = (TextView) convertView.findViewById(R.id.adapter_user_role);
			holder.UserIcon = (ImageView) convertView.findViewById(R.id.adapter_user_icon);

			convertView.setTag(holder);
		}
		else{
			holder = (ViewHolder) convertView.getTag();
		}

		User user = mUsers.get(position);

		holder.UserName.setText(user.getFullName());
		holder.UserEmail.setText(user.getEmail());
		holder.UserRole.setText(user.getRole().getStringResource());

		return convertView;
	}

	public void updateData(List<User> data) {
		this.mUsers = data;
		notifyDataSetChanged();
	}

	private static class ViewHolder {
		public TextView UserName;
		public TextView UserEmail;
		public TextView UserRole;
		public ImageView UserIcon;
	}
}
