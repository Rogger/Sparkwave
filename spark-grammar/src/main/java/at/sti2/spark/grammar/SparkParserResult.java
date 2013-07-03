package at.sti2.spark.grammar;

import at.sti2.spark.grammar.pattern.Pattern;


/**
 * Immutable SparkParserResult
 * @author michaelrogger
 */
public final class SparkParserResult{

	private final Pattern pattern;
	private final String warnings;
	
	public SparkParserResult(Pattern pattern, String warnings) {
		this.pattern = pattern;
		this.warnings = warnings;
	}
	
	public Pattern getPattern(){
		return pattern;
	}
	
	public String getWarnings(){
		return warnings;
	}
}
