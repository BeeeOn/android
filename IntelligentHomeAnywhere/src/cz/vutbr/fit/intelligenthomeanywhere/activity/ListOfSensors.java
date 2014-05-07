package cz.vutbr.fit.intelligenthomeanywhere.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import cz.vutbr.fit.intelligenthomeanywhere.R;

public class ListOfSensors extends SherlockFragment{

	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,	 Bundle savedInstanceState)
	 {
		 
		 View view = inflater.inflate(R.layout.listofsensors, container, false);
		 return view;
	 }

}