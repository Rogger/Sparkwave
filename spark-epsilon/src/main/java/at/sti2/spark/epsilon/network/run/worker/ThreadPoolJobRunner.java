package at.sti2.spark.epsilon.network.run.worker;

import at.sti2.spark.epsilon.network.Node;

public class ThreadPoolJobRunner implements Runnable{
	
	private ThreadPoolWorkerJob job = null;
	
	public ThreadPoolJobRunner(ThreadPoolWorkerJob job){
		this.job = job;
	}
	
	public void run(){
		//TODO This is where the processing implementation should go
		
		//Selecting the node at which the thread should 
		Node entranceNode = job.getNodeSelector().lookup(job.getTriple());
	}

}
