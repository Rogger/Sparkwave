package at.sti2.sparkwave.configuration;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;


public class SparkwaveConfigLoader {
	
	XMLConfiguration config = null;
	
	public SparkwaveConfigLoader() {
	}
	
	public ConfigurationModel load(String fileName) throws ConfigurationException{
		
		config = new XMLConfiguration();
		config.setDelimiterParsingDisabled(true);
		config.setAttributeSplittingDisabled(true);
		config.load(fileName);
		
		ConfigurationModel model = new ConfigurationModel();
		model.setPort(getPort());
		model.setPpPlugins(getPreprocessingPlugins());
		return model;
	}
	
	private int getPort(){
		return config.getInt("port");
	}
	
	private List<PPPluginConfig> getPreprocessingPlugins(){
		
		List<PPPluginConfig> ppPlugins = new ArrayList<PPPluginConfig>();
		
		for( HierarchicalConfiguration subConfig : config.configurationsAt("preprocess.plugin") ){
			String className = subConfig.getString("class");
			List<Object> propertyType = subConfig.getList("properties.property[@type]");
			List<Object> propertyValue = subConfig.getList("properties.property");
			PPPluginConfig ppPlugin = new PPPluginConfig();
			ppPlugin.setClassName(className);
			
			for(int i = 0; i < propertyType.size(); i++){
				ppPlugin.addProperty(propertyType.get(i).toString(), propertyValue.get(i).toString());				
			}
			
			ppPlugins.add(ppPlugin);
		}
		
		return ppPlugins;
	}

}
