/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.exception;

/**
 * @author ThinkDeep
 *
 */
public class NotRegAException extends CommunicationException {

	private static final long serialVersionUID = 1L;

	public NotRegAException() {	}

	/**
	 * @param detailMessage
	 */
	public NotRegAException(String detailMessage) {
		super(detailMessage);
	}

	/**
	 * @param throwable
	 */
	public NotRegAException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public NotRegAException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
