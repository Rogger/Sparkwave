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

import at.sti2.spark.core.triple.RDFLiteral;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.variable.RDFVariable;
import at.sti2.spark.rete.condition.TripleCondition;
import at.sti2.spark.rete.condition.TripleConstantTest;
import at.sti2.spark.rete.condition.TriplePatternGraph;

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
				String line;
				while ((line = in.readLine())!=null){
					if (line.startsWith("TIMEWINDOW")){
						long timewindow = parseTimewindow(line);
						triplePatternGraph.setTimeWindowLength(timewindow);
					}else
						parseTriplePattern(line, triplePatternGraph);					
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return triplePatternGraph;
	}
	
	private long parseTimewindow(String timeWindowLine){
		
		logger.info("Parsing " + timeWindowLine);
		
		int openBracketIndex = timeWindowLine.indexOf('(');
		int closedBracketIndex = timeWindowLine.indexOf(')');
		return Long.parseLong(timeWindowLine.substring(openBracketIndex+1, closedBracketIndex));
	}
	
	/**
	 * Method parsing triple patterns
	 * 
	 * NOTE: StringTokenizer cannot be used because objects may have literals which have blank spaces 
	 * 
	 * @param triplePattern
	 * @param triplePatternGraph
	 */
	private void parseTriplePattern(String triplePattern, TriplePatternGraph triplePatternGraph){
		
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
			
			//Copy URI value
			buffer = new StringBuffer();
			while (!Character.isWhitespace(tripleChars[currentPos])){
				buffer.append(tripleChars[currentPos]);
				currentPos++;
			}
			
			triple.setSubject(new RDFVariable(buffer.toString()));
			
		} else {
			
			//Copy URI value
			buffer = new StringBuffer();
			while (!Character.isWhitespace(tripleChars[currentPos])){
				buffer.append(tripleChars[currentPos]);
				currentPos++;
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
			
			//Copy URI value
			buffer = new StringBuffer();
			while (!Character.isWhitespace(tripleChars[currentPos])){
				buffer.append(tripleChars[currentPos]);
				currentPos++;
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
			
			//Copy URI value
			buffer = new StringBuffer();
			while ((currentPos!= tripleChars.length) && (!Character.isWhitespace(tripleChars[currentPos]))){
				buffer.append(tripleChars[currentPos]);
				currentPos++;
			}
			
			triple.setObject(new RDFURIReference(buffer.toString()));
			tripleCondition.addConstantTest(new TripleConstantTest(buffer.toString(), RDFTriple.Field.OBJECT));
		}
		
		triplePatternGraph.addTripleCondition(tripleCondition);
	}
}