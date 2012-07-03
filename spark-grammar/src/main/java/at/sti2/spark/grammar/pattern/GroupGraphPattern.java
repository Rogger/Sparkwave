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

import java.util.ArrayList;
import java.util.List;

import at.sti2.spark.core.triple.TripleCondition;
import at.sti2.spark.core.triple.TripleConstantTest;
import at.sti2.spark.grammar.pattern.expression.Expression;

/**
 * group graph pattern
 * @author michaelrogger
 */
public class GroupGraphPattern {

	
	private List <TripleCondition> triples = null;

	//The timewindow unit is ms
	private long timeWindowLength = 0l;
	
	//filter
	private List<Expression> expressions = null;
	
	public GroupGraphPattern() {
		super();
		triples = new ArrayList <TripleCondition> ();
		expressions = new ArrayList<Expression>();
	}

	public List<TripleCondition> getWhereConditions() {
		return triples;
	}
	
	public void addWhereCondition(TripleCondition condition) {
		triples.add(condition);
	}
	
	public TripleCondition getWhereConditionByIndex(int index){
		return triples.get(index);
	}
	
	public List<Expression> getFilter(){
		return expressions;
	}
	
	public void setFilter(List<Expression> expressions){
		this.expressions = expressions;
	}
		
	public long getTimeWindowLength(){
		return timeWindowLength;
	}
	
	public void setTimeWindowLength(long timeWindowLength){
		this.timeWindowLength = timeWindowLength;
	}
		
	public String toString(){
		
		StringBuffer buffer = new StringBuffer();

		buffer.append("TRIPLES\n");
		for (TripleCondition condition : triples){
			for (TripleConstantTest constantTest : condition.getConstantTests()){
				buffer.append(constantTest.getTestField());
				buffer.append(' ');
				buffer.append(constantTest.getLexicalTestSymbol());
				buffer.append('\n');
			}
		}
		
		buffer.append("TIMEWINDOW\n");
		buffer.append(timeWindowLength).append("\n");
		
		buffer.append("FILTER\n");
		for(Expression expression : expressions){
			buffer.append(expression.toString()).append("\n");
		}
		
		return buffer.toString();
	}

}
