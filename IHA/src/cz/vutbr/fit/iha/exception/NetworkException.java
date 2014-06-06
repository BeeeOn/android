/**
 * 
 */
package cz.vutbr.fit.iha.exception;

public class NetworkException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NetworkException() {}

	/**
	 * @param detailMessage
	 */
	public NetworkException(String detailMessage) {
		super(detailMessage);
	}

	/**
	 * @param throwable
	 */
	public NetworkException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public NetworkException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}
	
}
