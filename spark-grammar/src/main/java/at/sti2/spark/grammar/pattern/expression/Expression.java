package at.sti2.spark.grammar.pattern.expression;

public final class Expression {
	
	private final ExpressionAbstract left;
	private final ExpressionAbstract right;
	private final Operator operator;
	
	public Expression(ExpressionAbstract left, ExpressionAbstract right, Operator operator) {
		this.left = left;
		this.right = right;
		this.operator = operator;
	}
	
	public ExpressionAbstract getLeft() {
		return left;
	}
	
	public ExpressionAbstract getRight() {
		return right;
	}
	
	public Operator getOperator() {
		return operator;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(left).append(" ");
		sb.append(operator).append(" ");
		sb.append(right);
		return sb.toString();
	}
}
