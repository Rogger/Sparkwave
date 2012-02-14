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
package at.sti2.spark.core.stream;

import java.io.Serializable;

import at.sti2.spark.core.triple.RDFTriple;

public class StreamedTriple implements Serializable{
	
	private static final long serialVersionUID = 3421595263951329008L;
	
	private RDFTriple triple = null;
	private long timestamp = 0l;
	
	/**
	 * Hashed context value (e.g., stream identifier)
	 */
	private long context =0l;
	
	public StreamedTriple(RDFTriple triple, long timestamp, long context) {
		this.triple = triple;
		this.timestamp = timestamp;
		this.context = context;
	}
	public RDFTriple getTriple() {
		return triple;
	}
	public void setTriple(RDFTriple triple) {
		this.triple = triple;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public long getContext() {
		return context;
	}
	public void setContext(long context) {
		this.context = context;
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
