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

package at.sti2.spark.core.condition;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Triple pattern represents an ordered list of triple conditions
 * 
 * @author srdkom
 */
public class TriplePatternGraph {

	private List <TripleCondition> selectConditions = null;
	private List <TripleCondition> constructConditions = null;

	//The timewindow unit is ms
	private long timeWindowLength = 0l;
	
	public TriplePatternGraph() {
		super();
		selectConditions = new ArrayList <TripleCondition> ();
		constructConditions = new ArrayList <TripleCondition> ();
	}

	public List<TripleCondition> getSelectConditions() {
		return selectConditions;
	}
	
	public List<TripleCondition> getConstructConditions() {
		return constructConditions;
	}
	
	public void addSelectTripleCondition(TripleCondition condition) {
		selectConditions.add(condition);
	}
	
	public void addConstructTripleCondition(TripleCondition condition) {
		constructConditions.add(condition);
	}
	
	public TripleCondition getSelectConditionByIndex(int index){
		return selectConditions.get(index);
	}
	
	public TripleCondition getConstructConditionByIndex(int index){
		return constructConditions.get(index);
	}
	
	public long getTimeWindowLength(){
		return timeWindowLength;
	}
	
	public void setTimeWindowLength(long timeWindowLength){
		this.timeWindowLength = timeWindowLength;
	}
	
	public String toString(){
		
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("SELECT\n");
		
		for (TripleCondition condition : selectConditions)
			for (TripleConstantTest constantTest : condition.getConstantTests()){
				buffer.append(constantTest.getTestField());
				buffer.append(' ');
				buffer.append(constantTest.getLexicalTestSymbol());
				buffer.append('\n');
			}
		
		return buffer.toString();
	}
}
