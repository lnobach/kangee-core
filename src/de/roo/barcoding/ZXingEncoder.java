package de.roo.barcoding;

import java.util.Hashtable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.ByteMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * Access stub for the ZXing barcode encoder.
 * 
 * @author Leo Nobach
 *
 */
public class ZXingEncoder implements IEncoder {

	@Override
	public IBarcodeImage encode(String str, int prefX, int prefY) {
		ErrorCorrectionLevel errorCorrectionLevel = ErrorCorrectionLevel.L;
		
		Hashtable<Object, Object> hints = new Hashtable<Object, Object>();
		hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
	
		QRCodeWriter writer = new QRCodeWriter();
		try {
			ByteMatrix result =  writer.encode(str, BarcodeFormat.QR_CODE, prefX, prefY, hints);
			return new BarcodeImageImpl(result);
		} catch (WriterException e) {
			throw new BarcodeEncodingException();
		}
	}
	
	public class BarcodeImageImpl implements IBarcodeImage {

		private ByteMatrix bm;

		public BarcodeImageImpl(ByteMatrix bm) {
			this.bm = bm;
		}
		
		@Override
		public int getHeight() {
			return bm.getHeight();
		}

		@Override
		public int getWidth() {
			return bm.getWidth();
		}

		@Override
		public boolean isBlack(int i, int j) {
				return bm.get(i, j) >= 0;
		}

		@Override
		public String getErrorMsg() {
			return null;
		}
		
	}
	
}
