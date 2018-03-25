package de.roo.themes.candy;

import de.roo.themes.ITheme.ThemeDesc;
import de.roo.themes.common.SimpleStaticTemplateTheme;

/**
 * 
 * @author Leo Nobach
 *
 */
@ThemeDesc(name="Candy Kangee", author="Leo Nobach", desc="A colorful theme", key="candy")
public class Candy extends SimpleStaticTemplateTheme {
	
	public Candy() {
		this.addResource("candy-button.png");
		this.addResource("candy-tile.png");
	}
	
	@Override
	protected String getStaticRootTemplate(TemplateType type) {
		if (type == TemplateType.Download) return "candyDL.html";
		if (type == TemplateType.Upload) return "candyUL.html";
		return "candyStatus.html";
	}

	@Override
	protected int getFilenameMaxChars() {
		return 38;
	}

	@Override
	protected String getPreviewImageFilename() {
		return "candy-preview.png";
	}
	
}
