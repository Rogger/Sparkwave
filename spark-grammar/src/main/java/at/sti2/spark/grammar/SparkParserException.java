package at.sti2.spark.grammar;


public class SparkParserException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String warnings;

	public SparkParserException() {
		super();
	}
	
	public SparkParserException(String message){
		super(message);
	}
	
	public SparkParserException(String message, String warnings){
		super(message);
		this.warnings = warnings;
	}
	
	public SparkParserException(String message, String warnings, Throwable cause){
		super(message, cause);
		this.warnings = warnings;
	}
	
	public SparkParserException(Throwable cause){
		super(cause);
	}
	
	@Override
	public String getMessage() {
		return super.getMessage() + ": "+warnings;
	}
	
}
