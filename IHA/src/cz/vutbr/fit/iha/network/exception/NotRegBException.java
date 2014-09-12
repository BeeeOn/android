/**
 * 
 */
package cz.vutbr.fit.iha.network.exception;

/**
 * @author ThinkDeep
 * 
 */
public class NotRegBException extends NotRegException {

	private static final long serialVersionUID = 1L;

	public NotRegBException() {
	}

	/**
	 * @param detailMessage
	 */
	public NotRegBException(String detailMessage) {
		super(detailMessage);
	}

	/**
	 * @param throwable
	 */
	public NotRegBException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public NotRegBException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
