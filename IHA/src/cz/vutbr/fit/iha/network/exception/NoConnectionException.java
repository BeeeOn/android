/**
 * 
 */
package cz.vutbr.fit.iha.network.exception;

/**
 * @author ThinkDeep
 * 
 */
public class NoConnectionException extends NetworkException {

	private static final long serialVersionUID = 1L;

	public NoConnectionException() {
	}

	/**
	 * @param detailMessage
	 */
	public NoConnectionException(String detailMessage) {
		super(detailMessage);
	}

	/**
	 * @param throwable
	 */
	public NoConnectionException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public NoConnectionException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
