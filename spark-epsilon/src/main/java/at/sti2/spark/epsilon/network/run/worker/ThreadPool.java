package at.sti2.spark.epsilon.network.run.worker;

import java.util.ListIterator;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * 
 * @author skomazec
 *
 * http://java.sun.com/developer/Books/javaprogramming/threads/chap13.pdf
 */
public class ThreadPool {

	static Logger logger = Logger.getLogger(ThreadPool.class);
	
	private static ThreadPool threadPool;
	
	private int preworkerCount;
	private int workerCount;
	private int maxIdleWorkers;
	private int maxWorkers;
	private Vector<ThreadPoolWorker> usedWorkers;
	private Vector<ThreadPoolWorker> freeWorkers;
	
	public static ThreadPool getThreadPool() {
		return threadPool;
	}
	
	static {
		ResourceBundle bundle = PropertyResourceBundle.getBundle("at.sti2.spark.epsilon.network.run.worker.ThreadPool");

		int preworkerCount = 0;
		int maxIdleWorkers = 10;
		int maxWorkers = 10;

		try {

			preworkerCount = Integer.parseInt(bundle.getString("preworkerCount"));
			logger.debug("The value of preworkerCount is " + preworkerCount);
			
			maxIdleWorkers = Integer.parseInt(bundle.getString("maxIdleWorkers"));
			logger.debug("The value of maxIdleWorkers is " + maxIdleWorkers);
			
			maxWorkers = Integer.parseInt(bundle.getString("maxWorkers"));
			logger.debug("The value of maxWorkers is " + maxWorkers);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			threadPool = new ThreadPool(preworkerCount, maxIdleWorkers, maxWorkers);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	protected ThreadPool(int aPreworkerCount, int aMaxIdleWorkers, int aMaxWorkers) {

		freeWorkers = new Vector<ThreadPoolWorker>();
		usedWorkers = new Vector<ThreadPoolWorker>();

		preworkerCount = aPreworkerCount;
		maxIdleWorkers = aMaxIdleWorkers;
		maxWorkers = aMaxWorkers;

		for (int i = 0; i < preworkerCount; i++) {
			ThreadPoolWorker worker = new ThreadPoolWorker(workerCount++);
			freeWorkers.addElement(worker);
		}

		workerCount = preworkerCount;
		logger.debug("WorkerPool initialized.");	
	}

	public synchronized ThreadPoolWorker checkOut() {

		ThreadPoolWorker worker = null;

		if (freeWorkers.size() > 0) {

			worker = freeWorkers.elementAt(0);
			freeWorkers.removeElementAt(0);
			usedWorkers.addElement(worker);
		} else {
			if (workerCount < maxWorkers) {
				worker = new ThreadPoolWorker(workerCount++);
				workerCount++;
			} else {
				try {
					wait();
					worker = freeWorkers.elementAt(0);

					freeWorkers.removeElementAt(0);
					usedWorkers.addElement(worker);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		logger.debug("ThreadPoolWorker taken from the pool [Free: " + freeWorkers.size() + "Used: " + usedWorkers.size() + "]. ");
		
		return worker;
	}

	public synchronized void checkIn(ThreadPoolWorker aWorker) {

		if (aWorker == null)
			return;

		if (usedWorkers.removeElement(aWorker)) {

			freeWorkers.addElement(aWorker);

			while (freeWorkers.size() > maxIdleWorkers) {
				int lastOne = freeWorkers.size() - 1;
				freeWorkers.removeElementAt(lastOne);
			}
			notify();
		}
		
		logger.debug("ThreadPoolWorker returned to the pool [Free: " + freeWorkers.size() + "Used: " + usedWorkers.size() + "]. ");
	}
	
	public void execute(ThreadPoolWorkerJob job) throws InterruptedException {
		
		//Will block until new thread becomes available
		ThreadPoolWorker worker = checkOut();
		worker.addJob(job);
	}
	
	public void stopRequestWorkers(){
		
		ListIterator <ThreadPoolWorker> workerIterator = freeWorkers.listIterator();
		while (workerIterator.hasNext()){
			ThreadPoolWorker worker = workerIterator.next();
			worker.stopRequest();
		}
		freeWorkers.clear();
		
		try {
			Thread.sleep(250);
		} catch (InterruptedException iex) {}
		
		workerIterator = usedWorkers.listIterator();
		while (workerIterator.hasNext()){
			ThreadPoolWorker worker = workerIterator.next();
			worker.stopRequest();
		}
		usedWorkers.clear();
	}
	
	
}
