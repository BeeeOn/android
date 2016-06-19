package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gcm.notification.VisibleNotification;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Martin
 * @since 1. 5. 2015
 */
public class NotificationAdapter extends BaseAdapter {

	private List<VisibleNotification> mList = new ArrayList<>();
	private Context mContext;
	private LayoutInflater mInflater;

	public NotificationAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
	}

	public void swapItems(List<VisibleNotification> list) {
		mList = list;
		notifyDataSetChanged();
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
			convertView = mInflater.inflate(R.layout.adapter_notification, parent, false);

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

		mList.get(pos).setView(mContext, holder);

		return convertView;
	}

	public static class ViewHolder {
		public TextView name, text, time;
		public ImageView img;
		public View separator;
	}
}
