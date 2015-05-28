/**
 * @brief Package for non-standard controllers and views
 */
package com.rehivetech.beeeon.gui.view;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.Module;

/**
 * @author ThinkDeep
 * @brief Class for listener of ToggleButtons
 */
public class ToggleButtonOnClickListener implements OnClickListener {

	private Module mModule;

	/**
	 * Constructor
	 *
	 * @param module
	 */
	public ToggleButtonOnClickListener(Module module) {
		mModule = module;
	}

	@Override
	public void onClick(View v) {
		ToggleButton clicked = (ToggleButton) v;
		Toast.makeText(v.getContext(), mModule.getName() + v.getContext().getString(R.string.toast_changeto) + clicked.getText(), Toast.LENGTH_SHORT).show();
		// FIXME: this is completely since Devices/Values/... refactoring
		mModule.getValue().setValue(clicked.getText().toString());
		// TODO: createXml and send to server
	}

}
