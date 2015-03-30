package com.rehivetech.beeeon.achievements;

import android.content.Context;

/**
 * @author Jan Lamacz
 */
public class FbShareAchievement extends Achievement{
	public FbShareAchievement(Context context) {
		super("4", context);
		if(!mData.isDone()) {
			//TODO network
			show();
		}
	}
}
