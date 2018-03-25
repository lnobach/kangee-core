package de.roo.barcoding;

/**
 * 
 * Barcode image as a result from an arbitary barcode generator.
 * Interface reduced to a boolean matrix.
 * 
 * @author Leo Nobach
 *
 */
public interface IBarcodeImage {

	/**
	 * Returns null if there was no error. Otherwise, a displaying 
	 * component should display the error msg.
	 * @return
	 */
	public String getErrorMsg();
	
	/**
	 * Returns the height of the boolean matrix of this barcode.
	 * @return
	 */
	public int getHeight();
	
	/**
	 * Returns the width of the boolean matrix of this barcode.
	 * @return
	 */
	public int getWidth();
	
	/**
	 * Returns whether the field (i, j) of the boolean matrix is black.
	 * @param i
	 * @param j
	 * @return
	 */
	public boolean isBlack(int i, int j);
	
}
