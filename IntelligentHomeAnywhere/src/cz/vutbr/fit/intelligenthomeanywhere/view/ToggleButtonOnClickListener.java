package cz.vutbr.fit.intelligenthomeanywhere.view;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ToggleButton;
import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;

public class ToggleButtonOnClickListener implements OnClickListener{

	private String _name;
	
	public ToggleButtonOnClickListener(String name) {
		_name = name;
	}
	
	@Override
	public void onClick(View v) {
		ToggleButton clicked = (ToggleButton)v;
		Toast.makeText(v.getContext(), _name + v.getContext().getString(R.string.toast_changeto) + clicked.getText(), Toast.LENGTH_SHORT).show();
		BaseDevice device = Constants.getAdapter().getDeviceByName(_name);
		device.setValue(clicked.getText().toString());
		//TODO: createXml and send to server
	}

}
