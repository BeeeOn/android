/**
 * 
 */
package cz.vutbr.fit.iha.adapter.parser;

/**
 * @author ThinkDeep
 *
 */
public class ParsedMessage {

	private XmlParsers.State mState;
	private int mSessionId;
	
	/**
	 * Inner data of message, basically HashMap or List depend on type of message (state)
	 */
	public Object data;
	
	/**
	 * Constructor
	 */
	public ParsedMessage() {
		mState = null;
		mSessionId = 0;
		data = null;
	}
	
	/**
	 * Constructor
	 * @param state of communication (e.g. addview)
	 * @param sessionId id of actual communication with server
	 * @param data object with inner data of communication
	 */
	public ParsedMessage(XmlParsers.State state, int sessionId, Object data) {
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
	public int getSessionId(){
		return mSessionId;
	}
	
	/**
	 * Setter
	 * @param sessionId
	 */
	public void setSessionId(int sessionId){
		mSessionId = sessionId;
	}

}
