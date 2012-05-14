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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import at.sti2.spark.core.collect.IndexStructure;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFValue;
import at.sti2.spark.core.triple.RDFTriple.Field;
import at.sti2.spark.rete.WorkingMemoryElement;
import at.sti2.spark.rete.beta.JoinNodeTest;
import at.sti2.spark.rete.node.RETENode;

import com.google.common.collect.LinkedHashMultimap;

public class AlphaMemory {
	
	static Logger logger = Logger.getLogger(AlphaMemory.class);
	
	final IndexStructure<WorkingMemoryElement> indexStructure;
	
	private List <RETENode> successors  = null;
	
	public AlphaMemory(){
		
		successors  = new ArrayList <RETENode> ();
		indexStructure = new IndexStructure<WorkingMemoryElement>();
		
	}
	
	public void activateIndexesForTests (List<JoinNodeTest> tests){
		
		for(JoinNodeTest test :tests){
			Field arg1Field = test.getArg1Field();
			
			if(arg1Field == Field.SUBJECT){
				indexStructure.setSubjectIndexing(true);
			}else if(arg1Field == Field.PREDICATE){
				indexStructure.setPredicateIndexing(true);
			}else if(arg1Field == Field.OBJECT){
				indexStructure.setObjectIndexing(true);
			}
			
		}
	}
	
	public IndexStructure<WorkingMemoryElement> getIndexStructure() {
		return indexStructure;
	}

	public void activate(WorkingMemoryElement wme) {

		if(wme.getTriple().isPermanent())
			indexStructure.addElement(wme.getTriple().getRDFTriple(), wme, 0);
		else
			indexStructure.addElement(wme.getTriple().getRDFTriple(), wme, wme.getTriple().getTimestamp());

//		wme.addAlphaMemory(this);

		for (RETENode reteNode : successors)
			reteNode.rightActivate(wme);
	}
	

	public List<RETENode> getSuccessors() {
		return successors;
	}

	public void addSuccesor(RETENode node) {
		successors.add(node);
	}
	
	public String toString() {

		StringBuffer buffer = new StringBuffer();

//		for (WorkingMemoryElement item : permanentItems) {
//			buffer.append('\n');
//			buffer.append(item.getTriple().getRDFTriple().getValueOfField(RDFTriple.Field.SUBJECT));
//			buffer.append(" ");
//			buffer.append(item.getTriple().getRDFTriple().getValueOfField(RDFTriple.Field.PREDICATE));
//			buffer.append(" ");
//			buffer.append(item.getTriple().getRDFTriple().getValueOfField(RDFTriple.Field.OBJECT));
//		}
//		
////		synchronized(items){
//			for (WorkingMemoryElement item : items){
//				buffer.append('\n');
//				buffer.append(item.getTriple().getRDFTriple().getValueOfField(RDFTriple.Field.SUBJECT));
//				buffer.append(" ");
//				buffer.append(item.getTriple().getRDFTriple().getValueOfField(RDFTriple.Field.PREDICATE));
//				buffer.append(" ");
//				buffer.append(item.getTriple().getRDFTriple().getValueOfField(RDFTriple.Field.OBJECT));
//			}
//		}
		
		return buffer.toString();
	}
}
