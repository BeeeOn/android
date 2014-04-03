/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.exception;

/**
 * CommunicationVersionMismatchException
 * @author ThinkDeep
 *
 */
public class ComVerMisException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4221303886954019279L;

	/**
	 * Constructor
	 */
	public ComVerMisException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param detailMessage
	 */
	public ComVerMisException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param throwable
	 */
	public ComVerMisException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public ComVerMisException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

}
