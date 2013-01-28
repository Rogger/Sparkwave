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
import at.sti2.spark.grammar.pattern.expression.FilterExpression;

/**
 * GroupGraphPattern represents a set of triple conditions, a timewindow and an optional filter
 * @author michaelrogger
 */
public class GroupGraphPattern implements GraphPattern {

	
	private List <TripleCondition> triples = null;

	//The timewindow unit is ms
	private long timeWindowLength = 0l;
	
	//filter
	private List<FilterExpression> filters = null;
	
	public GroupGraphPattern() {
		super();
		triples = new ArrayList <TripleCondition> ();
		filters = new ArrayList<FilterExpression>();
	}

	public List<TripleCondition> getConditions() {
		return triples;
	}
	
	public void addWhereCondition(TripleCondition condition) {
		triples.add(condition);
	}
	
	public TripleCondition getWhereConditionByIndex(int index){
		return triples.get(index);
	}
	
	public List<FilterExpression> getFilters(){
		return filters;
	}
	
	/**
	 * Adds a list of filters to current filter list
	 * @param filters
	 */
	public void addFilters(List<FilterExpression> filters){
		this.filters.addAll(filters);
	}
	
	/**
	 * Adds a filter to current filter list
	 * @param filter
	 */
	public void addFilter(FilterExpression filter){
		this.filters.add(filter);
	}
		
	public long getTimeWindowLength(){
		return timeWindowLength;
	}
	
	public void setTimeWindowLength(long timeWindowLength){
		this.timeWindowLength = timeWindowLength;
	}
		
	public String toString(){
		
		StringBuffer buffer = new StringBuffer();

		for (TripleCondition condition : triples){
			buffer.append(condition.getConditionTriple()).append(" .\n");
//			for (TripleConstantTest constantTest : condition.getConstantTests()){
//				buffer.append(constantTest.getTestField());
//				buffer.append(' ');
//				buffer.append(constantTest.getLexicalTestSymbol());
//				buffer.append('\n');
//			}
		}
		
		buffer.append("TIMEWINDOW (").append(timeWindowLength).append(") \n");
		
//		buffer.append("FILTER\n");
//		for(FilterExpression expression : filters){
//			buffer.append(expression.toString()).append("\n");
//		}
		
		return buffer.toString();
	}

	@Override
	public int getChildSize() {
		return 0;
	}

	@Override
	public GraphPattern getChild(int index) {
		return null;
	}

}
