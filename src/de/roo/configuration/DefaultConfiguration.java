package de.roo.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author Leo Nobach
 *
 */
public class DefaultConfiguration implements IWritableConf {

	public DefaultConfiguration(Map<String, String> confValues) {
		this.confValues = confValues;
	}
	
	public DefaultConfiguration() {
		this(new HashMap<String, String>());
	}

	Map<String, String> confValues;
	
	public String toString() {
		return confValues.toString();
	}
	
	@Override
	public boolean getValueBoolean(String cfgKey) {
		String strVal = getValueString(cfgKey);
		if ("true".equalsIgnoreCase(strVal)) return true;
		if ("false".equalsIgnoreCase(strVal)) return false;
		throw new BadValueTypeException(strVal);
	}

	@Override
	public boolean getValueBoolean(String cfgKey, boolean defaultValue) {
		try {
			return getValueBoolean(cfgKey);
		} catch (NoSuchCfgKeyException e) {
			setValue(cfgKey, defaultValue);
			return defaultValue;
		}
	}

	@Override
	public double getValueDouble(String cfgKey) {
		String strVal = getValueString(cfgKey);
		try {
			return Double.parseDouble(strVal);
		} catch (NumberFormatException e) {
			throw new BadValueTypeException(strVal);
		}
	}

	@Override
	public double getValueDouble(String cfgKey, double defaultValue) {
		try {
			return getValueDouble(cfgKey);
		} catch (NoSuchCfgKeyException e) {
			setValue(cfgKey, defaultValue);
			return defaultValue;
		}
	}

	@Override
	public float getValueFloat(String cfgKey) {
		String strVal = getValueString(cfgKey);
		try {
			return Float.parseFloat(strVal);
		} catch (NumberFormatException e) {
			throw new BadValueTypeException(strVal);
		}
	}

	@Override
	public float getValueFloat(String cfgKey, float defaultValue) {
		try {
			return getValueFloat(cfgKey);
		} catch (NoSuchCfgKeyException e) {
			setValue(cfgKey, defaultValue);
			return defaultValue;
		}
	}

	@Override
	public int getValueInt(String cfgKey) {
		String strVal = getValueString(cfgKey);
		try {
			return Integer.parseInt(strVal);
		} catch (NumberFormatException e) {
			throw new BadValueTypeException(strVal);
		}
	}

	@Override
	public int getValueInt(String cfgKey, int defaultValue) {
		try {
			return getValueInt(cfgKey);
		} catch (NoSuchCfgKeyException e) {
			setValue(cfgKey, defaultValue);
			return defaultValue;
		}
	}

	@Override
	public long getValueLong(String cfgKey) {
		String strVal = getValueString(cfgKey);
		try {
			return Long.parseLong(strVal);
		} catch (NumberFormatException e) {
			throw new BadValueTypeException(strVal);
		}
	}

	@Override
	public long getValueLong(String cfgKey, long defaultValue) {
		try {
			return getValueLong(cfgKey);
		} catch (NoSuchCfgKeyException e) {
			setValue(cfgKey, defaultValue);
			return defaultValue;
		}
	}

	@Override
	public String getValueString(String cfgKey) {
		String confVal = confValues.get(cfgKey);
		if (confVal == null) throw new NoSuchCfgKeyException(cfgKey);
		return confVal;
	}

	@Override
	public String getValueString(String cfgKey, String defaultValue) {
		try {
			return getValueString(cfgKey);
		} catch (NoSuchCfgKeyException e) {
			setValue(cfgKey, defaultValue);
			return defaultValue;
		}
	}

	@Override
	public void setValue(String confKey, String confValue) {
		confValues.put(confKey, String.valueOf(confValue));
	}

	@Override
	public void setValue(String confKey, int confValue) {
		setValue(confKey, String.valueOf(confValue));
	}

	@Override
	public void setValue(String confKey, long confValue) {
		setValue(confKey, String.valueOf(confValue));
	}

	@Override
	public void setValue(String confKey, float confValue) {
		setValue(confKey, String.valueOf(confValue));
	}

	@Override
	public void setValue(String confKey, double confValue) {
		setValue(confKey, String.valueOf(confValue));
	}

	@Override
	public void setValue(String confKey, boolean confValue) {
		setValue(confKey, String.valueOf(confValue));
	}

	@Override
	public Map<String, String> getAsMap() {
		return Collections.unmodifiableMap(confValues);
	}

	@Override
	public void loadValsFrom(IConf conf) {
		Map<String, String> map = conf.getAsMap();
		for (Entry<String, String> e : map.entrySet()) {
			this.setValue(e.getKey(), e.getValue());
		}
	}

}
