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

/**
 * The construct part of a pattern, consisting of triple conditions
 * @author michaelrogger
 */
public class Construct{

	
	private List <TripleCondition> conditions = null;

	public Construct() {
		super();
		conditions = new ArrayList <TripleCondition> ();
	}

	public List<TripleCondition> getConditions() {
		return conditions;
	}
	
	public void setConditions(List<TripleCondition> conditions){
		this.conditions = conditions;
	}
	
	public void addCondition(TripleCondition condition){
		this.conditions.add(condition);
	}
	
	@Override
	public String toString(){
		
		StringBuffer buffer = new StringBuffer();

		for (TripleCondition condition : conditions){
			buffer.append(condition.getConditionTriple()).append(" .\n");
		}
		
		return buffer.toString();
	}

}
