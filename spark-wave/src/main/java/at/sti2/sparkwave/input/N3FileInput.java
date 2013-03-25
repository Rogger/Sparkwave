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

package at.sti2.sparkwave.input;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import at.sti2.spark.core.triple.RDFLiteral;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.RDFValue;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

/**
 * This class reads and parses an RDF file in N3 notation
 * 
 * @author srdkom
 */
public class N3FileInput {

	private String fileName = null;
	
	private List <RDFTriple> triples = new ArrayList <RDFTriple> ();
	
	public N3FileInput(String fileName){
		this.fileName = fileName;
	}
	
	public void parseTriples(){
		
		Model model = ModelFactory.createDefaultModel();

		InputStream in = FileManager.get().open(fileName);

		if (in == null) 
		    throw new IllegalArgumentException("File: " + fileName + " not found");
		
		RDFReader reader = model.getReader("N-TRIPLES");
		reader.read(model, in, null);
		
		//List the statements in the Model
		StmtIterator iter = model.listStatements();
		
		while (iter.hasNext()) {
			
		    Statement stmt      = iter.nextStatement();  // get next statement
		    Resource  subject   = stmt.getSubject();     // get the subject
		    Property  predicate = stmt.getPredicate();   // get the predicate
		    RDFNode   object    = stmt.getObject();      // get the object
		    
		    //Create subject - can be URI or blank node
		    RDFURIReference tripSubject = null;
		    //If it is URI
		    if (subject.getURI()!=null){
		    	tripSubject = RDFURIReference.Factory.createURIReference(subject.getURI());
		    }
		    
		    //Create predicate - must be URI
		    RDFURIReference tripPredicate = RDFURIReference.Factory.createURIReference(predicate.getURI());
		    
		    // Create object - can be URI, blank node or literal
		    
		    RDFValue tripObject = null;
		    
		    if (object.isLiteral()){
		    	
		    	String lexicalForm = null;
		    	String languageTag = null;
		    	RDFURIReference datatypeURI = null;
		    	
		    	//Extract language tag
		    	if (!object.asLiteral().getLanguage().equals(""))
		    		languageTag = object.asLiteral().getLanguage();
		    	
		    	//Extract lexical form
		    	lexicalForm = object.asLiteral().getLexicalForm();
		    	
		    	//Extract datatypeURI
		    	if (object.asLiteral().getDatatypeURI() != null)
		    		datatypeURI = RDFURIReference.Factory.createURIReference(object.asLiteral().getDatatypeURI());
		    	
		    	tripObject = RDFLiteral.Factory.createLiteral(lexicalForm, datatypeURI, languageTag);
		    		
		    } else if (object.isResource()){
		    	if (object.asResource().getURI()!=null){
		    		tripObject = RDFURIReference.Factory.createURIReference(object.asResource().getURI());
			    }
		    }
		    
		    RDFTriple triple = new RDFTriple(tripSubject, tripPredicate, tripObject);
		    
		    triples.add(triple);
		}
	}

	public List<RDFTriple> getTriples() {
		return triples;
	}	
	
}
