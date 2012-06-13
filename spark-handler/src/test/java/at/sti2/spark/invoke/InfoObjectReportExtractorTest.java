package at.sti2.spark.invoke;

import at.sti2.spark.handler.ImpactoriumHandler;
import junit.framework.TestCase;

public class InfoObjectReportExtractorTest extends TestCase {

	public void testReportIdExtraction(){
		
		String httpResponse = "<info-object name=\"Report 4711\" id=\"4711\" created=\"2012-02-28T11:34:18.531+01:00\"  updated=\"2012-02-28T11:34:18.937+01:00\"  credibility=\"\"/>";
		ImpactoriumHandler invoker = new ImpactoriumHandler();
		String reportId = invoker.extractInfoObjectIdentifier(httpResponse);
		assertEquals("4711", reportId);
	}
}
