/*
 * Copyright (c) 2011, University of Innsbruck, Austria.
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

import java.io.Serializable;

public class RDFTriple implements Serializable{
	
	private static final long serialVersionUID = -2301534685285465150L;

	public enum Field {SUBJECT, PREDICATE, OBJECT};
	
	private RDFValue subject;
	private RDFValue predicate;
	private RDFValue object;
	
	public RDFTriple(){
		
	}
	
	public RDFTriple(RDFValue subject, RDFValue predicate, RDFValue object){
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}
	
	public RDFValue getSubject() {
		return subject;
	}
	public void setSubject(RDFValue subject) {
		this.subject = subject;
	}
	public RDFValue getPredicate() {
		return predicate;
	}
	public void setPredicate(RDFValue predicate) {
		this.predicate = predicate;
	}
	public RDFValue getObject() {
		return object;
	}
	public void setObject(RDFValue object) {
		this.object = object;
	}
	
	//TODO Maybe to think about making equality relationships based on the lexical values?
	public boolean equals(Object triple){
		return subject.equals(((RDFTriple)triple).getSubject())&&
		       predicate.equals(((RDFTriple)triple).getPredicate())&&
		       object.equals(((RDFTriple)triple).getObject());
	}
	
	public String getLexicalValueOfField(Field field){
		
		String value = null;
		
		if (field == Field.SUBJECT){
			if (subject instanceof RDFURIReference)
				value = ((RDFURIReference)subject).getValue().toString();
		} else if (field == Field.PREDICATE){
			if (predicate instanceof RDFURIReference)
				value = ((RDFURIReference)predicate).getValue().toString();
		} else if (field == Field.OBJECT){
			if (object instanceof RDFLiteral)
				value = ((RDFLiteral)object).getValue();
			else if (object instanceof RDFURIReference)
				value = ((RDFURIReference)object).getValue().toString();
		}
		
		return value;
	}
	
	public String toString(){
		
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(subject.toString());
		buffer.append(" ");
		buffer.append(predicate.toString());
		buffer.append(" ");
		buffer.append(object.toString());
		
		return buffer.toString();
	}
}
