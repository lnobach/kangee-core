package de.roo.themes.common;

import de.roo.BuildConstants;
import de.roo.configuration.IConf;
import de.roo.model.RooDownloadResource;
import de.roo.model.RooResource;
import de.roo.model.RooUploadResource;
import de.roo.model.uiview.IRooDownloadResource;
import de.roo.model.uiview.IRooDownloadResource.DLState;
import de.roo.util.FileUtils;
import de.roo.util.NumberFormatToolkit;

/**
 * 
 * @author Leo Nobach
 *
 */
public abstract class SimpleStaticTemplateTheme extends StaticTemplateTheme {

	@Override
	protected String getVariableContent(String variableID, RooResource res, TemplateType type, IConf conf) {
		
		if (type != TemplateType.Download) {
			if (variableID.equalsIgnoreCase("MIMEType")) {
				return res.getMIMEType();
			} if (variableID.equalsIgnoreCase("FileNameShortened")) {
				if (res.getPlainFileName() == null) return "No file";
				if (getFilenameMaxChars() <= 0) return res.getPlainFileName();
				return FileUtils.shortenFileName(res.getPlainFileName(), getFilenameMaxChars());
			} if (variableID.equalsIgnoreCase("FileName"))
				return res.getPlainFileName();
			if (variableID.equalsIgnoreCase("Size")) {
				if (res.getFile() == null) return "n/a";
				return NumberFormatToolkit.formatSIPrefix(res.getFile().length(),
						1, true) + "B";
			}
		}
		if (res instanceof RooUploadResource) {
			RooUploadResource upRes = (RooUploadResource) res;
			if (variableID.equalsIgnoreCase("loadPath"))
				return "/" + upRes.getIdentifier() + "/" + upRes.getHttpFileName();
		} else if (res instanceof RooDownloadResource && type == TemplateType.Status) {
			IRooDownloadResource dlRes = (IRooDownloadResource)res;
			if (variableID.equalsIgnoreCase("StatusDesc")) {
				DLState state = dlRes.getDLState();
				if (state == DLState.FileUploaded) return "The file was successfully uploaded";
				if (state == DLState.FileUploadError) return "There was an error while the file was uploaded.";
				if (dlRes.downloadBlocked()) return "A file is currently being uploaded.";
				return "No file was uploaded yet.";
			}
		}

		if (variableID.equalsIgnoreCase("WelcomeText")) {
			return getWelcomeText(conf, res);
		}
		if (variableID.equalsIgnoreCase("about"))
			return BuildConstants.PROD_FULL_NAME + " v" + BuildConstants.PROD_VER + " <a href=\"" + BuildConstants.PROD_URL + "\">" + BuildConstants.PROD_URL + "</a>";
		return null;
	}

	protected String getWelcomeText(IConf conf, RooResource res) {
		String nickname = conf.getValueString("Nickname", "");
		boolean hasNoNickname = "".equals(nickname.trim());
		boolean isDownload = res instanceof RooDownloadResource;
		
		String welcomeText = conf.getValueString("WelcomeText", "");
		
		if (!"".equals(welcomeText.trim())) return welcomeText;
		
		if (hasNoNickname && isDownload) return "A friend wants to get a file from you!";
		if (!hasNoNickname && isDownload) return "<b>" + nickname + "</b> wants to get a file from you!";
		if (hasNoNickname && !isDownload) return "A friend has a file for you!";
		return "<b>" + nickname + "</b> has a file for you!";
		
		
		
	}

	protected abstract int getFilenameMaxChars();
	
	

}
