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
import java.util.Iterator;
import java.util.List;
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

		// permanent items
		leftActivate(token, tokenVector, alphaMemory.getPermanentItems(), true);

		// dynamic items
		leftActivate(token, tokenVector, alphaMemory.getItems(), false);

	}


	private void leftActivate(Token token, Vector<Token> tokenVect, List<WorkingMemoryElement> listItems, boolean permanent) {

		int testsSize = tests.size();
		if (testsSize > 0 && listItems.size() > 0) {

			// Declare join node test variable
			JoinNodeTest test1 = null, test2 = null, test3 = null;
			Field test1Arg1 = null, test2Arg1 = null, test3Arg1 = null;
			RDFValue test1TokenValue = null, test2TokenValue = null, test3TokenValue = null;

			// Prepare tests
			if (testsSize == 1) {
				test1 = tests.get(0);
				test1Arg1 = test1.getArg1Field();
				Token parentToken = tokenVect.get(test1
						.getArg2ConditionNumber());
				test1TokenValue = parentToken.getWme().getTriple()
						.getRDFTriple().getValueOfField(test1.getArg2Field());
			}
			if (testsSize == 2) {
				test2 = tests.get(1);
				test2Arg1 = test2.getArg1Field();
				Token parentToken = tokenVect.get(test2
						.getArg2ConditionNumber());
				test2TokenValue = parentToken.getWme().getTriple()
						.getRDFTriple().getValueOfField(test2.getArg2Field());
			}
			if (testsSize == 3) {
				test3 = tests.get(2);
				test3Arg1 = test3.getArg1Field();
				Token parentToken = tokenVect.get(test3
						.getArg2ConditionNumber());
				test3TokenValue = parentToken.getWme().getTriple()
						.getRDFTriple().getValueOfField(test3.getArg2Field());
			}

//			synchronized (listItems) {
				for (WorkingMemoryElement alphaWME : listItems) {

					RDFTriple rdfTriple = alphaWME.getTriple().getRDFTriple();

					// Perform Test 1
					if (test1 != null) {
						RDFValue alphaWMEValue = rdfTriple
								.getValueOfField(test1Arg1);
						if (!alphaWMEValue.equals(test1TokenValue)) {
							continue;
						}
					}

					// Perform Test 2
					if (test2 != null) {
						RDFValue alphaWMEValue = rdfTriple
								.getValueOfField(test2Arg1);
						if (!alphaWMEValue.equals(test2TokenValue)) {
							continue;
						}
					}

					// Perform Test 3
					if (test3 != null) {
						RDFValue alphaWMEValue = rdfTriple
								.getValueOfField(test3Arg1);
						if (!alphaWMEValue.equals(test3TokenValue)) {
							continue;
						}
					}

					// Check if the token and wme are falling into a window
					if (!permanent && !performTimeWindowTest(token, alphaWME))
						continue;

					// All tests successful
					for (RETENode reteNode : children)
						reteNode.leftActivate(token, alphaWME);

				}
//			}
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
