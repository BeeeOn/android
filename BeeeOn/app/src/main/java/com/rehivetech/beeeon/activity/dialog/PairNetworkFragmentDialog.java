package com.rehivetech.beeeon.activity.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.achievements.Achievement;
import com.rehivetech.beeeon.achievements.GeneralAchievement;
import com.rehivetech.beeeon.socialNetworks.BeeeOnFacebook;
import com.rehivetech.beeeon.socialNetworks.BeeeOnTwitter;
import com.rehivetech.beeeon.socialNetworks.BeeeOnVKontakte;

import java.util.ArrayList;

/**
 * Dialog shows social networks (Facebook and Twitter for now), that are not
 * already paired and opens their own GUI to connect.
 * @author Jan Lamacz
 */
public class PairNetworkFragmentDialog extends DialogFragment {
	private ArrayList<CharSequence> mSocialNetworks = new ArrayList<>();

	private BeeeOnFacebook mFb;
	private BeeeOnTwitter mTw;
	private BeeeOnVKontakte mVk;

	public PairNetworkFragmentDialog() {
		mFb = BeeeOnFacebook.getInstance(getActivity());
		mTw = BeeeOnTwitter.getInstance(getActivity());
		mVk = BeeeOnVKontakte.getInstance(getActivity());

		if(!mFb.isPaired()) mSocialNetworks.add(mFb.getName());
		if(!mTw.isPaired()) mSocialNetworks.add(mTw.getName());
		if(!mVk.isPaired()) mSocialNetworks.add(mVk.getName());
	}

	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.profile_new_account)
			.setItems(mSocialNetworks.toArray(new CharSequence[mSocialNetworks.size()]),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0 && !mFb.isPaired())
								mFb.logIn(getActivity());
							else if (which == 1 && !mFb.isPaired() ||
									which == 0 && !mTw.isPaired())
								mTw.logIn(getActivity());
							else if((which == 2 && !mFb.isPaired() && !mTw.isPaired()) ||
								which == 1 || which == 0 )
								mVk.logIn(getActivity());
						}
					})
			.setNegativeButton(R.string.action_close, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			});
		return builder.create();
	}
}
