package cz.vutbr.fit.intelligenthomeanywhere.exception;

public class NotImplementedException extends UnsupportedOperationException {

	private static final long serialVersionUID = 8439357910844353631L;

	public NotImplementedException() {
	}

	public NotImplementedException(String detailMessage) {
		super(detailMessage);
	}

	public NotImplementedException(Throwable cause) {
		super(cause);
	}

	public NotImplementedException(String message, Throwable cause) {
		super(message, cause);
	}

}
