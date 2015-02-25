/**
 * 
 */
package com.rehivetech.beeeon.network.xml;

/**
 * @author ThinkDeep
 * 
 */
public class CustomViewPair {

	private int mIcon;
	private String mName;

	/**
	 * 
	 */
	public CustomViewPair() {
		setIcon(0);
		setName("");
	}

	public CustomViewPair(int icon, String name) {
		mIcon = icon;
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
	public void setName(String mName) {
		this.mName = mName;
	}

	/**
	 * @return the mIcon
	 */
	public int getIcon() {
		return mIcon;
	}

	/**
	 * @param mIcon
	 *            the mIcon to set
	 */
	public void setIcon(int mIcon) {
		this.mIcon = mIcon;
	}

}
