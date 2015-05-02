package com.rehivetech.beeeon.activity.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.arrayadapter.NotificationAdapter;
import com.rehivetech.beeeon.gcm.notification.BaseNotification;
import com.rehivetech.beeeon.gcm.notification.GcmNotification;
import com.rehivetech.beeeon.gcm.notification.VisibleNotification;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin on 1. 5. 2015.
 */
public class NotificationFragment extends Fragment {

	private ListView mListView;
	private TextView mTextNoNotif;
	private ProgressBar mProgressBar;
	private NotificationAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Bundle bundle = getArguments();

		View view = inflater.inflate(R.layout.fragment_notification,
				container, false);

		mListView = (ListView) view.findViewById(R.id.notification_list);
		mTextNoNotif = (TextView) view.findViewById(R.id.notification_text);
		mProgressBar = (ProgressBar) view.findViewById(R.id.notification_progress_bar);

//		setProgressBarVisible();

		if (bundle != null) {
			VisibleNotification notification = (VisibleNotification) BaseNotification.parseBundle(getActivity(), bundle);
			notification.onClick(getActivity());

			List<VisibleNotification> list = new ArrayList<>();
			list.add(notification);
			setListVIewVisible(list);
		}

		return view;
	}

	private void setProgressBarVisible() {
		mListView.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.VISIBLE);
		mTextNoNotif.setVisibility(View.GONE);
	}

	private void setTextVisible() {
		mListView.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.GONE);
		mTextNoNotif.setVisibility(View.VISIBLE);
	}

	private void setListVIewVisible(List<VisibleNotification> notifications) {
		mListView.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
		mTextNoNotif.setVisibility(View.GONE);

		if (notifications.size() < 1) {
			// empty list
			setTextVisible();
			return;
		}

		mAdapter = new NotificationAdapter(this, notifications);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				((VisibleNotification)mAdapter.getItem(i)).onClick(getActivity());
				mAdapter.notifyDataSetChanged();
			}
		});
	}

}
