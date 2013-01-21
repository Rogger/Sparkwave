/*
 * Copyright (c) 2010, University of Innsbruck, Austria.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package at.sti2.spark.grammar.pattern;

/**
 * TemporalBeforeGraphPattern represents two graphPatterns connected by BEFORE(int,int)
 * @author michaelrogger
 */
public class TemporalBeforeGraphPattern implements GraphPattern {
	
	private GraphPattern left;
	private GraphPattern right;
	private int lowerBound;
	private int upperBound;
	
	public TemporalBeforeGraphPattern() {
		super();
	}
	
	public void setLeft(GraphPattern left){
		this.left = left;
	}
	
	public void setRight(GraphPattern right){
		this.right = right;
	}

	public GraphPattern getLeft(){
		return left;
	}
	
	public GraphPattern getRight(){
		return right;
	}

	public int getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(int lowerBound) {
		this.lowerBound = lowerBound;
	}

	public int getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(int upperBound) {
		this.upperBound = upperBound;
	}

	public String toString(){
		
		StringBuffer buffer = new StringBuffer();

//		buffer.append("TRIPLES\n");
//		for (TripleCondition condition : triples){
//			for (TripleConstantTest constantTest : condition.getConstantTests()){
//				buffer.append(constantTest.getTestField());
//				buffer.append(' ');
//				buffer.append(constantTest.getLexicalTestSymbol());
//				buffer.append('\n');
//			}
//		}
//		
//		buffer.append("TIMEWINDOW\n");
//		buffer.append(timeWindowLength).append("\n");
//		
//		buffer.append("FILTER\n");
//		for(FilterExpression expression : filters){
//			buffer.append(expression.toString()).append("\n");
//		}
		
		return buffer.toString();
	}

	@Override
	public int getChildSize() {
		int size = 0;
		
		if(left!=null)
			size++;
		if(right!=null)
			size++;
		
		return size;
	}

	@Override
	public GraphPattern getChild(int index) {
		if(index==0 && left!=null)
			return left;
		else if(index==1 && right!=null)
			return right;
		else
			return null;
	}

}
