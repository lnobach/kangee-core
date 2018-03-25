package de.roo.configuration;

import java.util.Map;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface IConf {

	public int getValueInt(String cfgKey);
	
	public boolean getValueBoolean(String cfgKey);
	
	public String getValueString(String cfgKey);
	
	public long getValueLong(String cfgKey);
	
	public double getValueDouble(String cfgKey);
	
	public float getValueFloat(String cfgKey);
	
	
	
	public int getValueInt(String cfgKey, int defaultValue);
	
	public boolean getValueBoolean(String cfgKey, boolean defaultValue);
	
	public String getValueString(String cfgKey, String defaultValue);
	
	public long getValueLong(String cfgKey, long defaultValue);
	
	public double getValueDouble(String cfgKey, double defaultValue);
	
	public float getValueFloat(String cfgKey, float defaultValue);
	
	public Map<String, String> getAsMap();
	
}
