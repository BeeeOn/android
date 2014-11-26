package cz.vutbr.fit.iha.adapter.device.values;

import android.graphics.Color;
import cz.vutbr.fit.iha.R;

public class OnOffActorValue extends BaseEnumValue {

	public static final String SWITCH_ON = "ON";
	public static final String SWITCH_OFF = "OFF";

	public OnOffActorValue() {
		super();

		mItems.add(this.new Item(0, SWITCH_OFF, R.drawable.dev_switch_off, R.string.dev_switch_value_off, Color.RED));
		mItems.add(this.new Item(1, SWITCH_ON, R.drawable.dev_switch_on, R.string.dev_switch_value_on, Color.GREEN));
	}

}
