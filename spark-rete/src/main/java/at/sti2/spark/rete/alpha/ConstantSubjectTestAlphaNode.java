package at.sti2.spark.rete.alpha;

import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.rete.WorkingMemoryElement;

public class ConstantSubjectTestAlphaNode extends ValueTestAlphaNode {

	public ConstantSubjectTestAlphaNode(String testValue) {
		super(testValue);
	}

	public void testActivation(WorkingMemoryElement wme) {
		
		if (!((RDFURIReference)wme.getStreamedTriple().getTriple().getSubject()).getValue().toString().equals(testValue))
			return;
		
		activate(wme);
	}

}
