package de.roo.util;

/**
 * 
 * @author Leo Nobach
 *
 */
public class NumberFormatToolkit {

	public static String formatPercentage(double quota, int decimals) {
		return floorToDecimals(quota * 100d, decimals) + "%";
	}

	public static double floorToDecimals(double value, int decimals) {
		double pow = Math.pow(10, decimals);
		return (Math.rint(value * pow)) / pow;
	}

	public static String floorToDecimalsString(double value, int decimals) {
		return String.valueOf(floorToDecimals(value, decimals));
	}
	
	public static String formatSIPrefix(final double value, int decimals, boolean use1024) {
		
		char[] siPrefixes = {'k','M','G','T','P','E','Z','Y'};
		
		int divisor = use1024?1024:1000;
		
		double valTemp = value;
		
		int i = 0;
		while (valTemp > divisor && i < siPrefixes.length) {
		 valTemp /= divisor;
		 i++;
		}
		if (i == 0) return String.valueOf((int)value);
		return floorToDecimalsString(valTemp, decimals) + (i==0?"":siPrefixes[i-1]);
		
		
	}

}
