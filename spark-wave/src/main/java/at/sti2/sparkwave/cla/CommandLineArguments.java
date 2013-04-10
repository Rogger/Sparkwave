package at.sti2.sparkwave.cla;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

public class CommandLineArguments {
	
	@Parameter(names = { "-p" }, description = "the pattern(s) that will be used", variableArity = true)
	private List<String> patterns = new ArrayList<String>();
	
	@Parameter(names = { "-c" }, description = "the config file that will be used")
	private String config = "config.xml";
	
	@Parameter(names = { "-v" }, description = "prints the version number")
	private boolean version;
	
	@Parameter(names = "-h", description = "prints this help", help = true)
	private boolean help;
	
	public List<String> getPatterns() {
		return patterns;
	}

	public boolean isVersion() {
		return version;
	}
	
	public boolean isHelp() {
		return help;
	}

	public String getConfig() {
		return config;
	}
	
}