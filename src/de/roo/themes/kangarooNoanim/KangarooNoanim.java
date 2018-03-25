package de.roo.themes.kangarooNoanim;

import de.roo.themes.ITheme.ThemeDesc;
import de.roo.themes.common.SimpleStaticTemplateTheme;

@ThemeDesc(name="Kangaroo (No Animation)", author="Leo Nobach", desc="A kangaroo", key="kangarooNoanim")
public class KangarooNoanim extends SimpleStaticTemplateTheme {

	public KangarooNoanim() {
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
