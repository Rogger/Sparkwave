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

package at.sti2.spark.rete.condition;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Triple pattern represents an ordered list of triple conditions
 * 
 * @author srdkom
 */
public class TriplePatternGraph {

	private List <TripleCondition> conditions = null;

	//The timewindow unit is ms
	private long timeWindowLength = 0l;
	
	public TriplePatternGraph() {
		super();
		conditions = new ArrayList <TripleCondition> ();
	}

	public List<TripleCondition> getConditions() {
		return conditions;
	}
	
	public void addTripleCondition(TripleCondition condition) {
		conditions.add(condition);
	}
	
	public TripleCondition getConditionByIndex(int index){
		return conditions.get(index);
	}
	
	public long getTimeWindowLength(){
		return timeWindowLength;
	}
	
	public void setTimeWindowLength(long timeWindowLength){
		this.timeWindowLength = timeWindowLength;
	}
	
	public String toString(){
		
		StringBuffer buffer = new StringBuffer();
		
		for (TripleCondition condition : conditions)
			for (TripleConstantTest constantTest : condition.getConstantTests()){
				buffer.append(constantTest.getTestField());
				buffer.append(' ');
				buffer.append(constantTest.getLexicalTestSymbol());
				buffer.append('\n');
			}
		
		return buffer.toString();
	}
}
