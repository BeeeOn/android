/**
 * 
 */
package cz.vutbr.fit.iha.exception;

import cz.vutbr.fit.iha.adapter.parser.FalseAnswer;

/**
 * FalseException
 * @author ThinkDeep
 *
 */
public class FalseException extends NetworkException {

	private static final long serialVersionUID = 1L;
	private FalseAnswer mFalseAnswer;

	public FalseException() { }

	/**
	 * @param detailMessage
	 */
	public FalseException(String detailMessage) {
		super(detailMessage);
	}

	/**
	 * @param throwable
	 */
	public FalseException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public FalseException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}
	
	/**
	 * Constructor
	 * @param answer with error code and message
	 */
	public FalseException(FalseAnswer answer){
		mFalseAnswer = answer;
	}
	
	/**
	 * Method return info about false answer
	 * @return FalseAnswer Obejct
	 */
	public FalseAnswer getDetail(){
		return mFalseAnswer;
	}

}
