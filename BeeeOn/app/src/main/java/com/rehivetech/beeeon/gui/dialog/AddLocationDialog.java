package com.rehivetech.beeeon.gui.dialog;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import java.util.List;

/**
 * @author David Kozak
 * @since 19.9.2015
 */
public class AddLocationDialog extends SimpleDialogFragment {

	public static final String DIALOG_TAG = "new_loc";

	/**
	 * Showing from fragment
	 *
	 * @param activity
	 * @param fragment
	 * @param requestCode
	 */
	public static <T extends Fragment & IAddLocationDialogListener> void show(FragmentActivity activity, @Nullable T fragment, int requestCode) {
		AddLocationDialog dialog = new AddLocationDialog();
		if (fragment != null) dialog.setTargetFragment(fragment, requestCode);
		dialog.show(activity.getSupportFragmentManager(), DIALOG_TAG);
	}

	/**
	 * Showing from activity
	 *
	 * @param activity
	 */
	public static <T extends FragmentActivity & IAddLocationDialogListener> void show(T activity) {
		show(activity, null, 0);
	}

	@SuppressLint("InflateParams")
	@Override
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

				for (IAddLocationDialogListener listener : getAddLocationListeners()) {
					listener.onCreateLocation(name, icon);
				}
				dismiss();
			}
		});

		builder.setNegativeButton(R.string.activity_fragment_btn_cancel, new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				for (IAddLocationDialogListener listener : getAddLocationListeners()) {
					listener.onCancelCreatingLocation();
				}
				dismiss();
			}
		});

		return builder;
	}

	protected List<IAddLocationDialogListener> getAddLocationListeners() {
		return this.getDialogListeners(IAddLocationDialogListener.class);
	}

	public interface IAddLocationDialogListener {
		/**
		 * Listener when location was created (form submitted)
		 * @param name of location
		 * @param icon of location
		 */
		void onCreateLocation(String name, Location.LocationIcon icon);

		/**
		 * Listener when location creating was canceled (form cancelled)
		 */
		void onCancelCreatingLocation();
	}
}
