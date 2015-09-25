package com.rehivetech.beeeon.gui.dialog;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.avast.android.dialogs.core.BaseDialogFragment;
import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.adapter.LocationIconAdapter;
import com.rehivetech.beeeon.household.location.Location;

/**
 * Created by xkozak15 on 19.9.15.
 */
public class AddLocationDialog extends SimpleDialogFragment {

	public static final String TAG = "new_loc";

	private AddLocationDialogListener mCallback;

	public static <T extends FragmentActivity & AddLocationDialogListener> void show(T activity) {
		AddLocationDialog addLocationDialog = new AddLocationDialog();
		addLocationDialog.show(activity.getSupportFragmentManager(), TAG);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (AddLocationDialogListener) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(String.format("%s must implement AddLocationDialogListener", activity.toString()));
		}
	}

	public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
		builder.setTitle(R.string.device_edit_overlay_dialog_create_new_location);
		LayoutInflater li = LayoutInflater.from(getActivity());

		View view = li.inflate(R.layout.fragment_dialog_new_location, null);
		final Spinner spinner = (Spinner) view.findViewById(R.id.fragment_dialog_new_location_icon);
		final EditText editText = (EditText) view.findViewById(R.id.fragment_dialog_new_location_name);

		spinner.setAdapter(new LocationIconAdapter(getActivity()));

		builder.setView(view);

		builder.setPositiveButton(R.string.activity_gate_user_setup_device_btn_save, new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String name = editText.getText().toString();
				Location.LocationIcon icon = (Location.LocationIcon) spinner.getAdapter().getItem(spinner.getSelectedItemPosition());
				if (icon == null || name.isEmpty()) {
					Toast.makeText(getActivity(), R.string.device_edit_toast_icon_or_text_is_null, Toast.LENGTH_SHORT).show();
					return;
				}
				mCallback.onCreateLocation(name, icon);
				dismiss();
			}
		});

		builder.setNegativeButton(R.string.activity_fragment_btn_cancel, new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});

		return builder;
	}

	public interface AddLocationDialogListener {
		void onCreateLocation(String name, Location.LocationIcon icon);
	}
}
