package com.rehivetech.beeeon.model.entity.automation;

/**
 * @author martin
 * @since 15/12/2016.
 */

public interface IAutomationItem {

	void setName(String name);

	String getName();

	void setActive(boolean active);

	boolean isActive();
}
