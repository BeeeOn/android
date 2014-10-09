/**
 * 
 */
package cz.vutbr.fit.iha.network.exception;

/**
 * @author ThinkDeep
 * 
 */
public class NotRegException extends NetworkException {

	private static final long serialVersionUID = 1L;

	public NotRegException() {
	}

	/**
	 * @param detailMessage
	 */
	public NotRegException(String detailMessage) {
		super(detailMessage);
	}

	/**
	 * @param throwable
	 */
	public NotRegException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public NotRegException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
