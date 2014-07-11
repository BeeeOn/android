/**
 * 
 */
package cz.vutbr.fit.iha.adapter.parser;

/**
 * @author ThinkDeep
 *
 */
public class ParsedMessage {

	private String mState;
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
	public ParsedMessage(String state, int sessionId, Object data) {
		mState = state;
		mSessionId = sessionId;
		data = null;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public String getState(){
		return mState;
	}
	
	/**
	 * Setter
	 * @param state
	 */
	public void setState(String state){
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
