/**
 * 
 */
package cz.vutbr.fit.iha.exception;

/**
 * XmlVersionMismatchException
 * @author ThinkDeep
 *
 */
public class XmlVerMisException extends Exception {

	private static final long serialVersionUID = 1L;

	public XmlVerMisException() { }

	/**
	 * @param detailMessage
	 */
	public XmlVerMisException(String detailMessage) {
		super(detailMessage);
	}

	/**
	 * @param throwable
	 */
	public XmlVerMisException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public XmlVerMisException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
