package at.sti2.spark.handler;

import java.util.List;

import org.apache.log4j.Logger;

import at.sti2.spark.core.solution.Match;
import at.sti2.spark.core.triple.TripleCondition;
import at.sti2.spark.grammar.pattern.Handler;

/**
 * This handler outputs the matched pattern to the console.
 * verbose: If set to true (default) the matched triple will be displayed, else only the number of match is displayed.
 * @author michaelrogger
 *
 */
public class ConsoleHandler implements SparkwaveHandler {

	private static Logger log = Logger.getLogger(ConsoleHandler.class);
	
	private long noMatches = 0;
	
	private boolean verbose = true;
	
	Handler handlerProperties = null;
	
	@Override
	public void init(Handler handlerProperties) {
		this.handlerProperties = handlerProperties;
		
		String strVerbose = handlerProperties.getValue("verbose");
		if(strVerbose!=null){
			if(strVerbose.equals("false"))
				verbose = false;
			else
				verbose = true;
		}
	}
	
	@Override
	public void invoke(Match match) throws SparkwaveHandlerException{

		noMatches++;
		log.info("Match no " + noMatches);
		
		if(verbose){
			//Format the output for the match
			final List<TripleCondition> conditions = handlerProperties.getTriplePatternGraph().getConstruct().getConditions();
			final String ntriplesOutput = match.outputNTriples(conditions);
			log.info(ntriplesOutput);
		}
		
	}
}
