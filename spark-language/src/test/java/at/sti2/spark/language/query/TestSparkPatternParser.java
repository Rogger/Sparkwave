package at.sti2.spark.language.query;

import junit.framework.TestCase;

public class TestSparkPatternParser extends TestCase {

	public void testSparkPatternParser(){
		
		SparkPatternParser parser = new SparkPatternParser("./resources/test.tpg");
		parser.parse();
		assertTrue(true);
	}
}
