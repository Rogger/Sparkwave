package at.sti2.spark.grammar.pattern.expression;

import at.sti2.spark.core.triple.RDFValue;

public final class FilterExpression {
	
	private final RDFValue left;
	private final RDFValue right;
	private final Operator operator;
	
	public FilterExpression(RDFValue left, RDFValue right, Operator operator) {
		this.left = left;
		this.right = right;
		this.operator = operator;
	}
	
	public RDFValue getLeft() {
		return left;
	}
	
	public RDFValue getRight() {
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
