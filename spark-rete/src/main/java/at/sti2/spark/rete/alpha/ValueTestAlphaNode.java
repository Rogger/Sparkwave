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

import at.sti2.spark.core.triple.RDFValue;

public abstract class ValueTestAlphaNode extends AlphaNode {

	protected RDFValue testValue = null;
	
	public ValueTestAlphaNode(RDFValue testValue) {
		super();
		this.testValue = testValue;
	}

	public RDFValue getTestValue() {
		return testValue;
	}

	public void setTestValue(RDFValue testValue) {
		this.testValue = testValue;
	}
	
	public String toString(){
		return "Value test node : " + testValue;
	}
}
