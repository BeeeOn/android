package com.rehivetech.beeeon.gui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;
import com.rehivetech.beeeon.gui.activity.MainActivity;

public class EmptyFragment extends BaseApplicationFragment {

	private static final String KEY_MESSAGE = "message";

	private String mMessage;

	public static EmptyFragment newInstance(String message) {
		Bundle args = new Bundle();
		args.putString(KEY_MESSAGE, message);

		EmptyFragment fragment = new EmptyFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		mMessage = args.getString(KEY_MESSAGE);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_empty, container, false);

		TextView message = (TextView) view.findViewById(R.id.empty_message);
		message.setText(mMessage);

		return view;
	}


	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mActivity.setupToolbar(R.string.app_name, BaseApplicationActivity.INDICATOR_MENU);
		mActivity.setupRefreshIcon(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((MainActivity) mActivity).doReloadAllData();
			}
		});
	}
}
