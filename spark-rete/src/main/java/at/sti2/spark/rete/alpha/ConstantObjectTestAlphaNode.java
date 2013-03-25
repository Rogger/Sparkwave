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
package at.sti2.spark.rete.alpha;

import org.apache.log4j.Logger;

import at.sti2.spark.core.triple.RDFLiteral;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.RDFValue;
import at.sti2.spark.rete.WorkingMemoryElement;

public class ConstantObjectTestAlphaNode extends ValueTestAlphaNode {

	static Logger log = Logger.getLogger(ConstantObjectTestAlphaNode.class);
	
	public ConstantObjectTestAlphaNode(RDFValue testValue) {
		super(testValue);
	}

	@Override
	public void testActivation(WorkingMemoryElement wme) {
		
		RDFValue lexicalNodeValue = null;
		
		//TODO This check is obsolete?
		if (wme.getTriple().getRDFTriple().getObject() instanceof RDFURIReference)
			lexicalNodeValue = ((RDFURIReference)wme.getTriple().getRDFTriple().getObject());
		else if (wme.getTriple().getRDFTriple().getObject() instanceof RDFLiteral)
			lexicalNodeValue = ((RDFLiteral)wme.getTriple().getRDFTriple().getObject());

		if (!lexicalNodeValue.equals(testValue))
			return;
		
		activate(wme);
	}

}
