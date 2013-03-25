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

package at.sti2.spark.core.triple;

import at.sti2.spark.core.triple.RDFTriple.Field;

/**
 * 
 * Represents triple constant test
 * 
 * @author srdkom
 */
public class TripleConstantTest {

	private RDFValue lexicalTestSymbol = null;
	private RDFTriple.Field testField = null;
	
	public TripleConstantTest(RDFValue lexicalTestSymbol, Field testField) {
		super();
		this.lexicalTestSymbol = lexicalTestSymbol;
		this.testField = testField;
	}
	public RDFValue getLexicalTestSymbol() {
		return lexicalTestSymbol;
	}
	public void setLexicalTestSymbol(RDFValue lexicalTestSymbol) {
		this.lexicalTestSymbol = lexicalTestSymbol;
	}
	public RDFTriple.Field getTestField() {
		return testField;
	}
	public void setTestField(RDFTriple.Field testField) {
		this.testField = testField;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(testField.toString());
		buffer.append(" ");
		buffer.append(lexicalTestSymbol);
		
		return buffer.toString();
	}
}
