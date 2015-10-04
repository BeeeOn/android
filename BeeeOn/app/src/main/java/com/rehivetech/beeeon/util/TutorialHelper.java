package com.rehivetech.beeeon.util;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.activity.MainActivity;

public class TutorialHelper {
	private static final String TAG = TutorialHelper.class.getSimpleName();

	/*private static int mLoginTutorialClick = 0;

	public static void showLoginTutorial(final LoginActivity activity) {
		final RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		int marginPx = 0;
		int currentOrientation = activity.getResources().getConfiguration().orientation;
		if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			marginPx = Utils.convertDpToPixel(15);
		} else {
			lps.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			marginPx = Utils.convertDpToPixel(40);
		}

		lps.setMargins(marginPx, marginPx, marginPx, marginPx);
		ViewTarget target_google = new ViewTarget(R.id.login_btn_google, activity);

		OnShowcaseEventListener listener = new OnShowcaseEventListener() {

			@Override
			public void onShowcaseViewShow(ShowcaseView showcaseView) {
				Log.d(TAG, "OnShowCase show");

			}

			@Override
			public void onShowcaseViewHide(ShowcaseView showcaseView) {
				Log.d(TAG, "OnShowCase hide");
				ShowcaseView mSV;

				if (mLoginTutorialClick == 1) {
					ViewTarget target = new ViewTarget(R.id.login_btn_mojeid, activity);
					mSV = new ShowcaseView.Builder(activity, true)
							.setTarget(target)
							.setContentTitle("MojeID account")
							.setContentText("You can login by your MojeID account.")
							.setStyle(R.style.CustomShowcaseTheme_Next)
							.setShowcaseEventListener(this)
							.build();
					mSV.setButtonPosition(lps);
					mLoginTutorialClick++;
					// TODO: Save that Google account was clicked

				} else if (mLoginTutorialClick == 2) {
					ViewTarget target = new ViewTarget(R.id.login_btn_demo, activity);
					mSV = new ShowcaseView.Builder(activity, true)
							.setTarget(target)
							.setContentTitle("Demo mode")
							.setContentText("You can try Demo house.")
							.setStyle(R.style.CustomShowcaseTheme)
							.setShowcaseEventListener(this)
							.build();
					mSV.setButtonPosition(lps);
					mLoginTutorialClick++;
					// TODO: Save that MojeID account was clicked
				} else if (mLoginTutorialClick == 3) {
					// TODO: Save that Demo mode was clicked

				}

			}

			@Override
			public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
				Log.d(TAG, "OnShowCase did hide");
			}
		};

		mLoginTutorialClick = 1;
		ShowcaseView mSV = new ShowcaseView.Builder(activity, true)
				.setTarget(target_google)
				.setContentTitle("Google account")
				.setContentText("You can login by your Google account.")
				.setStyle(R.style.CustomShowcaseTheme_Next)
				.setShowcaseEventListener(listener)
				.build();
		mSV.setButtonPosition(lps);
		mSV.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(TAG, "Showcase click");
			}
		});
	}*/

	public static void showAddDeviceTutorial(final MainActivity activity, View layout) {
		final RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

		int marginPx = Utils.convertDpToPixel(25);
		int marginBottomPx = Utils.convertDpToPixel(55);
		lps.setMargins(marginPx, marginPx, marginPx, marginBottomPx);
		ViewTarget target = new ViewTarget(layout.findViewById(R.id.devices_list_fab));

		OnShowcaseEventListener listener = new OnShowcaseEventListener() {

			@Override
			public void onShowcaseViewShow(ShowcaseView showcaseView) {
				Log.d(TAG, "OnShowCase show ADD MODULE");

			}

			@Override
			public void onShowcaseViewHide(ShowcaseView showcaseView) {
				Log.d(TAG, "OnShowCase hide");
				// TODO: Save that ADD GATE was clicked

			}

			@Override
			public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
				Log.d(TAG, "OnShowCase did hide");

			}
		};

		ShowcaseView mSV = new ShowcaseView.Builder(activity, true)
				.setTarget(target)
				.setContentTitle(activity.getString(R.string.tutorial_helper_tut_module_add_header))
				.setContentText(activity.getString(R.string.tutorial_helper_tut_module_add_text))
				.setStyle(R.style.CustomShowcaseTheme)
				.setShowcaseEventListener(listener)
				.build();
		mSV.setButtonPosition(lps);
		mSV.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(TAG, "Showcase click");
			}
		});

	}

	public static void showAddGateTutorial(final MainActivity activity, View layout) {
		final RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

		int marginPx = Utils.convertDpToPixel(25);
		int marginBottomPx = Utils.convertDpToPixel(55);
		lps.setMargins(marginPx, marginPx, marginPx, marginBottomPx);
		ViewTarget target = new ViewTarget(layout.findViewById(R.id.devices_list_fab));

		OnShowcaseEventListener listener = new OnShowcaseEventListener() {

			@Override
			public void onShowcaseViewShow(ShowcaseView showcaseView) {
				Log.d(TAG, "OnShowCase show ADD GATE");

			}

			@Override
			public void onShowcaseViewHide(ShowcaseView showcaseView) {
				Log.d(TAG, "OnShowCase hide");
				// TODO: Save that ADD GATE was clicked

			}

			@Override
			public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
				Log.d(TAG, "OnShowCase did hide");

			}
		};

		ShowcaseView mSV = new ShowcaseView.Builder(activity, true)
				.setTarget(target)
				.setContentTitle(activity.getString(R.string.tutorial_helper_tut_gate_add_header))
				.setContentText(activity.getString(R.string.tutorial_helper_tut_gate_add_text))
				.setStyle(R.style.CustomShowcaseTheme)
				.setShowcaseEventListener(listener)
				.build();
		mSV.setButtonPosition(lps);
		mSV.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(TAG, "Showcase click");
			}
		});
	}

}
