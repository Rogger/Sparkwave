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

import org.apache.log4j.Logger;

import at.sti2.spark.core.condition.TripleCondition;
import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.rete.Token;
import at.sti2.spark.rete.WorkingMemoryElement;
import at.sti2.spark.rete.alpha.AlphaMemory;
import at.sti2.spark.rete.node.RETENode;

public class JoinNode extends RETENode {

	static Logger logger = Logger.getLogger(JoinNode.class);

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

		// logger.debug("01_Right activate join node with triple " + wme.toString());
		// Look into the beta memory to find any token for which tests succeed.
		// logger.debug("02_Entered synchronization over beta parents.");


		List<WorkingMemoryElement> wmeOutOfWindow = new ArrayList<WorkingMemoryElement>();
		long gcThresholdTimestamp = System.currentTimeMillis() - timeWindowLength;

		Iterator<Token> betaMemoryTokenIter = ((BetaMemory) parent).getItems()
				.iterator();

		while (betaMemoryTokenIter.hasNext()) {

			Token betaMemoryToken = betaMemoryTokenIter.next();

			//garbage collect wme that is out of window
			WorkingMemoryElement tokenWME = betaMemoryToken.getWme();
			if (tokenWME.getTriple().getTimestamp() < gcThresholdTimestamp) {
				wmeOutOfWindow.add(tokenWME);
			} else {

				// logger.debug("Checking beta memory token " +
				// betaMemoryToken.toString());

				Vector<Token> wmeTokenVect = getTokenVect(betaMemoryToken);

				// logger.debug("Retrieved token vector...");

				// Check if variables have the same value
				if (performTests(betaMemoryToken, wme, wmeTokenVect)) {

					// logger.debug("Perform tests: SUCCESSFULL!");

					// Check if the token and wme are falling into a window
					if (!(wme.getTriple().isPermanent())
							&& (!performTimeWindowTest(betaMemoryToken, wme))) {
						continue;

					}
					for (RETENode reteNode : children)
						if (reteNode instanceof BetaMemory)
							((BetaMemory) reteNode).leftActivate(
									betaMemoryToken, wme);
						else
							((ProductionNode) reteNode).leftActivate(
									betaMemoryToken, wme);

				}
			}

		}

		//clean wme that are marked as out of window
		for (WorkingMemoryElement cWME : wmeOutOfWindow) {
			cWME.remove();
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

		// logger.debug("JoinNode left activated with token " + token.toString()
		// + " and alphaMemory " + alphaMemory.hashCode());
		// logger.debug("Alpha Memory " + alphaMemory.toString());

		Vector<Token> wmeTokenVect = getTokenVect(token);

		// permanent items
		leftActivatePermanent(token, alphaMemory.getPermanentItems(),wmeTokenVect);

		// dynamic items
		leftActivateDynamic(token, alphaMemory.getItems(), wmeTokenVect);

	}

	private void leftActivatePermanent(Token token,
			List<WorkingMemoryElement> listItems, Vector<Token> wmeTokenVect) {

		// logger.debug("Activated permanent.");

		for (WorkingMemoryElement alphaWME : listItems) {

			// Check if two WME and token can be joined
			if (performTests(token, alphaWME, wmeTokenVect)) {

				for (RETENode reteNode : children)
					if (reteNode instanceof BetaMemory)
						((BetaMemory) reteNode).leftActivate(token, alphaWME);
					else
						((ProductionNode) reteNode).leftActivate(token,
								alphaWME);
			}
		}
	}

	private void leftActivateDynamic(Token token,
			List<WorkingMemoryElement> listItems, Vector<Token> wmeTokenVect) {

		// logger.debug("Activated dynamic.");
		// logger.debug("Entered synchornized part over listItems.");

		long gcThresholdTimestamp = System.currentTimeMillis() - timeWindowLength;

		Iterator<WorkingMemoryElement> iterator = listItems.iterator();
		while (iterator.hasNext()) {

			WorkingMemoryElement alphaWME = iterator.next();
			
			//garbage collect wme that is out of window
			if (alphaWME.getTriple().getTimestamp() < gcThresholdTimestamp) {
				alphaWME.remove();
				iterator.remove();
			} else {

				// Check if two WME and token can be joined
				if (performTests(token, alphaWME, wmeTokenVect)) {

					// Check if the token and wme are falling into a window
					if (!performTimeWindowTest(token, alphaWME))
						continue;

					for (RETENode reteNode : children)
						if (reteNode instanceof BetaMemory)
							((BetaMemory) reteNode).leftActivate(token,
									alphaWME);
						else
							((ProductionNode) reteNode).leftActivate(token,
									alphaWME);
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
		else if (tripleTimestamp > token.getEndTime())
			return (tripleTimestamp - tokenStartTime) < timeWindowLength;
		else
			return tokenEndTime - tokenStartTime < timeWindowLength;
	}

	public boolean performTests(Token token, WorkingMemoryElement wme,
			Vector<Token> parentTokens) {

		String lexicalValueArg1;
		String lexicalvalueArg2;

		for (JoinNodeTest test : tests) {

			lexicalValueArg1 = wme.getTriple().getRDFTriple()
					.getLexicalValueOfField(test.getArg1Field());

			// TODO Fix this for faster processing; instead of using indices
			// maybe we can use pointers?!
			// The value I have is the one of index in the tree, while the only
			// thing I have is the token from which to start.

			int index = test.getArg2ConditionNumber();
			Token wmeToken = parentTokens.get(index);
			lexicalvalueArg2 = wmeToken.getWme().getTriple().getRDFTriple()
					.getLexicalValueOfField(test.getArg2Field());

			if (!lexicalValueArg1.equals(lexicalvalueArg2))
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
