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

public class RDFBlankNode extends RDFValue {

	private static final long serialVersionUID = -8935456420817842903L;
	
	private String value;

	public RDFBlankNode(String value){
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public boolean equals(Object blankNode){
		
		RDFBlankNode object = null;
		
		if (!(blankNode instanceof RDFBlankNode))
			object = (RDFBlankNode)blankNode;
		else
			return false;
		
		return value.equals(object.getValue());
	}
	
	public String toString(){
		return value;
	}
}
