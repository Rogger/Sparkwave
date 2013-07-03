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

package at.sti2.spark.core.triple;

import java.util.ArrayList;
import java.util.List;


/**
 * A condition represents an RDF triple which may include RDFVariable at any position
 * 
 * @author srdkom
 */
public class TripleCondition {

	private RDFTriple conditionTriple = null;
	private List <TripleConstantTest> constantTests = null;
	
	public TripleCondition() {
		super();
		constantTests = new ArrayList <TripleConstantTest> ();
	}

	public TripleCondition(RDFTriple conditionTriple) {
		super();
		this.conditionTriple = conditionTriple;
		constantTests = new ArrayList <TripleConstantTest> ();
	}

	public RDFTriple getConditionTriple() {
		return conditionTriple;
	}

	public void setConditionTriple(RDFTriple conditionTriple) {
		this.conditionTriple = conditionTriple;
	}
	
	public TripleConstantTest getSubjectConstantTest(){
		TripleConstantTest test = null;
		
		for (TripleConstantTest constantTest : constantTests)
			if (constantTest.getTestField().equals(RDFTriple.Field.SUBJECT)){
				test = constantTest;
				break;
			}
				
		return test;
	}
	
	public TripleConstantTest getPredicateConstantTest(){
		TripleConstantTest test = null;
		
		for (TripleConstantTest constantTest : constantTests)
			if (constantTest.getTestField().equals(RDFTriple.Field.PREDICATE)){
				test = constantTest;
				break;
			}
				
		return test;
	}
	
	public TripleConstantTest getObjectConstantTest(){
		TripleConstantTest test = null;
		
		for (TripleConstantTest constantTest : constantTests)
			if (constantTest.getTestField().equals(RDFTriple.Field.OBJECT)){
				test = constantTest;
				break;
			}
				
		return test;
	}
	
	public boolean isVariableAtSubject(){
		return (conditionTriple.getSubject() instanceof RDFVariable);
	}
	
	public boolean isVariableAtPredicate(){
		return (conditionTriple.getPredicate() instanceof RDFVariable);
	}
	
	public boolean isVariableAtObject(){
		return (conditionTriple.getObject() instanceof RDFVariable);
	}
	
	public List <TripleConstantTest> getConstantTests(){
		return constantTests;
	}
	
	public void addConstantTest(TripleConstantTest tripleConstantTest){
		constantTests.add(tripleConstantTest); 
	}
	
	@Override
	public String toString() {
		
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("CONSTANT TESTS");
		buffer.append(conditionTriple);
		buffer.append(' ');
		buffer.append(constantTests.toString());
		buffer.append('\n');
		
		return buffer.toString();
	}
	
	public StringBuffer formatString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(conditionTriple.formatString());
		return buffer;
	}
}
