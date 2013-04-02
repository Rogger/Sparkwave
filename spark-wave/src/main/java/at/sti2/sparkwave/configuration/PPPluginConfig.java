package at.sti2.sparkwave.configuration;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Plugin configuration model
 * @author michaelrogger
 *
 */
public class PPPluginConfig {
	
	private String className = null;
	private Map<String,String> properties = null;
	
	public PPPluginConfig() {
		properties = new TreeMap<String, String>();
	}
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public Map<String, String> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
	public void addProperty(String key, String value){
		properties.put(key, value);
	}
	
	public String getProperty(String key){
		return properties.get(key);
	}
	
	public Set<String> getPropertyKeys(){
		return properties.keySet();
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[className: ").append(className).append(";");
		sb.append("Properties: "+properties).append("]");
		return sb.toString();
	}

}
