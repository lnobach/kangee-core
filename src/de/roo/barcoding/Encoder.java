package de.roo.barcoding;

import de.roo.configuration.IConf;
import de.roo.logging.ILog;

/**
 * 
 * Static class which can be used to access the barcode generator in an abstract
 * way. No NoClassDefFoundError will be thrown if the class can not be accessed.
 * 
 * @author Leo Nobach
 *
 */
public class Encoder {
	
	/**
	 * Encodes a given string to a barcode image, given presets found in conf.
	 * @param str
	 * @param log
	 * @param conf
	 * @return
	 */
	public static IBarcodeImage encode(String str, ILog log, int barcodeSizeX, int barcodeSizeY) {
		
		try {
			IEncoder encoder2use = new ZXingEncoder();
			return encoder2use.encode(str, barcodeSizeX, barcodeSizeY);
		} catch (NoClassDefFoundError e) {
			log.warn(Encoder.class, "No implementation of a Barcode encoder was found."
					+ " This feature will be missing.", e);
			return null;
		}
		
	}
	
}
