package at.sti2.spark.epsilon.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class Network {

	static Logger logger = Logger.getLogger(Network.class);
	
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
		PropertyNode objectPropertyNode = getPropertyNodeByURI(objectPropertyURI);
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
