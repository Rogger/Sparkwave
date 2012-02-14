package at.sti2.spark.epsilon.network;

import java.util.ArrayList;
import java.util.List;

public class Network {

	private List <ClassNode> classNodes = null;
	private List <PropertyNode> propertyNodes = null;
	
	public Network(){
		classNodes = new ArrayList<ClassNode>();
		propertyNodes = new ArrayList<PropertyNode>();
	}
	
	public void addPropertyNode(String propertyURI){
		PropertyNode propertyNode = new PropertyNode();
		propertyNode.setUri(propertyURI);
		propertyNodes.add(propertyNode);
	}
	
	public void addClassNode(String classURI){
		ClassNode classNode = new ClassNode();
		classNode.setUri(classURI);
		classNodes.add(classNode);
	}
	
	public void addSubClassLink(String superclassURI, String subclassURI){
		ClassNode superClassNode = getClassNodeByURI(superclassURI);
		ClassNode subClassNode = getClassNodeByURI(subclassURI);
		subClassNode.addSLinkNode(superClassNode);
	}
	
	public void addDomainLink(String propertyURI, String domainClassURI){
		PropertyNode propertyNode = getPropertyNodeByURI(propertyURI);
		ClassNode domainClassNode = getClassNodeByURI(domainClassURI);
		propertyNode.addSLinkNode(domainClassNode);
	}
	
	public void addRangeLink(String propertyURI, String rangeClassURI){
		PropertyNode propertyNode = getPropertyNodeByURI(propertyURI);
		ClassNode rangeClassNode = getClassNodeByURI(rangeClassURI);
		propertyNode.addOLinkNode(rangeClassNode);
	}
	
	public void addSubPropertyLink(String superpropertyURI, String subpropertyURI){
		PropertyNode superPropertyNode = getPropertyNodeByURI(superpropertyURI);
		PropertyNode subPropertyNode = getPropertyNodeByURI(subpropertyURI);
		subPropertyNode.addSOLinkNode(superPropertyNode);
	}
	
	public void addInverseLink(String subjectPropertyURI, String objectPropertyURI){
		PropertyNode subjectPropertyNode = getPropertyNodeByURI(subjectPropertyURI);
		PropertyNode objectPropertyNode = getPropertyNodeByURI(objectPropertyURI);
		subjectPropertyNode.addOSLinkNode(objectPropertyNode);
		objectPropertyNode.addOSLinkNode(subjectPropertyNode);
	}
	
	public void addSymmetricLink(String symmetricPropertyURI){
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
