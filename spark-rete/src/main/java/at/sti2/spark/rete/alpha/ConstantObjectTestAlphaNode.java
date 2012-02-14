package at.sti2.spark.rete.alpha;

import at.sti2.spark.core.triple.RDFLiteral;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.rete.WorkingMemoryElement;

public class ConstantObjectTestAlphaNode extends ValueTestAlphaNode {

	public ConstantObjectTestAlphaNode(String testValue) {
		super(testValue);
	}

	@Override
	public void testActivation(WorkingMemoryElement wme) {
		
		String lexicalNodeValue = null;
		
		//TODO This can be further optimized by realizing from ontology/pattern if expected value is URI or literal
		if (wme.getStreamedTriple().getTriple().getObject() instanceof RDFURIReference)
			lexicalNodeValue = ((RDFURIReference)wme.getStreamedTriple().getTriple().getObject()).getValue().toString();
		else if (wme.getStreamedTriple().getTriple().getObject() instanceof RDFLiteral)
			lexicalNodeValue = ((RDFLiteral)wme.getStreamedTriple().getTriple().getObject()).getValue();

		if (!lexicalNodeValue.equals(testValue))
			return;
		
		activate(wme);
	}

}
