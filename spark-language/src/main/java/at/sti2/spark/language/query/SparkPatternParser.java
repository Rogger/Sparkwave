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
package at.sti2.spark.language.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import at.sti2.spark.core.condition.TripleCondition;
import at.sti2.spark.core.condition.TripleConstantTest;
import at.sti2.spark.core.condition.TriplePatternGraph;
import at.sti2.spark.core.prefix.Prefix;
import at.sti2.spark.core.triple.RDFLiteral;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.variable.RDFVariable;

/**
 * The class for parsing Spark Patterns
 * 
 * @author skomazec
 *
 */
public class SparkPatternParser {
	
	static Logger logger = Logger.getLogger(SparkPatternParser.class);

	private String patternFilePath = null;
	
	public SparkPatternParser(String patternFilePath){
		this.patternFilePath = patternFilePath;
	}
	
	public TriplePatternGraph parse(){
		
		TriplePatternGraph triplePatternGraph = new TriplePatternGraph();
		
		File patternFile = new File(patternFilePath);

		if (patternFile.exists()){			
			 try {
				BufferedReader in = new BufferedReader(new FileReader(patternFile));
				String line = in.readLine();
				
				do {
					
					if (line.toLowerCase().startsWith("@prefix"))
						line = parsePrefixes(line, triplePatternGraph, in);
						
					if (line.startsWith("CONSTRUCT"))
						line = parseConstructGraphPattern(triplePatternGraph, in);
					
					if (line.startsWith("SELECT"))
						line = parseSelectGraphPattern(triplePatternGraph, in);
										
					if (line.startsWith("TIMEWINDOW")){
						long timewindow = parseTimewindow(line);
						triplePatternGraph.setTimeWindowLength(timewindow);
					}
				} while ((line = in.readLine())!=null);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return triplePatternGraph;
	}
	
	private String parsePrefixes(String line, TriplePatternGraph triplePatternGraph, BufferedReader in){
		String processingLine = line;
		try {
			triplePatternGraph.addPrefix(parsePrefix(processingLine));
			//Each line should be a prefix description until we run onto SELECT or CONSTRUCT
			while ((processingLine = in.readLine())!=null){
				if (processingLine.startsWith("SELECT") || processingLine.startsWith("CONSTRUCT"))
					break;
				triplePatternGraph.addPrefix(parsePrefix(processingLine));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return processingLine;
	}
	
	private String parseSelectGraphPattern(TriplePatternGraph triplePatternGraph, BufferedReader in){
		String line = null;
		try {
			//Each line should be a triple pattern until we run onto TIMEWINDOW
			while ((line = in.readLine())!=null){
				if (line.startsWith("TIMEWINDOW"))
					break;
				triplePatternGraph.addSelectTripleCondition(parseTriplePattern(line, triplePatternGraph));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return line;
	}
	
	private String parseConstructGraphPattern(TriplePatternGraph triplePatternGraph, BufferedReader in){
		String line = null;
		try {
			//Each line should be a triple pattern until we run onto SELECT
			while ((line = in.readLine())!=null){
				if (line.startsWith("SELECT"))
					break;
				triplePatternGraph.addConstructTripleCondition(parseTriplePattern(line, triplePatternGraph));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return line;
	}
	
	private long parseTimewindow(String timeWindowLine){
		
		logger.info("Parsing " + timeWindowLine);
		
		int openBracketIndex = timeWindowLine.indexOf('(');
		int closedBracketIndex = timeWindowLine.indexOf(')');
		return Long.parseLong(timeWindowLine.substring(openBracketIndex+1, closedBracketIndex));
	}
	
	private Prefix parsePrefix(String prefixLine){
		
		logger.info("Parsing " + prefixLine );
		
		char prefixChars[] = prefixLine.toCharArray();
		int currentPos = 0; 
		StringBuffer buffer;
		
		Prefix prefix = new Prefix();
		
		//Find the first character which is != white space
		while (Character.isWhitespace(prefixChars[currentPos]))
			currentPos++;
		
		//Search for the whitespace character after '@prefix'
		while (!Character.isWhitespace(prefixChars[currentPos]))
			currentPos++;
		
		//Find the first character which is != white space, which is beginning of the label description
		while (Character.isWhitespace(prefixChars[currentPos]))
			currentPos++;
		
		buffer = new StringBuffer();
		
		//Copy label value
		while (!(prefixChars[currentPos]==':')){
			buffer.append(prefixChars[currentPos]);
			currentPos++;
		}
		
		prefix.setLabel(buffer.toString());

		//Search for the beginning of  uri
		while (prefixChars[currentPos]!='<')
			currentPos++;
		
		//Move one character place beyond <
		currentPos++;
		
		buffer = new StringBuffer();
		while (prefixChars[currentPos]!='>'){
			buffer.append(prefixChars[currentPos]);
			currentPos++;
		}
		
		prefix.setNamespace(buffer.toString());
		
		return prefix;
	}
	
	/**
	 * Method parsing triple patterns
	 * 
	 * NOTE: StringTokenizer cannot be used because objects may have literals which have blank spaces 
	 * 
	 * @param triplePattern
	 * @param triplePatternGraph
	 */
	private TripleCondition parseTriplePattern(String triplePattern, TriplePatternGraph triplePatternGraph){
		
		logger.info("Parsing " + triplePattern);
		
		char tripleChars[] = triplePattern.toCharArray();
		int currentPos = 0; 
		
		RDFTriple triple = new RDFTriple();
		TripleCondition tripleCondition = new TripleCondition();
		tripleCondition.setConditionTriple(triple);
		
		StringBuffer buffer;
		
		//---------------------------------------------------------
		//    Parse subject pattern which may be URL or variable
		//---------------------------------------------------------
		
		//Find the first character which is != white space
		while (Character.isWhitespace(tripleChars[currentPos]))
			currentPos++;
		
		//Test to see if string is variable or URI
		if (tripleChars[currentPos]=='?'){
			
			//Copy variable name
			buffer = new StringBuffer();
			while (!Character.isWhitespace(tripleChars[currentPos])){
				buffer.append(tripleChars[currentPos]);
				currentPos++;
			}
			
			triple.setSubject(new RDFVariable(buffer.toString()));
			
		} else {
			
			buffer = new StringBuffer();
			
			//Copy URI value
			if (isFullURI(tripleChars, currentPos)){
				while (!Character.isWhitespace(tripleChars[currentPos])){
					buffer.append(tripleChars[currentPos]);
					currentPos++;
				}
			}else{
				//Extract the prefix label
				while (tripleChars[currentPos]!=':'){
					buffer.append(tripleChars[currentPos]);
					currentPos++;
				}
				
				//Fetch namespace
				String namespace = triplePatternGraph.getNamespaceByLabel(buffer.toString());
				
				buffer = new StringBuffer();
				buffer.append(namespace);
				
				//Move pointer on next character
				currentPos++;
				
				while (!Character.isWhitespace(tripleChars[currentPos])){
					buffer.append(tripleChars[currentPos]);
					currentPos++;
				}
			}
			
			
			triple.setSubject(new RDFURIReference(buffer.toString()));
			tripleCondition.addConstantTest(new TripleConstantTest(buffer.toString(), RDFTriple.Field.SUBJECT));
		}
		
		//--------------------------------------------------------------
		//    Parse predicate pattern which may be URL or variable
		//--------------------------------------------------------------		
		
		//Find the first character which is != white space
		while (Character.isWhitespace(tripleChars[currentPos]))
			currentPos++;

		//Test to see if string is variable or URI
		if (tripleChars[currentPos]=='?'){
			
			//Copy variable value
			buffer = new StringBuffer();
			while (!Character.isWhitespace(tripleChars[currentPos])){
				buffer.append(tripleChars[currentPos]);
				currentPos++;
			}
			
			triple.setPredicate(new RDFVariable(buffer.toString()));
			
		} else {
			
			buffer = new StringBuffer();
			
			//Copy URI value
			if (isFullURI(tripleChars, currentPos)){
				while (!Character.isWhitespace(tripleChars[currentPos])){
					buffer.append(tripleChars[currentPos]);
					currentPos++;
				}
			}else{
				//Extract the prefix label
				while (tripleChars[currentPos]!=':'){
					buffer.append(tripleChars[currentPos]);
					currentPos++;
				}
				
				//Fetch namespace
				String namespace = triplePatternGraph.getNamespaceByLabel(buffer.toString());
				
				buffer = new StringBuffer();
				buffer.append(namespace);
				
				//Move pointer on next character
				currentPos++;
				
				while (!Character.isWhitespace(tripleChars[currentPos])){
					buffer.append(tripleChars[currentPos]);
					currentPos++;
				}
			}
			
			triple.setPredicate(new RDFURIReference(buffer.toString()));
			tripleCondition.addConstantTest(new TripleConstantTest(buffer.toString(), RDFTriple.Field.PREDICATE));
		}		
		
		//--------------------------------------------------------------
		//    Parse object pattern which may be URL or variable
		//--------------------------------------------------------------
		
		//Find the first character which is != white space
		while (Character.isWhitespace(tripleChars[currentPos]))
			currentPos++;

		//Test to see if string is variable
		if (tripleChars[currentPos]=='?'){
			
			//Copy URI value
			buffer = new StringBuffer();
			while ((currentPos!= tripleChars.length) && (!Character.isWhitespace(tripleChars[currentPos]))){
				buffer.append(tripleChars[currentPos]);
				currentPos++;
			}
			
			triple.setObject(new RDFVariable(buffer.toString()));
		
		//Test to see if the string is literal
		} else if (tripleChars[currentPos]=='"'){
			
			String lexicalForm = null;
	    	String languageTag = null;
	    	RDFURIReference datatypeURI = null;
			
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
			
			triple.setObject(new RDFLiteral(lexicalForm, datatypeURI, languageTag));
			
		} else {
			
			buffer = new StringBuffer();
			
			//Copy URI value
			if (isFullURI(tripleChars, currentPos)){
				while (!Character.isWhitespace(tripleChars[currentPos])){
					buffer.append(tripleChars[currentPos]);
					if (currentPos == tripleChars.length - 1)
						break;
					currentPos++;
				}
			}else{
				//Extract the prefix label
				while (tripleChars[currentPos]!=':'){
					buffer.append(tripleChars[currentPos]);
					currentPos++;
				}
				
				//Fetch namespace
				String namespace = triplePatternGraph.getNamespaceByLabel(buffer.toString());
				
				buffer = new StringBuffer();
				buffer.append(namespace);
				
				//Move pointer on next character
				currentPos++;
				
				while (!Character.isWhitespace(tripleChars[currentPos])){
					buffer.append(tripleChars[currentPos]);
					if (currentPos == tripleChars.length - 1)
						break;
					currentPos++;
				}
			}
			
			triple.setObject(new RDFURIReference(buffer.toString()));
			tripleCondition.addConstantTest(new TripleConstantTest(buffer.toString(), RDFTriple.Field.OBJECT));
		}
		
		return tripleCondition;
	}
	
	private boolean isFullURI(char tripleChars[], int currentPos){
		StringBuffer buffer = new StringBuffer();
		
		//Find the first character which is != white space
		while (!Character.isWhitespace(tripleChars[currentPos])){
			buffer.append(tripleChars[currentPos]);
			if (currentPos == tripleChars.length - 1)
				break;
			currentPos++;
		}
		
		if (buffer.toString().contains("://"))
			return true;
		else
			return false;
	}
}