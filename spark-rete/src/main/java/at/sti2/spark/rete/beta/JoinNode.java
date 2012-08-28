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

package at.sti2.spark.rete.beta;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import at.sti2.spark.core.collect.IndexStructure;
import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.TripleCondition;
import at.sti2.spark.core.triple.RDFTriple.Field;
import at.sti2.spark.core.triple.RDFValue;
import at.sti2.spark.rete.Token;
import at.sti2.spark.rete.WorkingMemoryElement;
import at.sti2.spark.rete.alpha.AlphaMemory;
import at.sti2.spark.rete.node.RETENode;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

public class JoinNode extends RETENode {

	private AlphaMemory alphaMemory = null;
	private List<JoinNodeTest> tests = null;
	private long timeWindowLength = 0l;
	private TripleCondition tripleCondition = null;

	public JoinNode(TripleCondition tripleCondition, long timeWindowLength) {
		tests = new ArrayList<JoinNodeTest>();
		this.timeWindowLength = timeWindowLength;
		this.tripleCondition = tripleCondition;
	}

	public AlphaMemory getAlphaMemory() {
		return alphaMemory;
	}

	public void setAlphaMemory(AlphaMemory alphaMemory) {
		this.alphaMemory = alphaMemory;
	}

	public List<JoinNodeTest> getTests() {
		return tests;
	}

	public void addJoinNodeTest(JoinNodeTest test) {
		tests.add(test);
	}

	public TripleCondition getTripleCondition() {
		return tripleCondition;
	}

	/**
	 * Activation coming from an alpha memory.
	 */
	@Override
	public void rightActivate(WorkingMemoryElement wme) {
		
		// If the join node is under dummy root beta node left activation should
		// fire		
		if (((BetaMemory) parent).isRootNode()){
			
			for (RETENode reteNode : children)
				reteNode.leftActivate(null, wme);
			
		}else{
			
			if(tests.size()>0){
				
				Set<Token> resultSet = null;
				Set<Token> intermediateSet = new LinkedHashSet<Token>();
				
				for (JoinNodeTest test : tests) {
					
					Field arg2Field = test.getArg2Field();
					RDFValue testTokenValue = wme.getTriple().getRDFTriple().getValueOfField(test.getArg1Field());
					
					Set<Token> tokensFromIndex = null;
					if (arg2Field == RDFTriple.Field.SUBJECT) {
						tokensFromIndex = test.getBetaMemory().getIndexStructure().getElementsFromSubjectIndex(testTokenValue);
					} else if (arg2Field == RDFTriple.Field.PREDICATE) {
						tokensFromIndex = test.getBetaMemory().getIndexStructure().getElementsFromPredicateIndex(testTokenValue);
					} else if (arg2Field == RDFTriple.Field.OBJECT) {
						tokensFromIndex = test.getBetaMemory().getIndexStructure().getElementsFromObjectIndex(testTokenValue);
					}
					
					
					// Get Tokens at the level of parent beta memory
					for(Token token : tokensFromIndex){
						Set<Token> childTokensAtBetaMemory = token.getChildTokensAtBetaMemory((BetaMemory)parent);
						intermediateSet = Sets.union(childTokensAtBetaMemory, intermediateSet);
					}
					
					if (resultSet == null && intermediateSet != null) {
						resultSet = intermediateSet;
					} else if (intermediateSet != null) {
						resultSet = Sets.intersection(resultSet, intermediateSet);
					}
					
				}
				
				if(resultSet!=null){
					for (Token token : resultSet) {
						
						// Check if the token and wme are falling into a window
						if (token.getWme().getTriple().isPermanent() || performTimeWindowTest(token, wme)) {
							// All tests successful
							for (RETENode reteNode : children)
								reteNode.leftActivate(token, wme);
						}
						
					}				
				}
			}
			else{
				ArrayList<Token> elementsFromTokenQueue = ((BetaMemory)parent).getIndexStructure().getElementsFromTokenQueue();
				for (RETENode reteNode : children)
					for(Token token : elementsFromTokenQueue)
					reteNode.leftActivate(token, wme);
			}
			
		}
	}

	/**
	 * Activation coming from a beta/root node.
	 */
	@Override
	public void leftActivate(Token token) {
		leftActivate(token, alphaMemory);
	}
	
	/**
	 * Checks whether there are wme in index structure that match tests and token
	 * @param token
	 * @param alphaMemory
	 */
	private void leftActivate(Token token, AlphaMemory alphaMemory) {
		
		if(tests.size()>0){
			Set<WorkingMemoryElement> resultSet = null;
			Set<WorkingMemoryElement> intermediateSet = null;
			for (JoinNodeTest test : tests) {
				
				Field arg1Field = test.getArg1Field();
				Token parentToken = token.getParentTokenAtBetaMemory(test.getBetaMemory());
				RDFValue testTokenValue = parentToken.getWme().getTriple().getRDFTriple().getValueOfField(test.getArg2Field());
				
				if (arg1Field == RDFTriple.Field.SUBJECT) {
					intermediateSet = alphaMemory.getIndexStructure()
							.getElementsFromSubjectIndex(testTokenValue);
				} else if (arg1Field == RDFTriple.Field.PREDICATE) {
					intermediateSet = alphaMemory.getIndexStructure()
							.getElementsFromPredicateIndex(testTokenValue);
				} else if (arg1Field == RDFTriple.Field.OBJECT) {
					intermediateSet = alphaMemory.getIndexStructure()
							.getElementsFromObjectIndex(testTokenValue);
				}
				
				if (resultSet == null && intermediateSet != null) {
					resultSet = intermediateSet;
				} else if (intermediateSet != null) {
					resultSet = Sets.intersection(resultSet, intermediateSet);
				}
				
			}
			
			if(resultSet!=null){
				for (WorkingMemoryElement wme : resultSet) {
					
					// Check if the token and wme are falling into a window
					if (wme.getTriple().isPermanent()
							|| performTimeWindowTest(token, wme)) {
						// All tests successful
						for (RETENode reteNode : children)
							reteNode.leftActivate(token, wme);
					}
					
				}
			}
			
		}
		else{
			ArrayList<WorkingMemoryElement> elementsFromTokenQueue = alphaMemory.getIndexStructure().getElementsFromTokenQueue();
			for(WorkingMemoryElement wme : elementsFromTokenQueue){
				
				if (wme.getTriple().isPermanent()
						|| performTimeWindowTest(token, wme)) {
					
					for (RETENode reteNode : children){
						reteNode.leftActivate(token, wme);					
					}
				}
			}
		}


	}

	public void setTests(List<JoinNodeTest> tests) {
		this.tests = tests;
	}

	private boolean performTimeWindowTest(Token token, WorkingMemoryElement wme) {
		Triple triple = wme.getTriple();
		long tripleTimestamp = triple.getTimestamp();
		long tokenStartTime = token.getStartTime();
		long tokenEndTime = token.getEndTime();

		if (tripleTimestamp < tokenStartTime)
			return (tokenEndTime - tripleTimestamp) < timeWindowLength;
		else if (tripleTimestamp > tokenEndTime)
			return (tripleTimestamp - tokenStartTime) < timeWindowLength;
		else
			return tokenEndTime - tokenStartTime < timeWindowLength;
	}

	@Override
	public void leftActivate(Token token, WorkingMemoryElement wme) {
		// TODO Auto-generated method stub
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("Tests: \n");
		for (JoinNodeTest test : tests) {
			buffer.append(test.toString());
			buffer.append("\n");
		}

		return buffer.toString();
	}
}