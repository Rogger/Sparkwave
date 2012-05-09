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
import java.util.List;
import java.util.Set;
import java.util.Vector;

import at.sti2.spark.core.condition.TripleCondition;
import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFTriple.Field;
import at.sti2.spark.core.triple.RDFValue;
import at.sti2.spark.rete.Token;
import at.sti2.spark.rete.WorkingMemoryElement;
import at.sti2.spark.rete.alpha.AlphaMemory;
import at.sti2.spark.rete.node.RETENode;

import com.google.common.collect.Sets;

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
		
		List<Token> items = ((BetaMemory) parent).getItems();
		
		// Look into the beta memory to find any token for which tests succeed.
			
			for (Token betaMemoryToken : items) {

				Vector<Token> wmeTokenVect = getTokenVect(betaMemoryToken);

			// Check if variables have the same value
			if (performTests(betaMemoryToken, wme, wmeTokenVect)) {

					// Check if the token and wme are falling into a window
					if (!(wme.getTriple().isPermanent()) && (!performTimeWindowTest(betaMemoryToken, wme)))
						continue;

					for (RETENode reteNode : children)
						reteNode.leftActivate(betaMemoryToken, wme);
				}
			}

		// If the join node is under dummy root beta node left activation should
		// fire
		if (((BetaMemory) parent).isRootNode())
			for (RETENode reteNode : children)
				if (reteNode instanceof BetaMemory)
					((BetaMemory) reteNode).leftActivate(null, wme);
				else
					((ProductionNode) reteNode).leftActivate(null, wme);
	}

	/**
	 * Activation coming from a beta/root node.
	 */
	@Override
	public void leftActivate(Token token) {

		Vector<Token> tokenVector = getTokenVect(token);
		leftActivate(token,tokenVector, alphaMemory);

	}
	
	/**
	 * Left Activate
	 * @param token
	 * @param alphaMemory
	 */
	private void leftActivate(Token token, Vector<Token> tokenVect,
			AlphaMemory alphaMemory) {

		Set<WorkingMemoryElement> resultSet = null;
		Set<WorkingMemoryElement> intermediateSet = null;
		for (JoinNodeTest test : tests) {

			Field arg1Field = test.getArg1Field();
			Field arg2Field = test.getArg2Field();
			Token parentToken = tokenVect.get(test.getArg2ConditionNumber());
			RDFValue testTokenValue = parentToken.getWme().getTriple()
					.getRDFTriple().getValueOfField(test.getArg2Field());

			if (arg1Field == RDFTriple.Field.SUBJECT) {
				intermediateSet = alphaMemory.getIndexStructure()
						.getElementFromSubject(testTokenValue);
			} else if (arg1Field == RDFTriple.Field.PREDICATE) {
				intermediateSet = alphaMemory.getIndexStructure()
						.getElementFromPredicate(testTokenValue);
			} else if (arg1Field == RDFTriple.Field.OBJECT) {
				intermediateSet = alphaMemory.getIndexStructure()
						.getElementFromObject(testTokenValue);
			}

			if (resultSet == null && intermediateSet != null) {
				resultSet = intermediateSet;
			} else if (intermediateSet != null) {
				resultSet = Sets.intersection(resultSet, intermediateSet);
			}

		}

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
		else if (tripleTimestamp > token.getEndTime())
			return (tripleTimestamp - tokenStartTime) < timeWindowLength;
		else
			return tokenEndTime - tokenStartTime < timeWindowLength;
	}

	public boolean performTests(Token token, WorkingMemoryElement wme,Vector<Token> parentTokens) {

		RDFValue valueArg1;
		RDFValue valueArg2;

		for (JoinNodeTest test : tests) {

			valueArg1 = wme.getTriple().getRDFTriple().getValueOfField(test.getArg1Field());

			// TODO Fix this for faster processing; instead of using indices
			// maybe we can use pointers?!
			// The value I have is the one of index in the tree, while the only
			// thing I have is the token from which to start.

			int index = test.getArg2ConditionNumber();
			Token wmeToken = parentTokens.get(index);
			valueArg2 = wmeToken.getWme().getTriple().getRDFTriple().getValueOfField(test.getArg2Field());

			if (!valueArg1.equals(valueArg2))
				return false;
		}

		return true;
	}

	private Vector<Token> getTokenVect(Token token) {
		Vector<Token> tokenVect = new Vector<Token>();
		Token currentToken = token;
		while (currentToken != null) {
			tokenVect.add(0, currentToken);
			currentToken = currentToken.getParent();
		}
		return tokenVect;
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