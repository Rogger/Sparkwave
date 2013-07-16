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
package at.sti2.spark.epsilon.network.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.sti2.spark.epsilon.network.ClassNode;
import at.sti2.spark.epsilon.network.PropertyNode;
import at.sti2.spark.epsilon.network.run.EpsilonNetwork;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * This class builds the epsilon network
 * 
 * @author skomazec
 * 
 */
public class NetworkBuilder {

	static Logger logger = LoggerFactory.getLogger(NetworkBuilder.class);
	
	private static String NS_OWL  = "http://www.w3.org/2002/07/owl#";
	private static String NS_XSD  = "http://www.w3.org/2001/XMLSchema#";
	private static String NS_RDF  = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private static String NS_RDFS = "http://www.w3.org/2000/01/rdf-schema#";
	
	private File file;

	public NetworkBuilder(File file) {
		this.file = file;
	}
	
	public EpsilonNetwork buildNetwork() {

		InputStream inStream;
		EpsilonNetwork epsilonNetwork = new EpsilonNetwork();
		
		try {
			
			if (file.exists()) {
			
				inStream = new FileInputStream(file);
				OntModel ontModelNoInf = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
				ontModelNoInf.read(inStream, null, "TURTLE");
	
				//Building network
				buildClassNodes(ontModelNoInf, epsilonNetwork);
				addSubClassLinks(ontModelNoInf, epsilonNetwork);
				buildPropertyNodes(ontModelNoInf, epsilonNetwork);
				addSubPropertyLinks(ontModelNoInf, epsilonNetwork);
				addDomainLinks(ontModelNoInf, epsilonNetwork);
				addRangeLinks(ontModelNoInf, epsilonNetwork);
				addInverseOfLinks(ontModelNoInf, epsilonNetwork);
				addSymmetricOfLinks(ontModelNoInf, epsilonNetwork);
			}
		    
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		}

		return epsilonNetwork;
	}
	
    private void addDomainLinks(OntModel model, EpsilonNetwork epsilonNetwork){
		
		int counter = 0;
		
		logger.info("Adding domain links...");
		
		for (PropertyNode propertyNode : epsilonNetwork.getNetwork().getPropertyNodes()){
				
				String queryStr = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
						          "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
	                              "SELECT ?domain { " +
		                          " <" + propertyNode.getUri()+ ">  rdfs:domain ?domain" +
	                              " }";
				
				Query query = QueryFactory.create(queryStr) ;
				QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
				
				try {
					ResultSet results = qexec.execSelect() ;
					while (results.hasNext()) {
						QuerySolution soln = results.nextSolution() ;        
						RDFNode domainNode = soln.get("domain") ;
						if (domainNode.asResource().getNameSpace().equals(NS_OWL) ||
							domainNode.asResource().getNameSpace().equals(NS_XSD) ||
							domainNode.asResource().getNameSpace().equals(NS_RDF) ||
							domainNode.asResource().getNameSpace().equals(NS_RDFS))
							continue;
						
						epsilonNetwork.getNetwork().addDomainLink(propertyNode.getUri(), domainNode.asResource().getURI());
						counter++;
						
						logger.debug("[" + counter + "] added domain link " + propertyNode.getUri() + "->" + domainNode.asResource().getURI());
					}
				} finally { 
					qexec.close() ;
				}
				
			}

		logger.info("Added " + counter + " domain links.");
	}
    
    private void addInverseOfLinks(OntModel model, EpsilonNetwork epsilonNetwork){
    	
    	int counter = 0;
		
		logger.info("Building inverseof links ...");

	    String queryStr = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
                		  "SELECT ?subjectProperty ?objectProperty { " +
		                  " ?subjectProperty owl:inverseOf ?objectProperty " +
                          " }";

		Query query = QueryFactory.create(queryStr) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model) ;

		try {
			ResultSet results = qexec.execSelect() ;
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution() ;        
				RDFNode subjectPropertyNode = soln.get("subjectProperty") ;
				RDFNode objectPropertyNode = soln.get("objectProperty") ;
				if (subjectPropertyNode.asResource().getNameSpace().equals(NS_OWL) ||
					subjectPropertyNode.asResource().getNameSpace().equals(NS_XSD) ||
					subjectPropertyNode.asResource().getNameSpace().equals(NS_RDF) ||
					subjectPropertyNode.asResource().getNameSpace().equals(NS_RDFS)||
					objectPropertyNode.asResource().getNameSpace().equals(NS_OWL) ||
					objectPropertyNode.asResource().getNameSpace().equals(NS_XSD) ||
					objectPropertyNode.asResource().getNameSpace().equals(NS_RDF) ||
					objectPropertyNode.asResource().getNameSpace().equals(NS_RDFS))
					continue;
				
				epsilonNetwork.getNetwork().addInverseLink(subjectPropertyNode.asResource().getURI(), objectPropertyNode.asResource().getURI());
				counter++;
				
				logger.debug("[" + counter + "] added inverse link between " + subjectPropertyNode.asResource().getURI() + "<->" + objectPropertyNode.asResource().getURI());
			}
		} finally { 
			qexec.close() ;
		}
		
		logger.info("Added " + counter + " inverse links.");
    	
    }
    
    private void addSymmetricOfLinks(OntModel model, EpsilonNetwork epsilonNetwork){
    	
    	int counter = 0;
		
		logger.info("Building symmetric links ...");

	    String queryStr = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
	    		          "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
	                      "SELECT ?subjectProperty { " +
	    		          " ?subjectProperty a owl:SymmetricProperty " +
	                      " }";

		Query query = QueryFactory.create(queryStr) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model) ;

		try {
			
			ResultSet results = qexec.execSelect() ;
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution() ;        
				RDFNode symmetricPropertyNode = soln.get("subjectProperty") ;
				if (symmetricPropertyNode.asResource().getNameSpace().equals(NS_OWL) ||
					symmetricPropertyNode.asResource().getNameSpace().equals(NS_XSD) ||
					symmetricPropertyNode.asResource().getNameSpace().equals(NS_RDF) ||
					symmetricPropertyNode.asResource().getNameSpace().equals(NS_RDFS))
					continue;
				
				epsilonNetwork.getNetwork().addSymmetricLink(symmetricPropertyNode.asResource().getURI());
				counter++;
				
				logger.debug("[" + counter + "] added symmetric link for " + symmetricPropertyNode.asResource().getURI());
			}
			
		} finally { 
			qexec.close() ;
		}
		
		logger.info("Added " + counter + " symmetric links.");
    	
    }
	
    private void addRangeLinks(OntModel model, EpsilonNetwork epsilonNetwork){
		
		int counter = 0;
		
		logger.info("Adding range links...");
		
		for (PropertyNode propertyNode : epsilonNetwork.getNetwork().getPropertyNodes()){
				
				String queryStr = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
						          "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
	                              "SELECT ?range { " +
		                          " <" + propertyNode.getUri()+ ">  rdfs:range ?range" +
	                              " }";
				
				Query query = QueryFactory.create(queryStr) ;
				QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
				
				try {
					ResultSet results = qexec.execSelect() ;
					while (results.hasNext()) {
						QuerySolution soln = results.nextSolution() ;        
						RDFNode rangeNode = soln.get("range") ;
						if (rangeNode.asResource().getNameSpace().equals(NS_OWL) ||
							rangeNode.asResource().getNameSpace().equals(NS_XSD) ||
							rangeNode.asResource().getNameSpace().equals(NS_RDF) ||
							rangeNode.asResource().getNameSpace().equals(NS_RDFS))
							continue;
						
						epsilonNetwork.getNetwork().addRangeLink(propertyNode.getUri(), rangeNode.asResource().getURI());
						counter++;
						
						logger.debug("[" + counter + "] added range link " + propertyNode.getUri() + "->" + rangeNode.asResource().getURI());
					}
				} finally { 
					qexec.close() ;
				}
				
			}

		logger.info("Added " + counter + " range links.");
		
	}
	
	private void buildPropertyNodes(OntModel model, EpsilonNetwork epsilonNetwork){
		
		int counter = 0;
		
		logger.info("Building property nodes ...");

		String queryStr = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
						  "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                          "SELECT ?property { " +
		                      " ?property a owl:ObjectProperty " +
                          " }";

		Query query = QueryFactory.create(queryStr) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model) ;

		try {
			ResultSet results = qexec.execSelect() ;
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution() ;        
				RDFNode propertyNode = soln.get("property") ;
				if (propertyNode.asResource().getNameSpace().equals(NS_OWL) ||
					propertyNode.asResource().getNameSpace().equals(NS_XSD) ||
					propertyNode.asResource().getNameSpace().equals(NS_RDF) ||
					propertyNode.asResource().getNameSpace().equals(NS_RDFS))
					continue;
				
				epsilonNetwork.getNetwork().addPropertyNode(propertyNode.asResource().getURI());
				counter++;
				
				//Adding property node to the node selector
				epsilonNetwork.getNodeSelector().put(propertyNode.asResource().getURI(), epsilonNetwork.getNetwork().getPropertyNodeByURI(propertyNode.asResource().getURI()));
				
				logger.debug("[" + counter + "] added property node " + propertyNode.asResource().getURI());
			}
		} finally { 
			qexec.close() ;
		}
		
		logger.info("Added " + counter + " property nodes.");
		
	}
	
	private void addSubClassLinks(OntModel model, EpsilonNetwork epsilonNetwork){
		
		int counter = 0;
		
		logger.info("Adding subclass links...");
		
		for (ClassNode classNode : epsilonNetwork.getNetwork().getClassNodes()){
			
			String queryStr = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
					          "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                              "SELECT ?subclass { " +
	                          " ?subclass rdfs:subClassOf <" + classNode.getUri()+ "> " +
                              " }";
			
			Query query = QueryFactory.create(queryStr) ;
			QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
			
			try {
				ResultSet results = qexec.execSelect() ;
				while (results.hasNext()) {
					QuerySolution soln = results.nextSolution() ;        
					RDFNode subclassNode = soln.get("subclass") ;
					if (subclassNode.asResource().getNameSpace().equals(NS_OWL) ||
						subclassNode.asResource().getNameSpace().equals(NS_XSD) ||
						subclassNode.asResource().getNameSpace().equals(NS_RDF) ||
						subclassNode.asResource().getNameSpace().equals(NS_RDFS))
						continue;
					
					epsilonNetwork.getNetwork().addSubClassLink(classNode.getUri(), subclassNode.asResource().getURI());
					counter++;
					
					logger.debug("[" + counter + "] added subclass link " + subclassNode.asResource().getURI() + "->" + classNode.getUri());
				}
				
			} finally { 
				qexec.close() ;
			}
			
		}

		logger.info("Added " + counter + " subclass links.");
		
	}
	
	private void addSubPropertyLinks(OntModel model, EpsilonNetwork epsilonNetwork){
		
		int counter = 0;
		
		logger.info("Adding subproperty links...");
		
		for (PropertyNode propertyNode : epsilonNetwork.getNetwork().getPropertyNodes()){
			
			String queryStr = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
					          "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                              "SELECT ?subproperty { " +
	                          " ?subproperty rdfs:subPropertyOf <" + propertyNode.getUri()+ "> " +
                              " }";
			
			Query query = QueryFactory.create(queryStr) ;
			QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
			
			try {
				ResultSet results = qexec.execSelect() ;
				while (results.hasNext()) {
					QuerySolution soln = results.nextSolution() ;        
					RDFNode subclassNode = soln.get("subproperty") ;
					if (subclassNode.asResource().getNameSpace().equals(NS_OWL) ||
						subclassNode.asResource().getNameSpace().equals(NS_XSD) ||
						subclassNode.asResource().getNameSpace().equals(NS_RDF) ||
						subclassNode.asResource().getNameSpace().equals(NS_RDFS))
						continue;
					
					epsilonNetwork.getNetwork().addSubPropertyLink(propertyNode.getUri(), subclassNode.asResource().getURI());
					counter++;
					
					logger.debug("[" + counter + "] added subproperty link " + subclassNode.asResource().getURI() + "->" + propertyNode.getUri());
				}
			} finally { 
				qexec.close() ;
			}
			
		}

		logger.info("Added " + counter + " subproperty links.");
		
	}
	
	
	private void buildClassNodes(OntModel model, EpsilonNetwork epsilonNetwork){
		
		int counter = 0;
		
		logger.info("Building class nodes...");

		String queryStr = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
						  "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                          "SELECT ?class { " +
		                      " ?class a owl:Class " +
                          " }";

		Query query = QueryFactory.create(queryStr) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model) ;

		try {
			ResultSet results = qexec.execSelect() ;
			
			while (results.hasNext()) {
				
				QuerySolution soln = results.nextSolution() ;
				
				RDFNode classNode = soln.get("class") ;
				
				if (classNode.asResource().getNameSpace().equals(NS_OWL) ||
					classNode.asResource().getNameSpace().equals(NS_XSD) ||
					classNode.asResource().getNameSpace().equals(NS_RDF) ||
					classNode.asResource().getNameSpace().equals(NS_RDFS))
					continue;
				
				epsilonNetwork.getNetwork().addClassNode(classNode.asResource().getURI());
				counter++;

				//Adding class node to the node selector
				epsilonNetwork.getNodeSelector().put(classNode.asResource().getURI(), epsilonNetwork.getNetwork().getClassNodeByURI(classNode.asResource().getURI()));
				
				logger.debug("[" + counter + "] added class node " + classNode.asResource().getURI());
			}
			
		} finally {
			qexec.close() ;
		}
		
		logger.info("Added " + counter + " class nodes.");
		
	}

}
