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
package at.sti2.spark.epsilon.network;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Network {

	static Logger logger = LoggerFactory.getLogger(Network.class);
	
	private List <ClassNode> classNodes = null;
	private List <PropertyNode> propertyNodes = null;
	
	public Network(){
		classNodes = new ArrayList<ClassNode>();
		propertyNodes = new ArrayList<PropertyNode>();
	}
	
	public void addPropertyNode(String propertyURI){
		
		logger.debug("Adding a property node [" + propertyURI + "].");
		
		PropertyNode propertyNode = new PropertyNode();
		propertyNode.setUri(propertyURI);
		propertyNodes.add(propertyNode);
	}
	
	public void addClassNode(String classURI){
		
		logger.debug("Adding a class node [" + classURI + "].");
		
		ClassNode classNode = new ClassNode();
		classNode.setUri(classURI);
		classNodes.add(classNode);
	}
	
	public void addSubClassLink(String superclassURI, String subclassURI){
		
		logger.debug("Adding a subclass link between [" + superclassURI + ", " + subclassURI + "].");
		
		ClassNode superClassNode = getClassNodeByURI(superclassURI);
		ClassNode subClassNode = getClassNodeByURI(subclassURI);
		subClassNode.addSLinkNode(superClassNode);
	}
	
	public void addDomainLink(String propertyURI, String domainClassURI){
		
		logger.debug("Adding a link between domain [" + domainClassURI + "] and property [" + propertyURI +  "].");
		
		PropertyNode propertyNode = getPropertyNodeByURI(propertyURI);
		ClassNode domainClassNode = getClassNodeByURI(domainClassURI);
		propertyNode.addSLinkNode(domainClassNode);
	}
	
	public void addRangeLink(String propertyURI, String rangeClassURI){
		
		logger.debug("Adding a link between range [" + rangeClassURI + "] and property [" + propertyURI +  "].");
		
		PropertyNode propertyNode = getPropertyNodeByURI(propertyURI);
		ClassNode rangeClassNode = getClassNodeByURI(rangeClassURI);
		propertyNode.addOLinkNode(rangeClassNode);
	}
	
	public void addSubPropertyLink(String superpropertyURI, String subpropertyURI){
		
		logger.debug("Adding a subproperty link between [" + superpropertyURI + ", " + subpropertyURI + "].");
		
		PropertyNode superPropertyNode = getPropertyNodeByURI(superpropertyURI);
		PropertyNode subPropertyNode = getPropertyNodeByURI(subpropertyURI);
		subPropertyNode.addSOLinkNode(superPropertyNode);
	}
	
	public void addInverseLink(String subjectPropertyURI, String objectPropertyURI){
		
		logger.debug("Adding an inverse property link between [" + subjectPropertyURI + ", " + objectPropertyURI + "].");
		
		PropertyNode subjectPropertyNode = getPropertyNodeByURI(subjectPropertyURI);
		
		logger.debug("Subject property node:" + subjectPropertyNode.getUri());
		
		PropertyNode objectPropertyNode = getPropertyNodeByURI(objectPropertyURI);
		
		logger.debug("Object property node:" + objectPropertyNode.getUri());
		
		subjectPropertyNode.addOSLinkNode(objectPropertyNode);
		objectPropertyNode.addOSLinkNode(subjectPropertyNode);
	}
	
	public void addSymmetricLink(String symmetricPropertyURI){
		
		logger.debug("Adding a symetric link for [" + symmetricPropertyURI + "].");
		
		PropertyNode propertyNode = getPropertyNodeByURI(symmetricPropertyURI);
		propertyNode.addOSLinkNode(propertyNode);
	}

	public List<ClassNode> getClassNodes() {
		return classNodes;
	}
	
	public List<PropertyNode> getPropertyNodes() {
		return propertyNodes;
	}
	
	public ClassNode getClassNodeByURI(String uri){
		ClassNode classNode = null;
		for (ClassNode node : classNodes)
			if (node.getUri().equals(uri)){
				classNode = node;
				break;
			}
		return classNode;	
	}
	
	public PropertyNode getPropertyNodeByURI(String uri){
		PropertyNode propertyNode = null;
		for (PropertyNode node : propertyNodes)
			if (node.getUri().equals(uri)){
				propertyNode = node;
				break;
			}
		return propertyNode;	
	}
	
	public Node getNodeByURI(String uri){
		Node node = getClassNodeByURI(uri);
		if (node == null)
			node = getPropertyNodeByURI(uri);
		return node;
	}
	
	public String getEpsilonMemoryLevels(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("EM ");
		for (ClassNode classNode : classNodes)
			buffer.append(classNode.getTokens().size() + " ");
		buffer.append("| ");		
		for (PropertyNode propertyNode : propertyNodes)
			buffer.append(propertyNode.getTokens().size() + " ");
		return buffer.toString();
	}
}
