package com.rehivetech.beeeon.model.entity.automation;

import io.realm.RealmObject;

/**
 * @author martin
 * @since 15/12/2016.
 */

public class DewingItem extends RealmObject implements IAutomationItem {

	private String insideTempAbsoluteModuleId;
	private String outsideTempAbsoluteModueId;
	private String humidityAbsoluteModuleId;
	private String name;
	private boolean active;

	public DewingItem() {
	}

	public String getInsideTempAbsoluteModuleId() {
		return insideTempAbsoluteModuleId;
	}

	public void setInsideTempAbsoluteModuleId(String insideTempAbsoluteModuleId) {
		this.insideTempAbsoluteModuleId = insideTempAbsoluteModuleId;
	}

	public String getOutsideTempAbsoluteModueId() {
		return outsideTempAbsoluteModueId;
	}

	public void setOutsideTempAbsoluteModueId(String outsideTempAbsoluteModueId) {
		this.outsideTempAbsoluteModueId = outsideTempAbsoluteModueId;
	}

	public String getHumidityAbsoluteModuleId() {
		return humidityAbsoluteModuleId;
	}

	public void setHumidityAbsoluteModuleId(String humidityAbsoluteModuleId) {
		this.humidityAbsoluteModuleId = humidityAbsoluteModuleId;
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
