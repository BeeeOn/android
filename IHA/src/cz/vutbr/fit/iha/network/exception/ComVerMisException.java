/**
 * 
 */
package cz.vutbr.fit.iha.network.exception;

/**
 * CommunicationVersionMismatchException
 * 
 * @author ThinkDeep
 * 
 */
public class ComVerMisException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ComVerMisException() {
	}

	/**
	 * @param detailMessage
	 */
	public ComVerMisException(String detailMessage) {
		super(detailMessage);
	}

	/**
	 * @param throwable
	 */
	public ComVerMisException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public ComVerMisException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
