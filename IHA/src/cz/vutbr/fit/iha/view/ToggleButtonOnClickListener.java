/**
 * @brief Package for non-standard controllers and views
 */
package cz.vutbr.fit.iha.view;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ToggleButton;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.BaseDevice;

/**
 * @brief Class for listener of ToggleButtons
 * @author ThinkDeep
 * 
 */
public class ToggleButtonOnClickListener implements OnClickListener {

	private BaseDevice mDevice;

	/**
	 * Constructor
	 * 
	 * @param device
	 */
	public ToggleButtonOnClickListener(BaseDevice device) {
		mDevice = device;
	}

	@Override
	public void onClick(View v) {
		ToggleButton clicked = (ToggleButton) v;
		Toast.makeText(v.getContext(), mDevice.getName() + v.getContext().getString(R.string.toast_changeto) + clicked.getText(), Toast.LENGTH_SHORT).show();
		//FIXME: this is completely since Devices/Values/... refactoring
		mDevice.getValue().setValue(clicked.getText().toString());
		// TODO: createXml and send to server
	}

}
