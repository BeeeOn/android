package com.rehivetech.beeeon.gui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.TrackDialogFragment;

/**
 * About dialog
 *
 * @author Martin Doudera
 */
public class InfoDialogFragment extends TrackDialogFragment {

	// ///////////////////////////////////////////////
	// COMPONENTS
	TextView version;

	// ///////////////////////////////////////////////

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		LayoutInflater factory = LayoutInflater.from(getActivity());
		final View view = factory.inflate(R.layout.fragment_dialog_info, null);

		version = (TextView) view.findViewById(R.id.dialog_info_version_text);
		try {
			version.setText(String.format("%s %s", getString(R.string.dialog_info_version), getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName));
		} catch (NameNotFoundException e) {
			version.setText("0.0");
		}

		return new AlertDialog.Builder(getActivity()).setView(view).setNegativeButton(R.string.dialog_info_btn_rate, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Uri uri = Uri.parse(getActivity().getResources().getString(R.string.dialog_info_play_link));
				Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(myAppLinkToMarket);
			}
		}).setPositiveButton(getString(R.string.fragment_configuration_widget_dialog_btn_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		})
				// .setNeutralButton(getString(R.string.website),
				// new OnClickListener() {
				// @Override
				// public void onClick(DialogInterface dialog,
				// int which) {
				// Intent companyLink = new Intent(
				// Intent.ACTION_VIEW,
				// Uri.parse(getString(R.string.company_web)));
				// startActivity(companyLink);
				// }
				// })
				.create();
	}
}
