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
package at.sti2.spark.handler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import at.sti2.spark.core.solution.Match;
import at.sti2.spark.core.triple.TripleCondition;
import at.sti2.spark.grammar.pattern.Handler;

public class FileHandler implements SparkwaveHandler {

	protected static Logger logger = Logger.getLogger(FileHandler.class);
	private Handler handlerProperties = null;
	
	//file logger
	private File path = null;
	private FileWriter writer = null;
	
	private int count = 0;
	
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	@Override
	public void init(Handler handlerProperties) throws SparkwaveHandlerException {
		this.handlerProperties = handlerProperties;
		this.path = new File(handlerProperties.getValue("path"));
		try {
			if(!path.exists()){
				path.createNewFile();				
			}
		} catch (IOException e) {
			throw new SparkwaveHandlerException(e);
		}
	}
	
	@Override
	public void invoke(Match match) throws SparkwaveHandlerException{
		
		logger.debug("Invoking FileHandler");
		Date date = new Date();
		
		final List<TripleCondition> conditions = handlerProperties.getTriplePatternGraph().getConstruct().getConditions();
		final String ntriplesOutput = match.outputNTriples(conditions);
		
		//store match to file
		StringBuffer sb = new StringBuffer();
		sb.append(dateFormat.format(date)).append(" match_number:").append(count++).append("\n");
		sb.append(ntriplesOutput);
		try {
			writer = new FileWriter(path,true);
			writer.write(sb.toString());
			writer.close();
			logger.info("Match number "+count+" written to file "+path);
		} catch (IOException e) {
			throw new SparkwaveHandlerException(e);
		}
		
	}
		
}
