package at.sti2.spark.grammar.pattern.expression;

public final class ExpressionNumericLiteral extends ExpressionAbstract{
	
	private final double value;
	
	public ExpressionNumericLiteral(double value) {
		this.value = value;
	}
	
	public double getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return Double.toString(value);
	}
}
