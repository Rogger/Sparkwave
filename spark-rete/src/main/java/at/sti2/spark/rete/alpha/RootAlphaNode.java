package at.sti2.spark.rete.alpha;

import at.sti2.spark.rete.WorkingMemoryElement;

public class RootAlphaNode extends AlphaNode {

	@Override
	public void testActivation(WorkingMemoryElement wme) {
		activate(wme);
	}
}
