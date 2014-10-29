package cz.vutbr.fit.iha.activity.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.MainActivity;

public class CustomViewFragment extends SherlockFragment {
	
	private MainActivity mActivity;
	
	public CustomViewFragment(MainActivity context) {
		mActivity = context;
	}
	public CustomViewFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.graphofsensors, container, false);
		return view;
	}

}
