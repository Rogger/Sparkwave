package at.sti2.sparkwave.configuration;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationModel {
	
	private int port = 0;
	private List<PPPluginConfig> ppPlugins;
	
	public ConfigurationModel() {
		ppPlugins = new ArrayList<PPPluginConfig>();
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public List<PPPluginConfig> getPPPluginsConfig() {
		return ppPlugins;
	}

	public void setPpPlugins(List<PPPluginConfig> ppPlugins) {
		this.ppPlugins = ppPlugins;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[port: ").append(port).append(";");
		sb.append("preprocessing plugins: "+ppPlugins).append("]");
		return sb.toString();
	}

}
