package de.roo.configuration;

/**
 * 
 * @author Leo Nobach
 *
 */
public class BadValueTypeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6787312180008906912L;
	private String valueAsStr;
	
	public BadValueTypeException(String valueAsStr) {
		super("Bad Value Type: " + valueAsStr);
		this.valueAsStr = valueAsStr;
		
	}
	
	public String getValueAsStr() {
		return valueAsStr;
	}

}
