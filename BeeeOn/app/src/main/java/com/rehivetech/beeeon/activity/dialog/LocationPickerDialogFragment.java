package com.rehivetech.beeeon.activity.dialog;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;

import com.avast.android.dialogs.core.BaseDialogFragment;
import com.rehivetech.beeeon.R;

/**
 * Created by Tomáš on 25. 4. 2015.
 */
public class LocationPickerDialogFragment extends BaseDialogFragment {
	public static String TAG = "location_picker";

	public static LocationPickerDialogFragment show(FragmentActivity activity){
		LocationPickerDialogFragment locFrag = new LocationPickerDialogFragment();

		locFrag.show(activity.getSupportFragmentManager(), TAG);

		return locFrag;
	}


	/*public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
		builder.setTitle("Jayne's hat");
		builder.setView(LayoutInflater.from(getActivity()).inflate(R.layout.fragment_dialog_location_choose, null));
		builder.setPositiveButton("I want one", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				for (IPositiveButtonDialogListener listener : getPositiveButtonDialogListeners()) {
					listener.onPositiveButtonClicked(mRequestCode);
				}
				dismiss();
			}
		});
		return builder;
	}
	//*/

	@Override
	protected Builder build(Builder builder){
		builder.setView(LayoutInflater.from(getActivity()).inflate(R.layout.fragment_dialog_location_choose, null));

		return builder;
	}
}


