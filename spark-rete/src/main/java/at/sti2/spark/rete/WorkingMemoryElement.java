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

package at.sti2.spark.rete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.rete.alpha.AlphaMemory;

public class WorkingMemoryElement {

	private Triple triple = null;

	// Added for retracting purposes
	private List<AlphaMemory> alphaMems = null;

	private List<Token> tokens = null;

	public WorkingMemoryElement() {
		alphaMems = new ArrayList<AlphaMemory>();
		tokens = new ArrayList<Token>();
	}

	public WorkingMemoryElement(Triple triple) {
		this.triple = triple;
		alphaMems = new ArrayList<AlphaMemory>();
		tokens = new ArrayList<Token>();
	}

	public Triple getTriple() {
		return triple;
	}

	public void setStreamedTriple(Triple triple) {
		this.triple = triple;
	}

	public List<AlphaMemory> getAlphaMems() {
		return alphaMems;
	}

	public void addAlphaMemory(AlphaMemory alphaMem) {
		alphaMems.add(alphaMem);
	}

	public void removeAlphaMemory(AlphaMemory alphaMem) {
		alphaMems.remove(alphaMem);
	}

	public void addToken(Token token) {
		tokens.add(token);
	}

	public void removeToken(Token token) {
		tokens.remove(token);
	}

	public void remove() {

		// Remove occurrence from each alpha memory
		// REMOVAL FROM ALPHA MEMORY IS DONE IN GC
		// for (AlphaMemory alphamem : alphaMems){
		// alphamem.removeItem(this);

		// If alpha memory just became empty
		// TODO Examine again why in the literature we have this behavior, I
		// believe it is not needed
		// if (alphamem.getItems().isEmpty())
		// for (RETENode node : alphamem.getSuccessors())
		// if (node instanceof JoinNode)
		// ((JoinNode)node).getParent().getChildren().remove(node);
		// }

		// Remove all the tokens having an occurrence of WME
		for (Iterator<Token> tokenIterator = tokens.iterator(); tokenIterator
				.hasNext();) {
			Token tokenToDelete = tokenIterator.next();
			tokenToDelete.deleteTokenAndDescendents();
			tokenIterator.remove();
		}
	}

	public String toString() {
		return triple.toString();
	}
}
