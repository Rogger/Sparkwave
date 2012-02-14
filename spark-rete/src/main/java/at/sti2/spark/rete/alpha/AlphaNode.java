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
import java.util.List;

import at.sti2.spark.rete.WorkingMemoryElement;

public abstract class AlphaNode {

	protected List <AlphaNode> children = null;
	protected AlphaMemory outputMemory = null;
	
	public AlphaNode(){
		children = new ArrayList <AlphaNode> ();
	}
	
	public void addChild(AlphaNode node){
		children.add(node);
	}
	
	public void removeChild(AlphaNode node){
		children.remove(node);
	}

	public AlphaMemory getOutputMemory() {
		return outputMemory;
	}

	public void setOutputMemory(AlphaMemory outputMemory) {
		this.outputMemory = outputMemory;
	}
	
	public List<AlphaNode> getChildren() {
		return children;
	}

	public abstract void testActivation(WorkingMemoryElement wme);
	
	public void activate(WorkingMemoryElement wme){
		
		if (outputMemory != null)
			outputMemory.activate(wme);
		
		for (AlphaNode child : children)
			child.testActivation(wme);
	}
}
