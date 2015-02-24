/**
 * 
 */
package com.rehivetech.beeeon.network.xml.action;

import java.util.List;

/**
 * @author ThinkDeep
 * 
 */
public class ComplexAction {

	private String mName;
	private String mId;
	private List<Action> mActions;

	public ComplexAction() {
	}

	/**
	 * 
	 */
	public ComplexAction(String id, String name) {
		mId = id;
		mName = name;
	}

	/**
	 * @return the mName
	 */
	public String getName() {
		return mName;
	}

	/**
	 * @param mName
	 *            the mName to set
	 */
	public void setName(String Name) {
		this.mName = Name;
	}

	/**
	 * @return the mId
	 */
	public String getId() {
		return mId;
	}

	/**
	 * @param mId
	 *            the mId to set
	 */
	public void setId(String Id) {
		this.mId = Id;
	}

	/**
	 * @return the mActions
	 */
	public List<Action> getActions() {
		return mActions;
	}

	/**
	 * @param mActions
	 *            the mActions to set
	 */
	public void setActions(List<Action> Actions) {
		this.mActions = Actions;
	}

}
