package com.rehivetech.beeeon.gamification;

import android.view.View;

/**
 * @author Jan Lamacz
 *         from http://milesburton.com/Android_-_Building_a_ListView_with_an_OnClick_Position
 */
public class AchievementListClickListener implements View.OnClickListener {
	private int position;
	private AchievementListOnClickListener callback;

	// Pass in the callback (this'll be the activity) and the row position
	public AchievementListClickListener(AchievementListOnClickListener callback, int pos) {
		position = pos;
		this.callback = callback;
	}

	// The onClick method which has NO position information
	@Override
	public void onClick(View v) {
		callback.OnAchievementClick(v, position);
	}
}
