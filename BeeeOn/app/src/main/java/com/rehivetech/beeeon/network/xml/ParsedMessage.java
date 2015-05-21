package com.rehivetech.beeeon.network.xml;

/**
 * @author ThinkDeep
 * 
 */
public class ParsedMessage {

	private XmlParsers.State mState;
	private String mUserId;

	/**
	 * Inner data of message, basically HashMap or List depend on type of message (state)
	 */
	public Object data;

	/**
	 * Constructor
	 * 
	 * @param state
	 *            of communication (e.g. addview)
	 */
	public ParsedMessage(XmlParsers.State state) {
		mState = state;
		mUserId = "";
		data = null;
	}

	/**
	 * Getter
	 * 
	 * @return
	 */
	public XmlParsers.State getState() {
		return mState;
	}

	/**
	 * Setter
	 * 
	 * @param state
	 */
	public void setState(XmlParsers.State state) {
		mState = state;
	}

	/**
	 * Getter
	 * 
	 * @return
	 */
	public String getUserId() {
		return mUserId;
	}

	/**
	 * Setter
	 * 
	 * @param userId
	 */
	public void setUserId(String userId) {
		mUserId = userId;
	}

}
