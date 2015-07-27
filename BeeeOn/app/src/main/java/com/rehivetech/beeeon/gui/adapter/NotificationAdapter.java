package com.rehivetech.beeeon.gui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gcm.notification.VisibleNotification;
import com.rehivetech.beeeon.gui.fragment.NotificationFragment;

import java.util.List;

/**
 * Created by Martin on 1. 5. 2015.
 */
public class NotificationAdapter extends BaseAdapter {

	private final NotificationFragment mFragment;
	private final List<VisibleNotification> mList;

	public NotificationAdapter(NotificationFragment fragment, List<VisibleNotification> list) {
		mFragment = fragment;
		mList = list;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int i) {
		return mList.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			LayoutInflater inflater = mFragment.getActivity().getLayoutInflater();
			convertView = inflater.inflate(R.layout.adapter_notification, parent, false);

			holder = new ViewHolder();
			holder.img = (ImageView) convertView.findViewById(R.id.notification_item_imageview);
			holder.text = (TextView) convertView.findViewById(R.id.notification_item_text);
			holder.time = (TextView) convertView.findViewById(R.id.notification_item_time_text);
			holder.name = (TextView) convertView.findViewById(R.id.notification_item_name);
			holder.separator = convertView.findViewById(R.id.item_notification_separator);

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (pos == mList.size() - 1) {
			holder.separator.setVisibility(View.INVISIBLE);
		} else {
			holder.separator.setVisibility(View.VISIBLE);
		}

		mList.get(pos).setView(mFragment.getActivity(), holder);

		return convertView;
	}

	public static class ViewHolder {
		public TextView name, text, time;
		public ImageView img;
		public View separator;
	}
}
