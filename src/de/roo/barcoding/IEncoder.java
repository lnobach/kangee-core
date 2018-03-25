package de.roo.barcoding;

/**
 * Access to a barcode encoder needed for Kangee's barcode functionality
 * to work correctly.
 * 
 * @author Leo Nobach
 *
 */
public interface IEncoder {

	public IBarcodeImage encode(String str, int prefX, int prefY);
	
}
