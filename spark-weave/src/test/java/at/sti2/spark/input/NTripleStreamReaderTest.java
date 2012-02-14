package at.sti2.spark.input;

import junit.framework.TestCase;

public class NTripleStreamReaderTest extends TestCase {

	public void testStreamReader(){
		
		NTripleStreamReader streamReader = new NTripleStreamReader("./resources/sparkweave_benchmark/offers.nt");
		streamReader.parseTriples();
		
		assertTrue(true);
	}
}
