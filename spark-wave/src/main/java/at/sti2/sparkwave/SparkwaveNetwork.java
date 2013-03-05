/*
 * Copyright (c) 2012, University of Innsbruck, Austria.
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
package at.sti2.sparkwave;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.epsilon.network.ClassNode;
import at.sti2.spark.epsilon.network.PropertyNode;
import at.sti2.spark.epsilon.network.build.NetworkBuilder;
import at.sti2.spark.epsilon.network.run.EpsilonNetwork;
import at.sti2.spark.grammar.pattern.GroupGraphPattern;
import at.sti2.spark.grammar.pattern.Pattern;
import at.sti2.spark.rete.RETENetwork;
import at.sti2.spark.rete.alpha.AlphaNode;
import at.sti2.spark.rete.alpha.ValueTestAlphaNode;
import at.sti2.spark.rete.alpha.WorkingMemory;
import at.sti2.spark.rete.beta.ProductionNode;
import at.sti2.sparkwave.input.NTripleStreamReader;

public class SparkwaveNetwork{
	
	static Logger logger = Logger.getLogger(SparkwaveNetwork.class);
	
	private RETENetwork reteNetwork = null;
	private EpsilonNetwork epsilonNetwork = null;
	
	//Input artifacts
	private Pattern pattern = null;
	
	//Values needed for garbage collection
	private long lastTimestamp = 0l;
	
	public SparkwaveNetwork(Pattern pattern) {
		this.pattern = pattern;	
	}
	
	/**
	 * Kick off initialization of SparkwaveNetwork
	 * @param pattern
	 * @param epsilonOntology
	 */
	public void init(){
		
		String epsilonOntologyFileName = pattern.getEpsilonOntology();
		String staticInstancesFileName = pattern.getStaticInstances();
		
		File epsilonOntologyFile = new File(epsilonOntologyFileName);
		if(!epsilonOntologyFileName.equalsIgnoreCase("null") && !epsilonOntologyFile.exists() ){
			logger.warn("Epsilon ontology "+epsilonOntologyFileName+" not found!");
		}
		buildNetwork(epsilonOntologyFile);
		
		//Print Rete network structure
		//getReteNetwork().printNetworkStructure();
		
		//If there are static instances to be added
		File staticInstancesFile = new File(staticInstancesFileName);
		if (!staticInstancesFileName.equalsIgnoreCase("null") && !staticInstancesFile.exists()){
			logger.warn("Static instances "+staticInstancesFileName+" not found!");
		} else if (!staticInstancesFileName.equalsIgnoreCase("null")) {
			
			NTripleStreamReader streamReader = new NTripleStreamReader(staticInstancesFile);
			streamReader.openFile();
			
			
			String tripleLine = null;
			logger.info("Background knowledge network showering started...");
			long startProcessingTime = (new Date()).getTime();
			while ((tripleLine = streamReader.nextTripleLine()) != null){
				RDFTriple rdfTriple = streamReader.parseTriple(tripleLine);
				Triple sTriple = new Triple(rdfTriple, 0l, true, 0l);
				activateNetwork(sTriple);
			}
			long endProcessingTime = new Date().getTime();
			streamReader.closeFile();
			
			StringBuffer timeBuffer = new StringBuffer();
			timeBuffer.append("Showering took ");
			timeBuffer.append((endProcessingTime - startProcessingTime)/1000);
			timeBuffer.append(" s ");
			timeBuffer.append((endProcessingTime - startProcessingTime)%1000);
			timeBuffer.append(" ms.");
			
			logger.info(timeBuffer.toString());
		}
	}
		
	public void buildNetwork(File epsilonOntology){
		
		logger.info("Building RETE network...");
		
		//Build RETE network
		reteNetwork = new RETENetwork();
		reteNetwork.addTriplePatternGraph(pattern);
		
		logger.info("Building epsilon network...");
		
		//Build epsilon network
		NetworkBuilder builder = new NetworkBuilder(epsilonOntology);
		epsilonNetwork = builder.buildNetwork();
		
		logger.info("Binding epsilon and RETE network binding...");
		
		bindNetworks(epsilonOntology);
		
		logger.info("Sparkwave network completed...");
		
		//If there is a CONSTRUCT part start also the output thread 
		if (pattern.getConstruct().getConditions().size() > 0){
			List<ProductionNode> productionNodes = reteNetwork.getProductionNodes();
			for (ProductionNode productionNode : productionNodes){
				HandlerThread outputThread = new HandlerThread(pattern, productionNode.getOutputBuffer());
				outputThread.start();
			}
			
			logger.info("Sparkwave output thread started...");
		}
		
	}

	public RETENetwork getReteNetwork() {
		return reteNetwork;
	}

	public EpsilonNetwork getEpsilonNetwork() {
		return epsilonNetwork;
	}
	
	private void bindNetworks(File epsilonOntology){
		
		if (epsilonOntology.exists()){
		
			WorkingMemory workingMemory = reteNetwork.getWorkingMemory();
			
			/**
			 * Connecting epsilon property nodes to alpha predicate nodes 
			 */
			
			List<AlphaNode> predicateAlphaNodes = workingMemory.getPropertyAlphaNodesWithoutRDFType();
			logger.debug("Found predicate alpha nodes:" + predicateAlphaNodes);
			
			for (AlphaNode predicateAlphaNode : predicateAlphaNodes){
				logger.debug("Connecting predicate alpha node " + predicateAlphaNode + " to corresponding epsilon node.");
				PropertyNode propertyNode = epsilonNetwork.getNetwork().getPropertyNodeByURI(((ValueTestAlphaNode)predicateAlphaNode).getTestValue());
				//If the node exists in the epsilon network add binding
				if (propertyNode != null)
					propertyNode.addAlphaNode(predicateAlphaNode);
			}
			
			/**
			 * Connecting all epsilon property nodes to all first children alpha nodes which are not predicate nodes 
			 */
			List<AlphaNode> rootChildAlphaNodes = workingMemory.getRootChildrenAlphaNodesWithoutPropertyAN();
			logger.debug("Found property alpha nodes:" + rootChildAlphaNodes);
			if (!rootChildAlphaNodes.isEmpty())
				for (PropertyNode propertyNode : epsilonNetwork.getNetwork().getPropertyNodes())
					for (AlphaNode alphaNode : rootChildAlphaNodes)
						propertyNode.addAlphaNode(alphaNode);
							
			/**
			 * Connecting epsilon class nodes to alpha object nodes
			 */
			List<AlphaNode> objectAlphaNodes = workingMemory.getObjectAlphaNodesOfRDFTypePredicateAlphaNode();
			logger.debug("Found object alpha nodes:" + objectAlphaNodes);
			for (AlphaNode objectAlphaNode : objectAlphaNodes){
				logger.debug("Connecting object alpha node " + objectAlphaNode + " to corresponding epsilon class node.");
				ClassNode classNode = epsilonNetwork.getNetwork().getClassNodeByURI(((ValueTestAlphaNode)objectAlphaNode).getTestValue());
				//If the node exists in the epsilon network add binding
				if (classNode != null)
					classNode.addAlphaNode(objectAlphaNode);
			}
			
			/**
			 * Connecting all epsilon class nodes to all first children of alpha rdf:type nodes which are not object nodes 
			 */
			List<AlphaNode> rdfTypeChildAlphaNodes = workingMemory.getRDFTypeChildrenAlphaNodesWithoutObjectAN();
			logger.debug("Found subject alpha nodes:" + rdfTypeChildAlphaNodes);
			if (!rdfTypeChildAlphaNodes.isEmpty())
				for (ClassNode classNode : epsilonNetwork.getNetwork().getClassNodes())
					for (AlphaNode alphaNode : rdfTypeChildAlphaNodes)
						classNode.addAlphaNode(alphaNode);
							
			/**
			 * Connecting all epsilon class nodes to all first children alpha nodes which are not predicate nodes 
			 */
			rootChildAlphaNodes = workingMemory.getRootChildrenAlphaNodesWithoutPropertyAN();
			logger.debug("Found alpha nodes:" + rootChildAlphaNodes);
			if (!rootChildAlphaNodes.isEmpty())
				for (ClassNode classNode : epsilonNetwork.getNetwork().getClassNodes())
					for (AlphaNode alphaNode : rootChildAlphaNodes)
						classNode.addAlphaNode(alphaNode);
			}		
		/**
		 * Connect the epsilon entry to RETE entry for triples that are not entering RETE through epsilon
		 */
		epsilonNetwork.setRootAlphaNode(reteNetwork.getWorkingMemory().getRootNode());
	}

	public long getLastTimestamp() {
		return lastTimestamp;
	}

	public void setLastTimestamp(long lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}
	
	public long getTimeWindowLength(){
		//TODO THIS IS A HACK!!! Assumming that whereClause contains only GroupGraphPattern
		GroupGraphPattern whereClause = (GroupGraphPattern) pattern.getWhereClause();
		return whereClause.getTimeWindowLength();
	}
	
	public void activateNetwork(Triple streamedTriple){
		lastTimestamp = streamedTriple.getTimestamp();
		epsilonNetwork.activate(streamedTriple);
	}
	
}
