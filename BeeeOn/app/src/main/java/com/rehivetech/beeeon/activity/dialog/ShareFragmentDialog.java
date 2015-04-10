package com.rehivetech.beeeon.activity.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.Toast;

import com.facebook.share.widget.ShareDialog;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.achievements.FbShareAchievement;
import com.rehivetech.beeeon.gamification.AchievementListItem;
import com.rehivetech.beeeon.socialNetworks.BeeeOnFacebook;
import com.rehivetech.beeeon.socialNetworks.BeeeOnTwitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jan Lamacz
 */
public class ShareFragmentDialog extends DialogFragment {
	private static final String TAG = ShareFragmentDialog.class.getSimpleName();

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
								if (which == 2 && mTw.isPaired() ||
										which == 1 && !mFb.isPaired()) {
//									Intent in = new TweetComposer.Builder(getActivity())
//											.text(name + " @beeeonapp")
//											.createIntent();
//									startActivityForResult(in, Constants.SHARE_TWITTER);
									startActivityForResult(mTw.shareNetwork(name), Constants.SHARE_TWITTER);
								}
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
		if(requestCode == Constants.SHARE_TWITTER) {
			new FbShareAchievement(getActivity());
		}
	}
}
