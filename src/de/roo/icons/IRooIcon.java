package de.roo.icons;

import java.io.IOException;
import java.io.InputStream;

public interface IRooIcon {

	InputStream getIconAsStream() throws IOException;

	byte[] getBytes();

	String getImageMimeType();

}
