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

import at.sti2.spark.core.condition.TriplePatternGraph;
import junit.framework.TestCase;

public class TestSparkPatternParser extends TestCase {

//	public void testParserPT2TW100(){
//		
//		SparkPatternParser parser = new SparkPatternParser("./resources/pattern-PT2-TW100.tpg");
//		TriplePatternGraph patternGraph = parser.parse();
//		System.out.println(patternGraph.toString());
//		
//		assertTrue(true);
//	}
//	
//	public void testParserPT2TW250(){
//		
//		SparkPatternParser parser = new SparkPatternParser("./resources/pattern-PT2-TW250.tpg");
//		TriplePatternGraph patternGraph = parser.parse();
//		System.out.println(patternGraph.toString());
//		
//		assertTrue(true);
//	}
	
	public void testParserSupportPattern2(){
		
		SparkPatternParser parser = new SparkPatternParser("./resources/support_pattern2.tpg");
		TriplePatternGraph patternGraph = parser.parse();
		System.out.println(patternGraph.toString());
		
		assertTrue(true);
	}
}
