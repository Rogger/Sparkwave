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
package at.sti2.spark.core.stream;

import java.io.Serializable;

import at.sti2.spark.core.triple.RDFTriple;

public class Triple implements Serializable{

	private static final long serialVersionUID = 1L;
	
	protected RDFTriple triple = null;
	
	private long timestamp = 0l;
	
	private boolean permanent = false;
	
	/**
	 * Hashed context value (e.g., stream identifier)
	 */
	protected long context = 0l;
	
	public Triple(RDFTriple triple, long timestamp, boolean permanent, long context) {
		this.triple = triple;
		this.context = context;
		this.timestamp = timestamp;
		this.permanent = permanent;
	}
	
	public Triple(RDFTriple triple, long timestamp, long context) {
		this.triple = triple;
		this.context = context;
		this.timestamp = timestamp;
		permanent = false;
	}
	
	public RDFTriple getRDFTriple() {
		return triple;
	}
	public void setRDFTriple(RDFTriple triple) {
		this.triple = triple;
	}
	public long getContext() {
		return context;
	}
	public void setContext(long context) {
		this.context = context;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public boolean isPermanent() {
		return permanent;
	}
	
	public void setPermanent(boolean permanent) {
		this.permanent = permanent;
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append('[');
		buffer.append(timestamp);
		buffer.append("] ");
		buffer.append(triple.toString());
		return buffer.toString();
	}

}
