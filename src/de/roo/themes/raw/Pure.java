package de.roo.themes.raw;

import de.roo.themes.ITheme.ThemeDesc;
import de.roo.themes.common.SimpleStaticTemplateTheme;

/**
 * 
 * @author Leo Nobach
 *
 */
@ThemeDesc(name="Pure Kangee", author = "Leo Nobach", 
		desc = "A pure design without any images or extra content", key = "pure")
public class Pure extends SimpleStaticTemplateTheme {

	@Override
	protected int getFilenameMaxChars() {
		return 40;
	}

	@Override
	protected String getStaticRootTemplate(TemplateType type) {
		if (type == TemplateType.Download) return "pureDL.html";
		if (type == TemplateType.Upload) return "pureUL.html";
		else return "pureStatus.html";
	}

	@Override
	protected String getPreviewImageFilename() {
		return "pure-preview.png";
	}
	
}
