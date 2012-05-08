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

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Immutable RDFLiteral
 * @author srdjankomazec
 * @author michaelrogger
 *
 */
public class RDFLiteral extends RDFValue {
	
	private static final long serialVersionUID = 4244499579141058280L;
	
	private final String value;
	private final RDFURIReference datatypeURI;
	private final String languageTag;
	
	// cached hash code
	private int hashCode = 0;
	
	public RDFLiteral(String value, RDFURIReference datatypeURI, String languageTag) {
		super();
		this.value = value;
		this.datatypeURI = datatypeURI;
		this.languageTag = languageTag;
	}
	
	public String getValue() {
		return value;
	}
	
	public RDFURIReference getDatatypeURI() {
		return datatypeURI;
	}
	
	public String getLanguageTag() {
		return languageTag;
	}
	
	public boolean equals(Object that){
		
		//reference check (fast)
		if(this == that) return true;
		
		//type check and null check
		if(!(that instanceof RDFLiteral)) return false;
		
		RDFLiteral rdfLiteral = (RDFLiteral)that;
				
		//Check language tags
		if ( (rdfLiteral.getLanguageTag()!=null)&&(languageTag==null)||
			 (rdfLiteral.getLanguageTag()==null)&&(languageTag!=null))
			return false;
		
		if ((rdfLiteral.getLanguageTag()!=null)&&(languageTag!=null))
			if (!rdfLiteral.getLanguageTag().equals(languageTag))
				return false;
		
		//Check datatypeURIs
		if ( (rdfLiteral.getDatatypeURI()!=null)&&(datatypeURI==null)||
				 (rdfLiteral.getDatatypeURI()==null)&&(datatypeURI!=null))
				return false;
			
			if ((rdfLiteral.getDatatypeURI()!=null)&&(datatypeURI!=null))
				if (!rdfLiteral.getDatatypeURI().equals(datatypeURI))
					return false;
			
		return rdfLiteral.getValue().equals(value);
			
	}
	
	@Override
	public int hashCode() {
		if(hashCode == 0){
			hashCode = new HashCodeBuilder(11,37)
					.append(value)
					.append(datatypeURI)
					.append(languageTag)
					.toHashCode();			
		}
		return hashCode;
	}
	
	public String toString(){
		
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(value);
		
		if (languageTag != null)
			buffer.append(languageTag);
		else if (datatypeURI != null){
			buffer.append("^^");
			buffer.append(datatypeURI.toString());
		}
			
		return buffer.toString();
	}
}
