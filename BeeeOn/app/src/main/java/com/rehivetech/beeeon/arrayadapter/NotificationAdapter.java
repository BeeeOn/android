package com.rehivetech.beeeon.arrayadapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.rehivetech.beeeon.activity.fragment.NotificationFragment;
import com.rehivetech.beeeon.gcm.notification.GcmNotification;

import java.util.List;

/**
 * Created by Martin on 1. 5. 2015.
 */
public class NotificationAdapter extends BaseAdapter {

	private final NotificationFragment mFragment;
	private final List<GcmNotification> mList;

	public NotificationAdapter(NotificationFragment fragment, List<GcmNotification> list) {
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
	public View getView(int i, View view, ViewGroup viewGroup) {
		// TODO
		return null;
	}
}
