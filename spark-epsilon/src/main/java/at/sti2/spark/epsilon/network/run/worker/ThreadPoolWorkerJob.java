package at.sti2.spark.epsilon.network.run.worker;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.epsilon.network.run.NodeSelector;

/**
 * The class holds all the needed artefacts for a strimed triple to be processed through the network
 * 
 * @author skomazec
 *
 */
public class ThreadPoolWorkerJob {

	private Triple triple;
	private NodeSelector nodeSelector;
	
	public ThreadPoolWorkerJob(Triple triple, NodeSelector nodeSelector) {
		this.triple = triple;
		this.nodeSelector = nodeSelector;
	}
	public Triple getTriple() {
		return triple;
	}
	public void setTriple(Triple triple) {
		this.triple = triple;
	}
	public NodeSelector getNodeSelector() {
		return nodeSelector;
	}
	public void setNodeSelector(NodeSelector nodeSelector) {
		this.nodeSelector = nodeSelector;
	}
}
