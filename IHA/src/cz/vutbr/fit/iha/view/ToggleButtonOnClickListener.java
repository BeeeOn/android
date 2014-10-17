/**
 * @brief Package for non-standard controllers and views
 */
package cz.vutbr.fit.iha.view;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ToggleButton;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.SwitchDevice;
import cz.vutbr.fit.iha.adapter.device.values.OnOffValue;

/**
 * @brief Class for listener of ToggleButtons
 * @author ThinkDeep
 * 
 */
public class ToggleButtonOnClickListener implements OnClickListener {

	private SwitchDevice mDevice;

	/**
	 * Constructor
	 * 
	 * @param device
	 */
	public ToggleButtonOnClickListener(SwitchDevice device) {
		mDevice = device;
	}

	@Override
	public void onClick(View v) {
		ToggleButton clicked = (ToggleButton) v;
		Toast.makeText(v.getContext(), mDevice.getName() + v.getContext().getString(R.string.toast_changeto) + clicked.getText(), Toast.LENGTH_SHORT).show();
		((OnOffValue)mDevice.getValue()).setValue(clicked.getText().toString());
		// TODO: createXml and send to server
	}

}
