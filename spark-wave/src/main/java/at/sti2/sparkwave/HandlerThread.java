/*
 * Copyright (c) 2012, University of Innsbruck, Austria.
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
package at.sti2.sparkwave;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import at.sti2.spark.core.solution.Match;
import at.sti2.spark.core.solution.OutputBuffer;
import at.sti2.spark.grammar.pattern.Handler;
import at.sti2.spark.grammar.pattern.Pattern;
import at.sti2.spark.handler.SparkwaveHandler;
import at.sti2.spark.handler.SparkwaveHandlerException;

/**
 * Manages and executes all handlers for a pattern
 * @author michaelrogger
 *
 */
public class HandlerThread extends Thread {

	static Logger logger = Logger.getLogger(HandlerThread.class);
	
	private OutputBuffer outputBuffer = null;
	private Pattern triplePatternGraph = null;
	
	public HandlerThread(Pattern patternGraph, OutputBuffer outputBuffer){
		this.outputBuffer = outputBuffer;
		this.triplePatternGraph = patternGraph;
		setName("Handler");
	}
	
	public void run() {
		Match match = null;
		List<SparkwaveHandler> handlerInstances = new ArrayList<SparkwaveHandler>();

		List<Handler> handlerPropertiesList = triplePatternGraph.getHandlers();

		// Instantiate handlers
		for (Handler handlerProperties : handlerPropertiesList) {
			String handlerClass = handlerProperties.getHandlerClass();
			SparkwaveHandler invoker = null;

			try {
				invoker = (SparkwaveHandler) Class.forName(handlerClass).newInstance();
				invoker.init(handlerProperties);
				handlerInstances.add(invoker);
				
				logger.info("An instance of " + invoker.getClass().getName() + " registered.");

			} catch (ClassNotFoundException e) {
				logger.error("Could not find class " + handlerClass);
			} catch (InstantiationException e) {
				logger.error("Could not instantiate class " + handlerClass);
			} catch (IllegalAccessException e) {
				logger.error(e);
			} catch (SparkwaveHandlerException e) {
				logger.error(e);
			}
		}

		while (true) {
			try {
				match = outputBuffer.get();
				for (SparkwaveHandler handlerInstance : handlerInstances) {
					handlerInstance.invoke(match);
				}
				// System.out.println( formatMatch(match));
			} catch (InterruptedException e) {
				logger.error(e);
			} catch (SparkwaveHandlerException e) {
				logger.error(e);
			}
		}

	}
	
}
