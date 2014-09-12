/**
 * 
 */
package cz.vutbr.fit.iha.network.exception;

/**
 * @author ThinkDeep
 * 
 */
public class CommunicationException extends NetworkException {

	private static final long serialVersionUID = 1L;

	public CommunicationException() {
	}

	/**
	 * @param detailMessage
	 */
	public CommunicationException(String detailMessage) {
		super(detailMessage);
	}

	/**
	 * @param throwable
	 */
	public CommunicationException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public CommunicationException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
