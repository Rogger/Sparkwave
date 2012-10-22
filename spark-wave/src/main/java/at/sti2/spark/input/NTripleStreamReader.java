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
package at.sti2.spark.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import at.sti2.spark.core.triple.RDFLiteral;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.RDFValue;

public class NTripleStreamReader {
	
	static Logger logger = Logger.getLogger(NTripleStreamReader.class);

	private File inputFile = null;
	
	private List <RDFTriple> triples = new ArrayList <RDFTriple> ();
	
	private BufferedReader reader = null;
	
	public NTripleStreamReader(File inputFile){
		this.inputFile = inputFile;
	}
	
	public void parseTriples(){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			
			String tripleLine = null;
			
			while((tripleLine = reader.readLine()) != null)
				triples.add(parseTriple(tripleLine));
				
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void openFile(){
		try {
			reader = new BufferedReader(new FileReader(inputFile));
		} catch (FileNotFoundException e) {
			logger.debug("Triple file not found.");
			e.printStackTrace();
		}
	}
	
	public void closeFile(){
		try {
			reader.close();
		} catch (IOException e) {
			logger.debug("Problem while closing triple file.");
			e.printStackTrace();
		}
	}
	
	public RDFTriple nextTriple(){
		String tripleLine = null;
		RDFTriple rdfTriple = null;
		
		try {
			if ((tripleLine = reader.readLine()) != null)
				rdfTriple = parseTriple(tripleLine);
			
		} catch (IOException e) {
			logger.debug("Problem while reading triple file.");
			e.printStackTrace();
		}
		
		return rdfTriple;
	}
	
	public String nextTripleLine(){
		String tripleLine = null;
		
		try {
			if ((tripleLine = reader.readLine()) != null)
				return tripleLine;
			
		} catch (IOException e) {
			logger.debug("Problem while reading triple file.");
			e.printStackTrace();
		}
		
		return tripleLine;
	}
	
	/**
	 * Method to parse triple in N-Triple format.
	 * 
	 * Note: StringTokenizer is not really helpful because lexical form in literals can have blank spaces. 
	 * 
	 * @param tripleLine
	 * @return
	 */
	public RDFTriple parseTriple(String tripleLine){
		
		char tripleChars[] = tripleLine.toCharArray();
		int currentPos = 0; 
		
		//----------------------------------------------
		//Parse subject RDF node
		//----------------------------------------------
		while (tripleChars[currentPos]!='<')
			currentPos++;
		
		//Move one place beyond '<'
		currentPos++;
		
		//Copy URI value
		StringBuffer buffer = new StringBuffer();
		while (tripleChars[currentPos]!='>'){
			buffer.append(tripleChars[currentPos]);
			currentPos++;
		}
		RDFURIReference tripSubject = new RDFURIReference(buffer.toString());
		
		//----------------------------------------------
		//Parse predicate RDF node
		//----------------------------------------------		
		while (tripleChars[currentPos]!='<')
			currentPos++;
		
		//Move one place beyond '<'
		currentPos++;
		buffer = new StringBuffer();
		while (tripleChars[currentPos]!='>'){
			buffer.append(tripleChars[currentPos]);
			currentPos++;
		}
		RDFURIReference tripPredicate = new RDFURIReference(buffer.toString());
		
		//----------------------------------------------
		//Parse object RDF node
		//----------------------------------------------		
		//Move one place beyond '>'
		currentPos++;
		RDFValue tripObject = null;
		String lexicalForm = null;
    	String languageTag = null;
    	RDFURIReference datatypeURI = null;
		
		//Search while character under scope is != ' '
		while ((tripleChars[currentPos]!='<') && (tripleChars[currentPos]!='"'))
			currentPos++;
		
		//The character indicates literal value
		if (tripleChars[currentPos]=='"'){
		
			//Move one character place beyond "
			currentPos++;
			
			buffer = new StringBuffer();
			while (tripleChars[currentPos]!='"'){
				buffer.append(tripleChars[currentPos]);
				currentPos++;
			}
			lexicalForm = buffer.toString();
			
			//Search for the beginning of datatype uri
			while (tripleChars[currentPos]!='<')
				currentPos++;
			
			//Move one character place beyond <
			currentPos++;
			
			buffer = new StringBuffer();
			while (tripleChars[currentPos]!='>'){
				buffer.append(tripleChars[currentPos]);
				currentPos++;
			}
			
			datatypeURI = new RDFURIReference(buffer.toString());
			
			tripObject = RDFLiteral.Factory.createLiteral(lexicalForm, datatypeURI, languageTag);
		//The character is '<' and we have another URL
		}else{
			//Move one place beyond '<'
			currentPos++;
			buffer = new StringBuffer();
			while (tripleChars[currentPos]!='>'){
				buffer.append(tripleChars[currentPos]);
				currentPos++;
			}
			tripObject = new RDFURIReference(buffer.toString());
		}
			
		return new RDFTriple(tripSubject, tripPredicate, tripObject);
	}

	public List<RDFTriple> getTriples() {
		return triples;
	}
}
