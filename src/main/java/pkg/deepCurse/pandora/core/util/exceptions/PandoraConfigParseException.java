package pkg.deepCurse.pandora.core.util.exceptions;

public class PandoraConfigParseException extends RuntimeException { // TODO add extra trace functionality, and an automatic error handler that opens a client screen

	public PandoraConfigParseException() {
		super();
	}

	public PandoraConfigParseException(String message) {
		super(message);
	}

	public PandoraConfigParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public PandoraConfigParseException(Throwable cause) {
		super(cause);
	}

	protected PandoraConfigParseException(String message, Throwable cause,
			boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
