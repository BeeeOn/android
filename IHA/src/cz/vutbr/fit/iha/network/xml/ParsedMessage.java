/**
 * 
 */
package cz.vutbr.fit.iha.network.xml;

/**
 * @author ThinkDeep
 *
 */
public class ParsedMessage {

	private XmlParsers.State mState;
	private String mSessionId;
	
	/**
	 * Inner data of message, basically HashMap or List depend on type of message (state)
	 */
	public Object data;
	
	/**
	 * Constructor
	 */
	public ParsedMessage() {
		mState = null;
		mSessionId = "";
		data = null;
	}
	
	/**
	 * Constructor
	 * @param state of communication (e.g. addview)
	 * @param sessionId id of actual communication with server
	 * @param data object with inner data of communication
	 */
	public ParsedMessage(XmlParsers.State state, String sessionId, Object data) {
		mState = state;
		mSessionId = sessionId;
		data = null;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public XmlParsers.State getState(){
		return mState;
	}
	
	/**
	 * Setter
	 * @param state
	 */
	public void setState(XmlParsers.State state){
		mState = state;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public String getSessionId(){
		return mSessionId;
	}
	
	/**
	 * Setter
	 * @param sessionId
	 */
	public void setSessionId(String sessionId) {
		mSessionId = sessionId;
	}

}
