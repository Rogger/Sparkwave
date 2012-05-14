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

package at.sti2.spark.rete;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import at.sti2.spark.core.collect.IndexStructure;
import at.sti2.spark.core.condition.TripleCondition;
import at.sti2.spark.core.condition.TripleConstantTest;
import at.sti2.spark.core.condition.TriplePatternGraph;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFTriple.Field;
import at.sti2.spark.core.triple.variable.RDFVariable;
import at.sti2.spark.rete.alpha.AlphaMemory;
import at.sti2.spark.rete.alpha.AlphaNode;
import at.sti2.spark.rete.alpha.ConstantObjectTestAlphaNode;
import at.sti2.spark.rete.alpha.ConstantPredicateTestAlphaNode;
import at.sti2.spark.rete.alpha.ConstantSubjectTestAlphaNode;
import at.sti2.spark.rete.alpha.ValueTestAlphaNode;
import at.sti2.spark.rete.alpha.WorkingMemory;
import at.sti2.spark.rete.beta.BetaMemory;
import at.sti2.spark.rete.beta.JoinNode;
import at.sti2.spark.rete.beta.JoinNodeTest;
import at.sti2.spark.rete.beta.ProductionNode;
import at.sti2.spark.rete.node.RETENode;

public class RETENetwork {

	private WorkingMemory workingMemory = null;
	private BetaMemory betaMemory = null;
	private List<TriplePatternGraph> triplePatternGraphs = null;

	static Logger logger = Logger.getLogger(RETENetwork.class);

	public RETENetwork() {

		// Initialize alpha memory
		workingMemory = new WorkingMemory();

		// Initialize beta memory
		betaMemory = new BetaMemory(); // This is dummy top node
		betaMemory.setRootNode();

		// Initialize list of triple pattern graphs
		triplePatternGraphs = new ArrayList<TriplePatternGraph>();
	}

	public RETENode buildOrShareBetaMemoryNode(RETENode parentNode) {

		for (RETENode childNode : parentNode.getChildren())
			if (childNode instanceof BetaMemory) // TODO Do they think here
													// about comparing types or
													// values
				return childNode;

		BetaMemory betaMemory = new BetaMemory();
		betaMemory.setParent(parentNode);

		parentNode.getChildren().add(betaMemory);
		betaMemory.update();

		return betaMemory;
	}

	public RETENode buildOrShareJoinNode(RETENode parentNode,
			AlphaMemory alphaMemory, List<JoinNodeTest> tests,
			TripleCondition tripleCondition, long timeWindowLength) {

		for (RETENode childNode : parentNode.getChildren())
			if ((childNode instanceof JoinNode)
					&& (((JoinNode) childNode).getAlphaMemory() == alphaMemory)
					&& (((JoinNode) childNode).getTests() == tests)) // TODO
																		// This
																		// is
																		// not a
																		// right
																		// way
																		// to
																		// compare
																		// lists
				return childNode;

		JoinNode joinNode = new JoinNode(tripleCondition, timeWindowLength);
		joinNode.setParent(parentNode);

		parentNode.getChildren().add(joinNode);
		joinNode.setAlphaMemory(alphaMemory);
		joinNode.setTests(tests);

		alphaMemory.getSuccessors().add(joinNode);

		return joinNode;
	}

	public List<JoinNodeTest> getTestsFromCondition(TripleCondition condition,
			List<TripleCondition> previousConditions,
			List<BetaMemory> betaMemories) {

		List<JoinNodeTest> tests = new ArrayList<JoinNodeTest>();

		// Test whether variable occurs at subject
		if (condition.isVariableAtSubject()) {

			TripleCondition earlierCondition = variableOccurs(
					(RDFVariable) condition.getConditionTriple().getSubject(),
					previousConditions);

			if (earlierCondition != null) {

				int indexOf = previousConditions.indexOf(earlierCondition);
				BetaMemory memory = betaMemories.get(indexOf);

				JoinNodeTest test = new JoinNodeTest(RDFTriple.Field.SUBJECT,
						getVariableField((RDFVariable) condition
								.getConditionTriple().getSubject(),
								earlierCondition), memory);

				tests.add(test);
			}
		}

		// Test whether variable occurs at predicate
		if (condition.isVariableAtPredicate()) {

			TripleCondition earlierCondition = variableOccurs(
					(RDFVariable) condition.getConditionTriple().getPredicate(),
					previousConditions);

			if (earlierCondition != null) {

				int indexOf = previousConditions.indexOf(earlierCondition);
				BetaMemory memory = betaMemories.get(indexOf);

				JoinNodeTest test = new JoinNodeTest(RDFTriple.Field.PREDICATE,
						getVariableField((RDFVariable) condition
								.getConditionTriple().getSubject(),
								earlierCondition), memory);

				tests.add(test);
			}
		}

		// Test whether variable occurs at object
		if (condition.isVariableAtObject()) {

			TripleCondition earlierCondition = variableOccurs(
					(RDFVariable) condition.getConditionTriple().getObject(),
					previousConditions);

			if (earlierCondition != null) {

				int indexOf = previousConditions.indexOf(earlierCondition);
				BetaMemory memory = betaMemories.get(indexOf);

				JoinNodeTest test = new JoinNodeTest(RDFTriple.Field.OBJECT,
						getVariableField((RDFVariable) condition
								.getConditionTriple().getSubject(),
								earlierCondition), memory);

				tests.add(test);
			}
		}

		return tests;
	}

	private AlphaMemory buildOrShareAlphaMemory(TripleCondition condition) {

		AlphaMemory alphaMemory = null;

		// Top node of alpha memory network
		AlphaNode currentNode = workingMemory.getRootNode();

		// Add/share a predicate constant test node
		TripleConstantTest constantTest = condition.getPredicateConstantTest();
		if (constantTest != null) {
			currentNode = buildOrShareConstantTestNode(currentNode,
					constantTest.getTestField(),
					constantTest.getLexicalTestSymbol());

			if (currentNode.getOutputMemory() != null)
				alphaMemory = currentNode.getOutputMemory();
		}

		// Add/share the object constant test node
		constantTest = condition.getObjectConstantTest();
		if (constantTest != null) {
			currentNode = buildOrShareConstantTestNode(currentNode,
					constantTest.getTestField(),
					constantTest.getLexicalTestSymbol());

			if (currentNode.getOutputMemory() != null)
				alphaMemory = currentNode.getOutputMemory();
		}

		// Add/share the subject constant test node
		constantTest = condition.getSubjectConstantTest();
		if (constantTest != null) {
			currentNode = buildOrShareConstantTestNode(currentNode,
					constantTest.getTestField(),
					constantTest.getLexicalTestSymbol());

			if (currentNode.getOutputMemory() != null)
				alphaMemory = currentNode.getOutputMemory();

		}

		// TODO Not all alpha nodes should have memory, only those which are
		// connectives to beta memory
		if (currentNode.getOutputMemory() == null) {
			alphaMemory = new AlphaMemory();
			currentNode.setOutputMemory(alphaMemory);
			workingMemory.addAlphaMemory(alphaMemory);
		}

		return alphaMemory;
	}

	private AlphaNode buildOrShareConstantTestNode(AlphaNode currentNode,
			RDFTriple.Field field, String lexicalTestSymbol) {

		AlphaNode returnNode = null;

		// Check if the constant value test already exists and return it
		for (AlphaNode childNode : currentNode.getChildren()) {

			if (((childNode instanceof ConstantSubjectTestAlphaNode) && (field
					.equals(RDFTriple.Field.SUBJECT)))
					|| ((childNode instanceof ConstantPredicateTestAlphaNode) && (field
							.equals(RDFTriple.Field.PREDICATE)))
					|| ((childNode instanceof ConstantObjectTestAlphaNode) && (field
							.equals(RDFTriple.Field.OBJECT))))
				if (((ValueTestAlphaNode) childNode).getTestValue().equals(
						lexicalTestSymbol))
					return childNode;
		}

		if (field.equals(RDFTriple.Field.SUBJECT))
			returnNode = new ConstantSubjectTestAlphaNode(lexicalTestSymbol);
		else if (field.equals(RDFTriple.Field.PREDICATE))
			returnNode = new ConstantPredicateTestAlphaNode(lexicalTestSymbol);
		else if (field.equals(RDFTriple.Field.OBJECT))
			returnNode = new ConstantObjectTestAlphaNode(lexicalTestSymbol);

		currentNode.addChild(returnNode);

		return returnNode;
	}

	private TripleCondition variableOccurs(RDFVariable variable,
			List<TripleCondition> conditions) {

		TripleCondition variableOccursTripleCondition = null;

		// Assuming that the conditions are ordered in the way they are ordered
		// in the rule
		// we are going backwards so to find the latest occurrence of the
		// variable
		TripleCondition condition = null;

		for (int i = conditions.size() - 1; i >= 0; i--) {

			condition = conditions.get(i);

			if (condition.getConditionTriple().getSubject() instanceof RDFVariable)
				if (variable.equals((RDFVariable) condition
						.getConditionTriple().getSubject())) {
					variableOccursTripleCondition = condition;
					break;
				}

			if (condition.getConditionTriple().getPredicate() instanceof RDFVariable)
				if (variable.equals((RDFVariable) condition
						.getConditionTriple().getPredicate())) {
					variableOccursTripleCondition = condition;
					break;
				}

			if (condition.getConditionTriple().getObject() instanceof RDFVariable)
				if (variable.equals((RDFVariable) condition
						.getConditionTriple().getObject())) {
					variableOccursTripleCondition = condition;
					break;
				}
		}

		return variableOccursTripleCondition;
	}

	private RDFTriple.Field getVariableField(RDFVariable variable,
			TripleCondition condition) {

		RDFTriple.Field field = null;

		if (condition.getConditionTriple().getSubject() instanceof RDFVariable)
			if (variable.equals((RDFVariable) condition.getConditionTriple()
					.getSubject())) {
				field = RDFTriple.Field.SUBJECT;
			}

		if (condition.getConditionTriple().getPredicate() instanceof RDFVariable)
			if (variable.equals((RDFVariable) condition.getConditionTriple()
					.getPredicate())) {
				field = RDFTriple.Field.PREDICATE;
			}

		if (condition.getConditionTriple().getObject() instanceof RDFVariable)
			if (variable.equals((RDFVariable) condition.getConditionTriple()
					.getObject())) {
				field = RDFTriple.Field.OBJECT;
			}

		return field;
	}

	/**
	 * This method adds a triple pattern graph
	 * 
	 * @param triplePatternGraph
	 */
	public void addTriplePatternGraph(TriplePatternGraph triplePatternGraph) {

		RETENode currentNode = betaMemory;

		List<TripleCondition> previousConditions = new ArrayList<TripleCondition>();
		List<TripleCondition> tripleConditions = triplePatternGraph
				.getSelectConditions();

		List<BetaMemory> betaMemories = new ArrayList<BetaMemory>();

		// TODO Check whether there is anything to add, i.e., whether the list
		// is empty or not

		List<JoinNodeTest> tests = getTestsFromCondition(
				tripleConditions.get(0), previousConditions, betaMemories);
		AlphaMemory alphaMemory = buildOrShareAlphaMemory(tripleConditions
				.get(0));
		alphaMemory.activateIndexesForTests(tests);
		alphaMemory.getIndexStructure().setWindowInMillis(
				triplePatternGraph.getTimeWindowLength());
		currentNode = buildOrShareJoinNode(currentNode, alphaMemory, tests,
				tripleConditions.get(0),
				triplePatternGraph.getTimeWindowLength());

		for (int i = 1; i < tripleConditions.size(); i++) {

			// beta memory
			currentNode = buildOrShareBetaMemoryNode(currentNode);
			betaMemories.add((BetaMemory) currentNode);

			// get next join node tests
			previousConditions.add(tripleConditions.get(i - 1));
			tests = getTestsFromCondition(tripleConditions.get(i),previousConditions, betaMemories);

			// activate beta memory indexes for each test
			for(JoinNodeTest test :tests){
				test.getBetaMemory().activateIndexesForTests(test);
			}
			((BetaMemory) currentNode).getIndexStructure().setWindowInMillis(triplePatternGraph.getTimeWindowLength());

			// alpha memory
			alphaMemory = buildOrShareAlphaMemory(tripleConditions.get(i));

			// activate alpha memory indexes
			alphaMemory.activateIndexesForTests(tests);
			alphaMemory.getIndexStructure().setWindowInMillis(triplePatternGraph.getTimeWindowLength());

			// join node
			currentNode = buildOrShareJoinNode(currentNode, alphaMemory, tests,
					tripleConditions.get(i),
					triplePatternGraph.getTimeWindowLength());
		}

		// Build a new production node, make it a child of current node
		ProductionNode productionNode = new ProductionNode();
		currentNode.addChild(productionNode);
		productionNode.setParent(currentNode);

		// Update the new node with the matches above
		productionNode.update();

		triplePatternGraphs.add(triplePatternGraph);
	}

	// public void addProduction(List <TripleCondition> conditions){
	//
	// RETENode currentNode = null;
	//
	// currentNode = buildOrShareNetworkForConditions(betaMemory, conditions,
	// new ArrayList <TripleCondition> ());
	//
	// //TODO The problem with this one is in the issue of building first beta
	// node while not marking it root
	// //Build a new production node, make it a child of current node
	// ProductionNode productionNode = new ProductionNode();
	// currentNode.addChild(productionNode);
	// productionNode.setParent(currentNode);
	//
	// //Update the new node with the matches above
	// productionNode.update();
	//
	// }

	// public RETENode buildOrShareNetworkForConditions(RETENode parent, List
	// <TripleCondition> conditions, List <TripleCondition> earlierConditions){
	//
	// RETENode currentNode = parent;
	// List <TripleCondition> conditionsHigherUp = earlierConditions;
	// List <JoinNodeTest> tests = null;
	// AlphaMemory alphaMemory = null;
	//
	// for (TripleCondition condition : conditions){
	// currentNode = buildOrShareBetaMemoryNode(currentNode);
	// tests = getTestsFromCondition(condition, conditionsHigherUp);
	// alphaMemory = buildOrShareAlphaMemory(condition);
	// currentNode = buildOrShareJoinNode(currentNode, alphaMemory, tests);
	// conditionsHigherUp.add(condition);
	// }
	//
	// return currentNode;
	// }

	public void printNetworkStructure() {

		// Logging out the network structure
		logger.debug("================= NETWORK STRUCTURE - START ================= ");

		logger.debug("******************* ALPHA Memory Structure *******************");

		printAlphaNode(workingMemory.getRootNode(), "  ");

		logger.debug("******************* ALPHA Memory Structure *******************");

		logger.debug("******************* BETA Memory Structure *******************");

		printBetaNode(betaMemory, "  ");

		logger.debug("******************* BETA Memory Structure *******************");

		logger.debug("================= NETWORK STRUCTURE - END   ================= ");

	}

	public void printAlphaNode(AlphaNode alphaNode, String prefix) {

		logger.debug(prefix + "Field to test:"
				+ (alphaNode.getClass().getName()));
		if (alphaNode instanceof ValueTestAlphaNode)
			logger.debug(prefix + "Value to test:"
					+ ((ValueTestAlphaNode) alphaNode).getTestValue());
		if (alphaNode.getOutputMemory() != null)
			logger.debug(prefix
					+ "Alpha mem id:"
					+ Integer.toHexString(alphaNode.getOutputMemory()
							.hashCode()));
		logger.debug(prefix + "---");

		for (AlphaNode childNode : alphaNode.getChildren())
			printAlphaNode(childNode, prefix + "  ");
	}

	public void printBetaNode(RETENode reteNode, String prefix) {

		StringBuffer buffer = new StringBuffer();

		buffer.append(prefix);
		buffer.append("Type: ");
		if (reteNode instanceof JoinNode) {
			buffer.append("JoinNode\n");
			buffer.append(prefix);
			buffer.append("Alpha mem id:"
					+ Integer.toHexString((((JoinNode) reteNode)
							.getAlphaMemory()).hashCode()) + '\n');
			for (JoinNodeTest test : ((JoinNode) reteNode).getTests()) {
				buffer.append(prefix);
				buffer.append("[ARG1Field]:" + test.getArg1Field()
						+ " [ARG2Field]:" + test.getArg2Field()
						+ " [Index]:" + test.getIndexStructure()
						+ '\n');
			}

		} else if (reteNode instanceof BetaMemory) {
			if (((BetaMemory) reteNode).isRootNode())
				buffer.append("RootNode");
			else
				buffer.append("BetaMemory");
		} else if (reteNode instanceof ProductionNode) {
			buffer.append("ProductionNode");
		}

		logger.debug(buffer.toString());
		logger.debug(prefix + "---");

		for (RETENode childNode : reteNode.getChildren())
			printBetaNode(childNode, prefix + "  ");
	}

	public WorkingMemory getWorkingMemory() {
		return workingMemory;
	}

	public BetaMemory getBetaMemory() {
		return betaMemory;
	}

	public long getNumMatches() {
		return getProductionNodeMatches(betaMemory);
	}

	private long getProductionNodeMatches(RETENode reteNode) {

		long matchCounterValue = -1;

		if (reteNode instanceof ProductionNode)
			return ((ProductionNode) reteNode).getMatchCounterValue();

		for (RETENode childReteNode : reteNode.getChildren()) {
			matchCounterValue = getProductionNodeMatches(childReteNode);
			if (matchCounterValue != -1)
				return matchCounterValue;

		}

		return matchCounterValue;
	}

//	public String getBetaMemoryLevels() {
//		StringBuffer bufferLevels = new StringBuffer();
//		bufferLevels.append("BM MEM ALLOC ");
//		getBetaMemoryLevel(betaMemory, bufferLevels);
//		return bufferLevels.toString();
//	}

//	private void getBetaMemoryLevel(RETENode node, StringBuffer buffer) {
//
//		if (node instanceof BetaMemory) {
//			buffer.append(((BetaMemory) node).getItems().size() + " ");
//		} else if (node instanceof ProductionNode) {
//			buffer.append(((ProductionNode) node).getItems().size() + " ");
//		}
//
//		for (RETENode childReteNode : node.getChildren())
//			getBetaMemoryLevel(childReteNode, buffer);
//	}

	/**
	 * Extracts all production nodes from the network
	 * 
	 * @return
	 */
	public List<ProductionNode> getProductionNodes() {
		List<ProductionNode> productionNodes = new ArrayList<ProductionNode>();
		addProductionNode(betaMemory, productionNodes);
		return productionNodes;
	}

	private void addProductionNode(RETENode node,
			List<ProductionNode> productionNodes) {
		if (node instanceof ProductionNode)
			productionNodes.add((ProductionNode) node);
		for (RETENode childReteNode : node.getChildren())
			addProductionNode(childReteNode, productionNodes);
	}
}
