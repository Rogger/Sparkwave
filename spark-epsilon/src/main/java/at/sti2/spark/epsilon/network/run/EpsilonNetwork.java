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
package at.sti2.spark.epsilon.network.run;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.epsilon.network.Network;
import at.sti2.spark.epsilon.network.Node;
import at.sti2.spark.rete.WorkingMemoryElement;
import at.sti2.spark.rete.alpha.AlphaNode;

public class EpsilonNetwork {
	
	static Logger logger = Logger.getLogger(EpsilonNetwork.class);
	
	// The hashtable which serves as a lookup to pickup the node which is processing a triple
	private NodeSelector nodeSelector = null;
	
	//The epsilon network class, property and link nodes
	private Network network = null;
	
	private AlphaNode rootAlphaNode = null;
	
	//Added for garbage collection purposes
	private List <Triple> processedTriples = null;
	
	private Hashtable <Triple, List<Token>> gcTokens = null;
	
	public EpsilonNetwork(){
		nodeSelector = new NodeSelector();
		network = new Network();
//		processedTriples = Collections.synchronizedList(new ArrayList<Triple>());
		processedTriples = new ArrayList<Triple>();
		gcTokens = new Hashtable <Triple, List<Token>> ();
	}
	
	public void setRootAlphaNode(AlphaNode rootAlphaNode){
		this.rootAlphaNode = rootAlphaNode;
	}

	public NodeSelector getNodeSelector() {
		return nodeSelector;
	}

	public Network getNetwork() {
		return network;
	}
	
    public List <Triple> getProcessedTriples(){
    	return processedTriples;
    }
    
    public List<Token> getTokenNodesByStreamedTriple(Triple streamedTriple){
    	return gcTokens.get(streamedTriple);
    }
    
    public void removeListByStreamedTriple(Triple streamedTriple){
    	gcTokens.remove(streamedTriple);
    }

	public void activate(Triple triple){
		
		//Selecting a node to put the triple inside of it
		Node entryNode = nodeSelector.lookup(triple);
		
		//Activating the node or bypassing the epsilon network 
		if (entryNode != null){
			List<Token> tokenNodes = new ArrayList <Token> ();
			entryNode.activateEntry(triple, tokenNodes);
			synchronized(processedTriples){
				processedTriples.add(triple);
				gcTokens.put(triple, tokenNodes);
			}
		}else{
			rootAlphaNode.activate(new WorkingMemoryElement(triple));
			logger.debug("Activating root alpha node...");
		}
	}
}
