package at.sti2.spark.preprocess;

import java.io.InputStream;
import java.io.OutputStream;

public interface PreProcess extends Runnable{
	
	/**
	 * Initialization method is called first and passes the input- and outputstream
	 * @param in the input stream
	 * @param out the processed results should be written to this stream
	 */
	public void init(InputStream in, OutputStream out);
	
	/**
	 * This method is called to pass parameter that where found in the xml configuration
	 * @param key
	 * @param value
	 */
	public void setProperty(String key, String value);
	
	/**
	 * This method is called to do the actual processing.
	 */
	public void process();
		
}
