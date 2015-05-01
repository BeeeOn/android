package com.rehivetech.beeeon.activity.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gcm.notification.BaseNotification;
import com.rehivetech.beeeon.gcm.notification.GcmNotification;

/**
 * Created by Martin on 1. 5. 2015.
 */
public class NotificationFragment extends Fragment {

	private ListView mListView;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Bundle bundle = getArguments();

		if (bundle != null) {
			GcmNotification notification = BaseNotification.parseBundle(getActivity(), bundle);
			notification.onClick(getActivity());
		}

		View view = inflater.inflate(R.layout.fragment_notification,
				container, false);

		mListView = (ListView) view.findViewById(R.id.notification_list);

		return view;
	}
}
