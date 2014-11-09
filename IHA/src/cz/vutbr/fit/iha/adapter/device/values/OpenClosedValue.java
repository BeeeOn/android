package cz.vutbr.fit.iha.adapter.device.values;

import android.graphics.Color;
import cz.vutbr.fit.iha.R;

public class OpenClosedValue extends BaseEnumValue {

	public static final String STATE_OPEN = "OPEN";
	public static final String STATE_CLOSED = "CLOSED";

	public OpenClosedValue() {
		super();

		mItems.add(this.new Item(0, STATE_CLOSED, R.drawable.dev_state_closed, R.string.dev_state_value_closed, Color.RED));
		mItems.add(this.new Item(1, STATE_OPEN, R.drawable.dev_state_open, R.string.dev_state_value_open, Color.GREEN));
	}

}
