package at.sti2.sparwave;

import java.util.List;

import junit.framework.Assert;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import at.sti2.sparkwave.configuration.ConfigurationModel;
import at.sti2.sparkwave.configuration.PPPluginConfig;
import at.sti2.sparkwave.configuration.SparkwaveConfigLoader;

public class ConfigurationLoaderTest {

	@Test
	public void testLoad() throws ConfigurationException {
		SparkwaveConfigLoader loader = new SparkwaveConfigLoader();
		ConfigurationModel configurationModel = loader.load("target/classes/config.xml");
		
		Assert.assertTrue(configurationModel.getPort() == 8080);
		
		List<PPPluginConfig> ppPlugins = configurationModel.getPPPluginsConfig();
		Assert.assertTrue(ppPlugins.size() == 2);
		Assert.assertTrue(ppPlugins.get(1).getProperties().size() == 2);
	}

}
