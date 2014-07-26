package cz.vutbr.fit.iha.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import cz.vutbr.fit.iha.R;

public class ListOfSensors extends SherlockFragment {
	
//	private static final String SSII = "ssii";
	public static boolean ready = false;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		ready = false;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.listofsensors, container, false);
		return view;
	}
	
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		ready = true;
	}
	
	public void onPause(){
		super.onPause();
		ready = false;
	}
	
	public void onResume(){
		super.onResume();
		ready = true;
	}

}