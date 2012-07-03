package at.sti2.spark.rete.alpha;

import at.sti2.spark.core.triple.RDFNumericLiteral;
import at.sti2.spark.core.triple.RDFValue;
import at.sti2.spark.rete.WorkingMemoryElement;

public final class FilterEqualAlphaNode extends AlphaNode {
	
//	protected final Expression expression;
	final RDFNumericLiteral testValue;
	
	public FilterEqualAlphaNode(RDFNumericLiteral testValue) {
		this.testValue = testValue;
	}

	@Override
	public void testActivation(WorkingMemoryElement wme) {
		RDFValue value = wme.getTriple().getRDFTriple().getObject();
		
		if(value instanceof RDFNumericLiteral){
			
			//test if values are equal
			if(testValue.equals(value)){
				activate(wme);
			}
		}

	}

}
