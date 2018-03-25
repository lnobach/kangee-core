package de.roo.util.resourceAccess;

import java.io.IOException;
import java.io.InputStream;

public interface IResource {

	public String getAbsolutePath();
	
	public String getSimpleName();
	
	public InputStream getResourceAsStream() throws IOException;
	
}
