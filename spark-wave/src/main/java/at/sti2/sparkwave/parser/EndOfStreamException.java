package at.sti2.sparkwave.parser;

public class EndOfStreamException extends Exception {

	private static final long serialVersionUID = 1L;

	public EndOfStreamException() {
	}

	public EndOfStreamException(String arg0) {
		super(arg0);
	}

	public EndOfStreamException(Throwable arg0) {
		super(arg0);
	}

	public EndOfStreamException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
