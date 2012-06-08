package at.sti2.spark.network;

public class SparkwaveEndOfStreamException extends Exception {

	private static final long serialVersionUID = 1L;

	public SparkwaveEndOfStreamException() {
	}

	public SparkwaveEndOfStreamException(String arg0) {
		super(arg0);
	}

	public SparkwaveEndOfStreamException(Throwable arg0) {
		super(arg0);
	}

	public SparkwaveEndOfStreamException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
