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
package at.sti2.spark.output;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import at.sti2.spark.core.solution.Match;
import at.sti2.spark.core.solution.OutputBuffer;
import at.sti2.spark.core.triple.RDFValue;

public class SparkweaveNetworkOutputThread extends Thread {

	static Logger logger = Logger.getLogger(SparkweaveNetworkOutputThread.class);
	
	private OutputBuffer outputBuffer = null;
	
	public SparkweaveNetworkOutputThread(OutputBuffer outputBuffer){
		this.outputBuffer = outputBuffer;
	}
	
	public void run(){
		Match match = null;
		while(true){
			try {
				match = outputBuffer.get();
				System.out.println(formatMatch(match));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String formatMatch(Match match){
		StringBuffer buffer = new StringBuffer();
		Enumeration <String> keyEnum = match.getVariableBindings().keys();
		while(keyEnum.hasMoreElements()){
			String variableId = (String)keyEnum.nextElement();
			RDFValue value = (RDFValue)match.getVariableBindings().get(variableId);
			buffer.append(variableId);
			buffer.append(" : ");
			buffer.append(value.toString());
			buffer.append('\n');
		}		
		return buffer.toString();
	}
}
