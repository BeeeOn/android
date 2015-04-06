package com.rehivetech.beeeon.household.watchdog;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.activity.spinnerItem.SpinnerItem;
import com.rehivetech.beeeon.util.UnitsHelper;

import java.util.Arrays;

/**
 * @author mlyko
 */
public abstract class WatchDogBaseType {
	protected UnitsHelper mUnitsHelper;

	public enum WatchDogOperatorType{
		SENSOR,
		GEOFENCE
	}

	public int mIndex;
	private WatchDogOperatorType mType;

	public WatchDogBaseType(WatchDogOperatorType type, int index){
		mIndex = index;
		mType = type;
	}

	public void setUnitsHelper(UnitsHelper uHelper){
		mUnitsHelper = uHelper;
	}
	public UnitsHelper getUnitsHelper(){
		return mUnitsHelper;
	}

	public abstract int[] getAllIcons();
	public abstract String[] getAllCodes();

	public WatchDogOperatorType getType(){
		return mType;
	}

	public int getIndex(){
		return mIndex;
	}
	public void setIndex(int op){
		mIndex = op;
	}

	public WatchDogBaseType next(){
		mIndex++;
		return this;
	}

	public int getIndexByType(String op){
		int index = Arrays.asList(getAllCodes()).indexOf(op);
		if(index == -1)
			return 0;
		else
			return index;
	}

	public void setByType(String operatorByType) {
		mIndex = getIndexByType(operatorByType);
	}

	// TODO nevim jestli modulo je nej reseni
	public String getCode(){
		return getAllCodes()[mIndex % getAllCodes().length];
	}

	public int getIconResource(){
		int index = mIndex % getAllIcons().length;
		return getAllIcons()[index];
	}

	public void setupGUI(SpinnerItem selected, FloatingActionButton operatorButton, EditText ruleTreshold, TextView ruleTresholdUnit){
		// sets default icon for this type
		operatorButton.setImageResource(getIconResource());

		operatorButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ImageButton img = (ImageButton) v;
				img.setImageResource(next().getIconResource());
			}
		});
	}
}
