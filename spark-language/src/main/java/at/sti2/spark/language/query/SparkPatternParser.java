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
import java.util.StringTokenizer;

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
		
		System.out.println("Parsing " + timeWindowLine);
		
		int openBracketIndex = timeWindowLine.indexOf('(');
		int closedBracketIndex = timeWindowLine.indexOf(')');
		return Long.parseLong(timeWindowLine.substring(openBracketIndex+1, closedBracketIndex));
	}
	
	private void parseTriplePattern(String triplePattern, TriplePatternGraph triplePatternGraph){
		
		System.out.println("Parsing " + triplePattern);
		
		String subject;
		String predicate;
		String object;
		
		StringTokenizer tokenizer = new StringTokenizer(triplePattern);
		
		subject = tokenizer.nextToken();
		predicate = tokenizer.nextToken();
		object = tokenizer.nextToken();
		
		RDFTriple triple = new RDFTriple();
		TripleCondition tripleCondition = new TripleCondition();
		tripleCondition.setConditionTriple(triple);
		
		//Take care of subject
		if (subject.startsWith("?"))
			triple.setSubject(new RDFVariable(subject));
		else {
			triple.setSubject(new RDFURIReference(subject));
			tripleCondition.addConstantTest(new TripleConstantTest(subject, RDFTriple.Field.SUBJECT));
		}
		
		//Take care of predicate
		if (predicate.startsWith("?"))
			triple.setPredicate(new RDFVariable(predicate));
		else {
			triple.setPredicate(new RDFURIReference(predicate));
			tripleCondition.addConstantTest(new TripleConstantTest(predicate, RDFTriple.Field.PREDICATE));
		}
		
		//Take care of object
		if (object.startsWith("?"))
			triple.setObject(new RDFVariable(object));
		else {
			triple.setObject(new RDFURIReference(object));
			tripleCondition.addConstantTest(new TripleConstantTest(object, RDFTriple.Field.OBJECT));
		}
		
		triplePatternGraph.addTripleCondition(tripleCondition);
	}
}