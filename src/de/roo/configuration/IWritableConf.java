package de.roo.configuration;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface IWritableConf extends IConf {

	public void setValue(String confKey, String confValue);
	public void setValue(String confKey, int confValue);
	public void setValue(String confKey, long confValue);
	public void setValue(String confKey, float confValue);
	public void setValue(String confKey, double confValue);
	public void setValue(String confKey, boolean confValue);
	public void loadValsFrom(IConf conf);
	
}
