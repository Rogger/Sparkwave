package at.sti2.spark.rete.alpha;

import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.rete.WorkingMemoryElement;

public class ConstantPredicateTestAlphaNode extends ValueTestAlphaNode {

	public ConstantPredicateTestAlphaNode(String testValue) {
		super(testValue);
	}

	@Override
	public void testActivation(WorkingMemoryElement wme) {
		
		if (!((RDFURIReference)wme.getStreamedTriple().getTriple().getPredicate()).getValue().toString().equals(testValue))
			return;
		
		activate(wme);

	}
	
	public String toString(){
		return super.toString();
	}

}
