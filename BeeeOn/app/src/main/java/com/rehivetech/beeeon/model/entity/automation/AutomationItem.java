package com.rehivetech.beeeon.model.entity.automation;

import io.realm.RealmObject;

/**
 * @author martin
 * @since 15/12/2016.
 */

public class AutomationItem extends RealmObject {

	private String gateId;

	private VentilationItem mVentilationItem;
	private DewingItem mDewingItem;

	public AutomationItem() {
	}

	public void setGateId(String gateId) {
		this.gateId = gateId;
	}

	public String getGateId() {
		return gateId;
	}

	public VentilationItem getVentilationItem() {
		return mVentilationItem;
	}

	public void setVentilationItem(VentilationItem ventilationItem) {
		mVentilationItem = ventilationItem;
	}

	public DewingItem getDewingItem() {
		return mDewingItem;
	}

	public void setDewingItem(DewingItem dewingItem) {
		mDewingItem = dewingItem;
	}
}
