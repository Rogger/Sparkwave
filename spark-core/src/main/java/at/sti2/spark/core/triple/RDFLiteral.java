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

public class RDFLiteral extends RDFValue {
	
	private static final long serialVersionUID = 4244499579141058280L;
	
	private String value;
	private RDFURIReference datatypeURI;
	private String languageTag;
	
	public RDFLiteral(String value, RDFURIReference datatypeURI, String languageTag) {
		super();
		this.value = value;
		this.datatypeURI = datatypeURI;
		this.languageTag = languageTag;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public RDFURIReference getDatatypeURI() {
		return datatypeURI;
	}
	
	public void setDatatypeURI(RDFURIReference datatypeURI) {
		this.datatypeURI = datatypeURI;
	}
	
	public String getLanguageTag() {
		return languageTag;
	}
	
	public void setLanguageTag(String languageTag) {
		this.languageTag = languageTag;
	}
	
	public boolean equals(Object rdfLiteral){
		
		RDFLiteral object = null;
		
		if (!(rdfLiteral instanceof RDFLiteral))
			object = (RDFLiteral)rdfLiteral;
		else
			return false;
		
		//Check language tags
		if ( (object.getLanguageTag()!=null)&&(languageTag==null)||
			 (object.getLanguageTag()==null)&&(languageTag!=null))
			return false;
		
		if ((object.getLanguageTag()!=null)&&(languageTag!=null))
			if (!object.getLanguageTag().equals(languageTag))
				return false;
		
		//Check datatypeURIs
		if ( (object.getDatatypeURI()!=null)&&(datatypeURI==null)||
				 (object.getDatatypeURI()==null)&&(datatypeURI!=null))
				return false;
			
			if ((object.getDatatypeURI()!=null)&&(datatypeURI!=null))
				if (!object.getDatatypeURI().equals(datatypeURI))
					return false;
			
		return object.getValue().equals(value);
			
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
