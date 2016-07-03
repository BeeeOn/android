package com.rehivetech.beeeon.gui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.gcm.analytics.GoogleAnalyticsManager;
import com.rehivetech.beeeon.gcm.notification.BaseNotification;
import com.rehivetech.beeeon.gcm.notification.VisibleNotification;
import com.rehivetech.beeeon.gui.adapter.NotificationAdapter;
import com.rehivetech.beeeon.threading.CallbackTask;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class NotificationActivity extends BaseApplicationActivity {

	@BindView(R.id.notification_listview)
	ListView mListView;
	@BindView(R.id.notification_text)
	TextView mTextNoNotif;
	@BindView(R.id.notification_progress_bar)
	ProgressBar mProgressBar;

	NotificationAdapter mAdapter;
	List<VisibleNotification> mNotifications = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification);
		ButterKnife.bind(this);
		setupToolbar(R.string.notification_title_notification, INDICATOR_BACK);

		mAdapter = new NotificationAdapter(this);
		mListView.setAdapter(mAdapter);

		setProgressBarVisible();

		VisibleNotification notification = (VisibleNotification) BaseNotification.parseBundle(this, getIntent().getExtras());
		if (notification != null) {
			notification.onClick(this);
			mNotifications.add(notification);
		}

		setListViewVisible(mNotifications);
	}

	@Override
	public void onResume() {
		super.onResume();
		GoogleAnalyticsManager.getInstance().logScreen(GoogleAnalyticsManager.NOTIFICATION_LIST_SCREEN);
		doNotificationRequest();
	}

	/**
	 * Creates request for getting notifications
	 * TODO should be reworked as standalone task with some caching
	 */
	private void doNotificationRequest() {
		CallbackTask<String> task = new CallbackTask<String>(this) {
			@Override
			protected Boolean doInBackground(String param) {
				try {
					Controller controller = Controller.getInstance(NotificationActivity.this);
					mNotifications = controller.getGcmModel().getNotificationHistory();
					return true;
				} catch (AppException e) {
					Timber.e("Cannot get notification history, error: %s", e.getTranslatedErrorMessage(NotificationActivity.this));
					return false;
				}
			}
		};

		task.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					setListViewVisible(mNotifications);
				} else {
					setTextVisible();
				}
			}
		});

		callbackTaskManager.executeTask(task);
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
		if (notifications.isEmpty()) {
			// empty list
			setTextVisible();
			return;
		}

		mListView.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
		mTextNoNotif.setVisibility(View.GONE);

		mAdapter.swapItems(notifications);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				((VisibleNotification) mAdapter.getItem(i)).onClick(NotificationActivity.this);
				mAdapter.notifyDataSetChanged();
			}
		});
	}
}
