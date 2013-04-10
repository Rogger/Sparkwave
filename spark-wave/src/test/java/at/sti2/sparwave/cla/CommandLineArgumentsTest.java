package at.sti2.sparwave.cla;

import static junit.framework.Assert.*;

import org.junit.Test;

import com.beust.jcommander.JCommander;

import at.sti2.sparkwave.cla.CommandLineArguments;

public class CommandLineArgumentsTest {
	
	@Test
	public void testCLP() {
		
		CommandLineArguments cla = new CommandLineArguments();
		
		String[] argv = new String[]{"-v"};
		new JCommander(cla,argv);
		assertTrue(cla.isVersion());
		
		argv = new String[]{"-p", "pattern1", "pattern2", "-c", "configx.xml"};
		new JCommander(cla,argv);
		assertTrue(cla.getPatterns().size() == 2);
		assertTrue("configx.xml".equals(cla.getConfig()));
	}

}
