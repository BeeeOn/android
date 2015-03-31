package com.rehivetech.beeeon.adapter.watchdog;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author mlyko
 */
public abstract class WatchDogBaseType {

	public enum WatchDogOperatorType{
		SENSOR, GEOFENCE
	}

	public int mIndex;
	private WatchDogOperatorType mType;
	private ArrayList<String> mParams;

	WatchDogBaseType(WatchDogOperatorType type){
		mIndex = 0;
		mType = type;
	}

	public abstract int[] getAllIcons();

	public abstract String[] getAllCodes();

	public WatchDogOperatorType getType(){
		return mType;
	}

	public void setParams(ArrayList<String> params) {
		mParams = params;
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
		return getAllIcons()[mIndex % getAllIcons().length];
	}

}
