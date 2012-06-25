package at.sti2.spark.grammar.pattern.expression;

public final class ExpressionVariable extends ExpressionAbstract{
	
	private final String value;
	
	public ExpressionVariable(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return "?"+value;
	}
}
