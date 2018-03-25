package de.roo.themes.kangaroo;

import de.roo.themes.ITheme.ThemeDesc;
import de.roo.themes.common.SimpleStaticTemplateTheme;

@ThemeDesc(name="Kangaroo", author="Leo Nobach", desc="A kangaroo jumps by", key="kangaroo")
public class Kangaroo extends SimpleStaticTemplateTheme {

	public Kangaroo() {
		this.addResource("kangaroo.js");
		this.addResource("style.css");
		this.addResource("roo.jpg");
	}
	
	@Override
	protected String getStaticRootTemplate(TemplateType type) {
		if (type == TemplateType.Download) return "kangarooDL.html";
		if (type == TemplateType.Upload) return "kangarooUL.html";
		return "kangarooStatus.html";
	}

	@Override
	protected int getFilenameMaxChars() {
		return 30;
	}

	@Override
	protected String getPreviewImageFilename() {
		return "kangaroo-preview.png";
	}
	
}
