/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.exception;

/**
 * XmlVersionMismatchException
 * @author ThinkDeep
 *
 */
public class XmlVerMisException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7259307611261716830L;

	/**
	 * Constructor
	 */
	public XmlVerMisException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param detailMessage
	 */
	public XmlVerMisException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param throwable
	 */
	public XmlVerMisException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public XmlVerMisException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

}