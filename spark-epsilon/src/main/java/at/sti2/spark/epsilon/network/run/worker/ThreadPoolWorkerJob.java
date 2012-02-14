package at.sti2.spark.epsilon.network.run.worker;

import at.sti2.spark.core.stream.StreamedTriple;
import at.sti2.spark.epsilon.network.run.NodeSelector;

/**
 * The class holds all the needed artefacts for a strimed triple to be processed through the network
 * 
 * @author skomazec
 *
 */
public class ThreadPoolWorkerJob {

	private StreamedTriple triple;
	private NodeSelector nodeSelector;
	
	public ThreadPoolWorkerJob(StreamedTriple triple, NodeSelector nodeSelector) {
		this.triple = triple;
		this.nodeSelector = nodeSelector;
	}
	public StreamedTriple getTriple() {
		return triple;
	}
	public void setTriple(StreamedTriple triple) {
		this.triple = triple;
	}
	public NodeSelector getNodeSelector() {
		return nodeSelector;
	}
	public void setNodeSelector(NodeSelector nodeSelector) {
		this.nodeSelector = nodeSelector;
	}
}
