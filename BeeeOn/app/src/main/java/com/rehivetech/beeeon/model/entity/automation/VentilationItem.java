package com.rehivetech.beeeon.model.entity.automation;

import io.realm.RealmObject;

/**
 * @author martin
 * @since 15/12/2016.
 */

public class VentilationItem extends RealmObject implements IAutomationItem {

	private String mOutsideAbsoluteModuleId;
	private String mInSideAbsoluteModuleId;
	private boolean active;
	private String name;

	public VentilationItem() {
	}

	public String getOutsideAbsoluteModuleId() {
		return mOutsideAbsoluteModuleId;
	}

	public String getInSideAbsoluteModuleId() {
		return mInSideAbsoluteModuleId;
	}

	public void setOutsideAbsoluteModuleId(String outsideAbsoluteModuleId) {
		mOutsideAbsoluteModuleId = outsideAbsoluteModuleId;
	}

	public void setInSideAbsoluteModuleId(String inSideAbsoluteModuleId) {
		mInSideAbsoluteModuleId = inSideAbsoluteModuleId;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public boolean isActive() {
		return active;
	}
}
