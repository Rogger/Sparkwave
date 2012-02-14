package at.sti2.spark.epsilon.network.run.worker;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

public class ThreadPoolWorker {

	static Logger logger = Logger.getLogger(ThreadPoolWorker.class);
	
	private int workerId;
	private Thread internalThread;
	private volatile boolean noStopRequest;
	
	//The FIFO structure holding jobs (streamed triple, entrance point) which are due to be processed
	private LinkedBlockingQueue <ThreadPoolWorkerJob> jobQueue ;
	
	public ThreadPoolWorker(int workerId){
		this.workerId = workerId;
		noStopRequest = true;
		
		jobQueue = new LinkedBlockingQueue <ThreadPoolWorkerJob> ();
		
		Runnable r = new Runnable() {

			public void run() {
				try{
					runWork();
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		
		internalThread = new Thread(r);
		internalThread.start();
	}
	
	public void addJob(ThreadPoolWorkerJob job){
		try {
			jobQueue.put(job);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void runWork(){
		while(noStopRequest){
			try {
				logger.debug("workerId=" + workerId + ", ready to work.");
			
				//Wait until there is a request
				ThreadPoolWorkerJob job = jobQueue.take();
				ThreadPoolJobRunner runner = new ThreadPoolJobRunner(job);				
			
				logger.debug("workerId=" + workerId + ", starting execution for a new streamed triple.");
				runIt(runner);
				
			}catch(InterruptedException iex){
				Thread.currentThread().interrupt();
			}
		}
	}
	
	private void runIt(Runnable r){
		try{
			r.run();
		}catch (Exception runex) {
			logger.error("Uncaught exception fell through from run()", runex);
		}finally {
			Thread.interrupted();
		}
	}
	
	public void stopRequest() {
	  logger.debug("workerId=" + workerId + ", stopRequest() received.");
	  noStopRequest = false;
	  internalThread.interrupt();
	}
	
	public boolean isAlive() {
		return internalThread.isAlive();
	}
}
