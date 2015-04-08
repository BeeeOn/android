package com.rehivetech.beeeon.activity.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.facebook.share.widget.ShareDialog;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gamification.AchievementListItem;
import com.rehivetech.beeeon.socialNetworks.BeeeOnFacebook;
import com.rehivetech.beeeon.socialNetworks.BeeeOnTwitter;

import java.util.ArrayList;

/**
 * @author Jan Lamacz
 */
public class ShareFragmentDialog extends DialogFragment {
	private ShareDialog mShareDialog;

	private String name;
	private String date;

	private BeeeOnFacebook mFb;
	private BeeeOnTwitter mTw;
	private ArrayList<CharSequence> socialNetworks = new ArrayList<>();

	public ShareFragmentDialog(AchievementListItem item, ShareDialog shareDialog) {
		mShareDialog = shareDialog;

		this.name = item.getName();
		this.date = item.getDate();

		mFb = BeeeOnFacebook.getInstance(getActivity());
		mTw = BeeeOnTwitter.getInstance(getActivity());
		socialNetworks.add("Google Plus");
		if(mFb.isPaired()) socialNetworks.add("Facebook");
		if(mTw.isPaired()) socialNetworks.add("Twitter");
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
									Toast.makeText(getActivity(), "Google", Toast.LENGTH_LONG).show();
								if (which == 1 && mFb.isPaired())
									mShareDialog.show(mFb.shareAchievement(getActivity(), name, date));
								if(which == 2 && mTw.isPaired() ||
								   which == 1 && !mFb.isPaired())
									Toast.makeText(getActivity(), "Twitter", Toast.LENGTH_LONG).show();
							}
						})
				.setNegativeButton(R.string.action_close, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {}
				});
		return builder.create();
	}
}
