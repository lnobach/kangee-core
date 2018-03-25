package de.roo.themes;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.roo.configuration.IConf;
import de.roo.model.RooResource;
import de.roo.srvApi.IResponseFactory;
import de.roo.srvApi.ServerException;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface ITheme {
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ThemeDesc {
		String key();
		String name();
		String author();
		String desc();
	}
	
	enum TemplateType {
		Download,
		Upload,
		Status;
	}

	public void answerTemplateResource(String resourceName, IResponseFactory f) throws IOException, ServerException;

	public void answerStartDocumentFor(RooResource res, IResponseFactory f, TemplateType type, IConf conf) throws IOException, ServerException;

	public InputStream getPreviewImage();
	
}
