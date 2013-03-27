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

package at.sti2.spark.rete.alpha;

import java.util.ArrayList;
import java.util.List;

import at.sti2.spark.rete.WorkingMemoryElement;

/**
 * Working Memory
 * 
 * @author srdkom
 */
public class WorkingMemory {

	private RootAlphaNode rootNode = null;
//	private List <WorkingMemoryElement> wmeList = null;
	private List <AlphaMemory> alphaMemories = null;
	
	public WorkingMemory(){
		
		rootNode = new RootAlphaNode();
		alphaMemories = new ArrayList <AlphaMemory> ();
//		wmeList = new ArrayList <WorkingMemoryElement> ();
		
	}
	
	public void addWorkingMemoryElement(WorkingMemoryElement wme){
//		wmeList.add(wme);
		rootNode.testActivation(wme);
	}

	public RootAlphaNode getRootNode() {
		return rootNode;
	}

	public void setRootNode(RootAlphaNode rootNode) {
		this.rootNode = rootNode;
	}
	
	public void addAlphaMemory(AlphaMemory alphaMemory){
		alphaMemories.add(alphaMemory);
	}
	
	public List <AlphaMemory> getAlphaMemories(){
		return alphaMemories;
	}
	
//	public List <WorkingMemoryElement> getWorkingMemoryElements(){
//		return wmeList;
//	}
	
	/**
	 * The method returns a list of property testing alpha nodes without rdf:type node 
	 * 
	 * Assumption: The property testing nodes are connected directly to the root alpha node
	 */
	public List<AlphaNode> getPropertyAlphaNodesWithoutRDFType(){

		String rdfTypeIdentifier = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		
		List<AlphaNode> propertyAlphaNodes = new ArrayList <AlphaNode> ();
		
		for (AlphaNode childNode : rootNode.getChildren())
			if ((childNode instanceof ConstantPredicateTestAlphaNode) &&
				!(((ConstantPredicateTestAlphaNode)childNode).getTestValue().toString().equals(rdfTypeIdentifier)))
				propertyAlphaNodes.add(childNode);
		
		return propertyAlphaNodes;
	}
	
	/**
	 * The method returns a list of alpha nodes which are first children of root node and they are not 
	 * property alpha nodes
	 * 
	 * Assumption: The nodes are built in the P,O,S order
	 */
	public List<AlphaNode> getRootChildrenAlphaNodesWithoutPropertyAN(){
		
		List<AlphaNode> alphaNodes = new ArrayList <AlphaNode> ();
		
		for (AlphaNode childNode : rootNode.getChildren())
			if (!(childNode instanceof ConstantPredicateTestAlphaNode))
				alphaNodes.add(childNode);
					
		return alphaNodes;
	}
	
	public AlphaNode getRDFTypeAlphaNode(){
		String rdfTypeIdentifier = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		
		for (AlphaNode childNode : rootNode.getChildren())
			if ((childNode instanceof ConstantPredicateTestAlphaNode) &&
				(((ConstantPredicateTestAlphaNode)childNode).getTestValue().toString().equals(rdfTypeIdentifier)))
					return childNode;
		return null;
	}
	
	/**
	 * The method returns a list of alpha nodes which are first children of rdf:type predicate node
	 * 
	 * Assumption: The nodes are built in the P,O,S order
	 */
	
	public List<AlphaNode> getObjectAlphaNodesOfRDFTypePredicateAlphaNode(){
		List<AlphaNode> alphaNodes = new ArrayList <AlphaNode> ();
		
		AlphaNode rdfTypeAlphaNode = getRDFTypeAlphaNode();
		if(rdfTypeAlphaNode!=null){
			
			for (AlphaNode childNode : rdfTypeAlphaNode.getChildren())
				
				if (childNode instanceof ConstantObjectTestAlphaNode)
					alphaNodes.add(childNode);			
		}
		
		
		return alphaNodes;
	}
	
	/**
	 * The method returns a list of alpha nodes which are first children of rdf:type node and they are not 
	 * object alpha nodes
	 * 
	 * Assumption: The nodes are built in the P,O,S order
	 */
	public List<AlphaNode> getRDFTypeChildrenAlphaNodesWithoutObjectAN(){
		
		List<AlphaNode> alphaNodes = new ArrayList <AlphaNode> ();
		
		AlphaNode rdfTypeAlphaNode = getRDFTypeAlphaNode();
		if(rdfTypeAlphaNode!=null){
			
			for (AlphaNode childNode : getRDFTypeAlphaNode().getChildren())
				
				if (!(childNode instanceof ConstantObjectTestAlphaNode))
					alphaNodes.add(childNode);
		}
		
					
		return alphaNodes;
	}
	
//	public String getAlphaMemoryLevels(){
//		StringBuffer bufferLevels = new StringBuffer();
//		bufferLevels.append("AM ");
//		for (AlphaMemory alphaMemory : alphaMemories){
//			bufferLevels.append((alphaMemory.getItemsSize()) + " ");
//		}
//		return bufferLevels.toString();
//	}
}
