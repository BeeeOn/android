package com.rehivetech.beeeon.gui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rehivetech.beeeon.R;

public class EmptyFragment extends BaseApplicationFragment {
	private static final String TAG = EmptyFragment.class.getSimpleName();

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

		/*mShowLegendButton = (Button) mView.findViewById(R.id.module_graph_show_legend_btn);
		mShowLegendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SimpleDialogFragment.createBuilder(mActivity, getFragmentManager())
						.setTitle(getString(R.string.chart_helper_chart_y_axis))
						.setMessage(mYlabels.toString())
						.setNeutralButtonText("close")
						.show();
			}
		});*/
		return view;
	}

}
