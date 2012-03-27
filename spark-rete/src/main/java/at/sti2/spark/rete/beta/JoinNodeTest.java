/*
 * Copyright (c) 2010, University of Innsbruck, Austria.
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

package at.sti2.spark.rete.beta;

import at.sti2.spark.core.triple.RDFTriple;

public class JoinNodeTest {

	private RDFTriple.Field arg1Field = null;
	private RDFTriple.Field arg2Field = null;
	private int arg2ConditionNumber;
	
	public JoinNodeTest(RDFTriple.Field arg1Field, RDFTriple.Field arg2Field,
			int arg2ConditionNumber) {
		
		this.arg1Field = arg1Field;
		this.arg2Field = arg2Field;
		this.arg2ConditionNumber = arg2ConditionNumber;
	}
	public RDFTriple.Field getArg1Field() {
		return arg1Field;
	}
	public void setArg1Field(RDFTriple.Field arg1Field) {
		this.arg1Field = arg1Field;
	}
	public RDFTriple.Field getArg2Field() {
		return arg2Field;
	}
	public void setArg2Field(RDFTriple.Field arg2Field) {
		this.arg2Field = arg2Field;
	}
	public int getArg2ConditionNumber() {
		return arg2ConditionNumber;
	}
	public void setArg2ConditionNumber(int arg2ConditionNumber) {
		this.arg2ConditionNumber = arg2ConditionNumber;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(arg1Field);
		buffer.append(" ");
		buffer.append(arg2Field);
		buffer.append(" ");
		buffer.append(arg2ConditionNumber);
		
		return buffer.toString();
	}
}