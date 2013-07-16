package at.sti2.sparwave.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.sti2.spark.core.stream.Triple;
import at.sti2.sparkwave.parser.StreamParserThread;

public class StreamParserTest {
	
	static Logger logger = LoggerFactory.getLogger(StreamParserTest.class);

	@Test
	public void test() throws InterruptedException {
		
		String toParse = 
				"<http://www.foi.se/support/wp4demo#Observation_20110830_100005_373_PET_00001_1> <http://www.foi.se/support/wp4demo#has_status> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> . \n"+
				"<http://www.foi.se/support/wp4demo#Observation_20110830_100005_373_PET_00001_1> <http://www.foi.se/support/wp4demo#has_status> \"true\" . \n" +
				"<http://www.foi.se/support/wp4demo#Observation_20110830_100005_373_PET_00001_1> <http://www.foi.se/support/wp4demo#name> \"blub\" . \n";
		InputStream inputStream = IOUtils.toInputStream(toParse);
		List<BlockingQueue<Triple>> queues = new ArrayList<BlockingQueue<Triple>>();
		BlockingQueue<Triple> queue = new ArrayBlockingQueue<Triple>(10);
		queues.add(queue);
		
		StreamParserThread streamParserThread = new StreamParserThread(inputStream, queues);
		Thread t1  = new Thread(streamParserThread);
		t1.start();
		t1.join();
		
		Triple triple;
		int i = 0;
		while((triple=queue.poll()) != null){
			logger.info(triple.toString());
			i++;
		}
		
		logger.info("Parsed "+i+" triples!");
		
	}

}
