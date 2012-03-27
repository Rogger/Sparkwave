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

package at.sti2.spark.rete.alpha;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.rete.WorkingMemoryElement;
import at.sti2.spark.rete.node.RETENode;

public class AlphaMemory {
	
	//Synchronized list which holds all WMEs currently in the memory
	private List <WorkingMemoryElement> items = null;
	private List <WorkingMemoryElement> permanentItems = null;
	
	private List <RETENode> successors  = null;
	
	public AlphaMemory(){
		items = Collections.synchronizedList(new ArrayList <WorkingMemoryElement> ());
		permanentItems = new ArrayList <WorkingMemoryElement>();
		successors  = new ArrayList <RETENode> ();
	}

	public void activate(WorkingMemoryElement wme){
		
		//Add it to the list
		addItem(wme);
		
		wme.addAlphaMemory(this);
		
		for (RETENode reteNode : successors)
			reteNode.rightActivate(wme);
	}

	public List<WorkingMemoryElement> getItems() {
		return items;
	}

	public List<RETENode> getSuccessors() {
		return successors;
	}
	
	public void addSuccesor(RETENode node){
		successors.add(node);
	}
	
	public void addItem(WorkingMemoryElement wme){
		
		if(!wme.getTriple().isPermanent()){
			synchronized(items){
				items.add(wme);
			}
		}else{
			permanentItems.add(wme);
		}
		
	}
	
	public void removeItem(WorkingMemoryElement wme){
		synchronized(items){
			items.remove(wme);
		}
	}
	

	public List<WorkingMemoryElement> getPermanentItems() {
		return permanentItems;
	}


	public String toString(){
		
		StringBuffer buffer = new StringBuffer();
		
		for (WorkingMemoryElement item : permanentItems){
			buffer.append('\n');
			buffer.append(item.getTriple().getRDFTriple().getLexicalValueOfField(RDFTriple.Field.SUBJECT));
			buffer.append(" ");
			buffer.append(item.getTriple().getRDFTriple().getLexicalValueOfField(RDFTriple.Field.PREDICATE));
			buffer.append(" ");
			buffer.append(item.getTriple().getRDFTriple().getLexicalValueOfField(RDFTriple.Field.OBJECT));
		}
		
		synchronized(items){
			for (WorkingMemoryElement item : items){
				buffer.append('\n');
				buffer.append(item.getTriple().getRDFTriple().getLexicalValueOfField(RDFTriple.Field.SUBJECT));
				buffer.append(" ");
				buffer.append(item.getTriple().getRDFTriple().getLexicalValueOfField(RDFTriple.Field.PREDICATE));
				buffer.append(" ");
				buffer.append(item.getTriple().getRDFTriple().getLexicalValueOfField(RDFTriple.Field.OBJECT));
			}
		}
		
		return buffer.toString();
	}
}
