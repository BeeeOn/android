package com.rehivetech.beeeon.gui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.gcm.notification.BaseNotification;
import com.rehivetech.beeeon.gcm.notification.VisibleNotification;
import com.rehivetech.beeeon.gui.adapter.NotificationAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin on 1. 5. 2015.
 */
public class NotificationFragment extends Fragment {

	public static final String TAG = NotificationFragment.class.getSimpleName();

	private ListView mListView;
	private TextView mTextNoNotif;
	private ProgressBar mProgressBar;
	private NotificationAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Bundle bundle = getArguments();

		View view = inflater.inflate(R.layout.fragment_notification,
				container, false);

		mListView = (ListView) view.findViewById(R.id.notification_listview);
		mTextNoNotif = (TextView) view.findViewById(R.id.notification_text);
		mProgressBar = (ProgressBar) view.findViewById(R.id.notification_progress_bar);

		setProgressBarVisible();

		createNotifRequest();

		if (bundle != null) {
			VisibleNotification notification = (VisibleNotification) BaseNotification.parseBundle(getActivity(), bundle);
			notification.onClick(getActivity());

			List<VisibleNotification> list = new ArrayList<>();
			list.add(notification);
			setListViewVisible(list);
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

	private void setListViewVisible(List<VisibleNotification> notifications) {
		if (notifications == null || notifications.size() < 1) {
			// empty list
			setTextVisible();
			return;
		}

		mListView.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
		mTextNoNotif.setVisibility(View.GONE);

		mAdapter = new NotificationAdapter(this, notifications);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				((VisibleNotification) mAdapter.getItem(i)).onClick(getActivity());
				mAdapter.notifyDataSetChanged();
			}
		});
	}


	private void createNotifRequest() {

		Thread thread = new Thread() {
			public void run() {
				try {
					final Controller controller = Controller.getInstance(getActivity());
					final List<VisibleNotification> notifications = controller.getGcmModel().getNotificationHistory();
					getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							setListViewVisible(notifications);
						}
					});
				} catch (AppException e) {
					Log.e(TAG, "Cannot get notification history, error: " + e.getLocalizedMessage());
					getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							setTextVisible();
						}
					});
				}
			}
		};
		thread.start();
	}
}
