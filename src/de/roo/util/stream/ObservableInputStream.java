package de.roo.util.stream;

import java.io.InputStream;

/**
 * 
 * @author Leo Nobach
 *
 */
public abstract class ObservableInputStream extends InputStream {
	
	public abstract IStreamObserver getNewStreamObserver();
	
}
