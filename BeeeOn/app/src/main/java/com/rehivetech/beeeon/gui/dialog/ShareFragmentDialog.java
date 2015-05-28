package com.rehivetech.beeeon.gui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.facebook.share.widget.ShareDialog;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.achievements.GeneralAchievement;
import com.rehivetech.beeeon.socialNetworks.BeeeOnFacebook;
import com.rehivetech.beeeon.socialNetworks.BeeeOnGooglePlus;
import com.rehivetech.beeeon.socialNetworks.BeeeOnTwitter;
import com.rehivetech.beeeon.socialNetworks.BeeeOnVKontakte;

import java.util.ArrayList;

/**
 * @author Jan Lamacz
 */
public class ShareFragmentDialog extends DialogFragment {
//	private static final String TAG = ShareFragmentDialog.class.getSimpleName();

	private ShareDialog mShareDialog;

	private String name;
	private String date;

	private BeeeOnFacebook mFb;
	private BeeeOnTwitter mTw;
	private BeeeOnGooglePlus mGp;
	private BeeeOnVKontakte mVk;
	private ArrayList<CharSequence> socialNetworks = new ArrayList<>();

	public ShareFragmentDialog() {
//		mShareDialog = this;

//		this.name = item.getName();
//		this.date = item.getDate();

		mFb = BeeeOnFacebook.getInstance(getActivity());
		mTw = BeeeOnTwitter.getInstance(getActivity());
		mGp = BeeeOnGooglePlus.getInstance(getActivity());
		mVk = BeeeOnVKontakte.getInstance(getActivity());

		socialNetworks.add(mGp.getName());
		if (mFb.isPaired()) socialNetworks.add(mFb.getName());
		if (mTw.isPaired()) socialNetworks.add(mTw.getName());
		if (mVk.isPaired()) socialNetworks.add(mVk.getName());
	}

	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.share_title)
				.setItems(socialNetworks.toArray(new CharSequence[socialNetworks.size()]),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								if (which == 0)
									startActivityForResult(mGp.shareAchievement(name), Constants.SHARE_GOOGLE);
								else if (mFb.isPaired() && which == 1) {
//									this.show(mFb.shareAchievement(name, date));
								} else if (mTw.isPaired() && (which == 1 || which == 2))
									startActivityForResult(mTw.shareAchievement(name), Constants.SHARE_TWITTER);
								else if (mVk.isPaired() && (which == 1 || which == 2 || which == 3))
									mVk.shareAchievement(name, date).show(getFragmentManager(), "string");
							}
						})
				.setNegativeButton(R.string.action_close, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});
		return builder.create();
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// works when user haven't twitter native app - cant control, if sharing was successful
		if (requestCode == Constants.SHARE_TWITTER) {
			new GeneralAchievement(Constants.ACHIEVEMENT_TWITTER_SHARE, getActivity());
		}
	}
}
