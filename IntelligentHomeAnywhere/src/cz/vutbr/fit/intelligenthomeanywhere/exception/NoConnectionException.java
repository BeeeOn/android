/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.exception;

/**
 * @author ThinkDeep
 *
 */
public class NoConnectionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8609203582603372621L;

	/**
	 * 
	 */
	public NoConnectionException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param detailMessage
	 */
	public NoConnectionException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param throwable
	 */
	public NoConnectionException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public NoConnectionException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

}
