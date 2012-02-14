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

public class RDFURIReference extends RDFValue {

	private static final long serialVersionUID = -900633337817042617L;
	
//	private URI value;
	private String value;
	
//	public RDFURIReference(String value){
////		try {
//			this.value = value;
////		} catch (URISyntaxException e) {
////			e.printStackTrace();
////		}
//	}
	
	public RDFURIReference(String value){
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public boolean equals(Object uri){
		
		RDFURIReference object = null;
		
		if (!(uri instanceof RDFURIReference))
			object = (RDFURIReference)uri;
		else
			return false;
		
		return object.getValue().equals(value);
	}
	
	public String toString(){
		return value.toString();
	}
}
