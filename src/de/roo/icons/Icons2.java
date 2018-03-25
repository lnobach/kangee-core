package de.roo.icons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.roo.logging.ILog;
import de.roo.util.filechecking.FileFormatChecker;
import de.roo.util.resourceAccess.IResource;
import de.roo.util.resourceAccess.IResourceManager;
import de.roo.util.stream.StreamCopy;

/**
 * 
 * @author Leo Nobach
 *
 */
public class Icons2 {
	
	static final Set<String> allowedImageMimeTypes = new HashSet<String>();
	static final Set<String> allowedImageExts = new HashSet<String>();
	
	public static final int S_16x16 = 0;
	public static final int S_22x22 = 1;
	public static final int S_32x32 = 2;
	public static final int S_48x48 = 3;
	public static final int S_64x64 = 4;
	
	static {
		allowedImageMimeTypes.add("image/gif");
		allowedImageMimeTypes.add("image/png");
		allowedImageExts.add("gif");
		allowedImageExts.add("png");
	}
	
	FileFormatChecker c = new FileFormatChecker();
	Map<String, RooMultiScaleIconImpl> allIcons = new HashMap<String, RooMultiScaleIconImpl>();
	private ILog log;
	RooMultiScaleIconImpl fallbackIcon = new RooMultiScaleIconImpl();
	RooMultiScaleIconImpl emptyIcon = new RooMultiScaleIconImpl();
	
	public Icons2(IResourceManager prov, ILog log) {
		this.log = log;
		
		fallbackIcon.setIcon(S_48x48, new RooIconImpl(new ClassLoaderStreamResource("default48x48.png"), "image/png"));
		emptyIcon.setIcon(S_48x48, new RooIconImpl(new ClassLoaderStreamResource("empty48x48.png"), "image/png"));
		
		generateSize(S_16x16, "16x16", prov);
		generateSize(S_22x22, "22x22", prov);
		generateSize(S_32x32, "32x32", prov);
		generateSize(S_48x48, "48x48", prov);
		generateSize(S_64x64, "64x64", prov);
		
	}
	
	private void generateSize(int sizeCode, String string, IResourceManager prov) {
		List<IResource> resources = prov.getAllResourcesInDirName(string);
		if (resources == null) return;
		for (IResource res : resources) {
			handleIcon(res, sizeCode);
		}
	}
	
	public IRooMultiScaleIcon getIconForFile(File f) {
		RooMultiScaleIconImpl result;
		if (f == null ) {
			result = allIcons.get("empty");
			if (result != null) return result;
			return emptyIcon;
		}
		String fName = f.getName();
		String ext = fName.substring(fName.lastIndexOf(".")+1).toLowerCase();
		result = allIcons.get(ext);
		if (result != null) return result;
		result = allIcons.get("default");
		if (result != null) return result;	
		return fallbackIcon;
	}
	
	private void handleIcon(IResource res, int sizeCode) {
		
		String imageMimeType;
		try {
			imageMimeType = c.getMIME(res.getResourceAsStream());
		} catch (IOException e) {
			log.warn(this, "Could not check magic code of resource " + res + ".", e);
			return;
		}
		if (!allowedImageMimeTypes.contains(imageMimeType)) {
			log.warn(this, imageMimeType + " is not a valid MIME type for a Roo icon.");
			return;
		}
		
		String resName = res.getSimpleName();
		
		int resNameSepPos = resName.lastIndexOf(".");
		if (resNameSepPos <= 0) {
			log.warn(this, resName + ": This file does not have an extension.");
			return;
		}
		String ext = resName.substring(resNameSepPos+1);
		String fNamePure = resName.substring(0, resNameSepPos);
		if (!allowedImageExts.contains(ext)) {
			log.warn(this, resName + ": Image file extension '" + ext + "' is not allowed.");
			return;
		}
		String[] regExts = fNamePure.split("-");

		for (String regExt : regExts) {
			regExt = regExt.toLowerCase();
			//log.dbg(this, "Mapping " + resName + " to file extension " + regExt);
			
			assert (res != null);
			RooMultiScaleIconImpl icon = allIcons.get(regExt);
			if (icon == null) {
				icon = new RooMultiScaleIconImpl();
				allIcons.put(regExt, icon);
			}
			RooIconImpl conflictIcon = icon.getIcon(sizeCode);
			if (conflictIcon != null) log.warn(this, "Icon Conflict: " + regExt + " already assigned from " + conflictIcon.getResource().getSimpleName()
					+ " while trying to assign " + res.getSimpleName());
			icon.setIcon(sizeCode, new RooIconImpl(res, imageMimeType));
		}
		
	}

	public String getSizeString(int sizeCode) {
		switch (sizeCode) {
			case S_16x16: return "16x16";
			case S_22x22: return "22x22";
			case S_32x32: return "32x32";
			case S_48x48: return "48x48";
			case S_64x64: return "64x64";
		}
		throw new IllegalArgumentException("Illegal size code");
	}

	static class RooMultiScaleIconImpl implements IRooMultiScaleIcon {
		
		public RooIconImpl[] icons = new RooIconImpl[5];
		
		public void setIcon(int sizeCode, RooIconImpl iconRes) {
			this.icons[sizeCode] = iconRes;
		}
		
		public RooIconImpl getIcon(int sizeCode) {
			return icons[sizeCode];
		}
		
		public RooIconImpl getIconSmallerEqual(int sizeCode) {
			while (sizeCode >= S_16x16) {
				if (icons[sizeCode] != null) return icons[sizeCode];
				sizeCode--;
			}
			return null;
		}
			
	}
	
	class RooIconImpl implements IRooIcon {

		private IResource res;
		byte[] bytes = null;
		private String imageMimeType;
		
		public RooIconImpl(IResource res, String imageMimeType) {
			this.res = res;
			this.imageMimeType = imageMimeType;
		}
		
		@Override
		public InputStream getIconAsStream() throws IOException {
			return res.getResourceAsStream();
		}

		@Override
		public byte[] getBytes() {
			if (bytes == null) {
				try {
					bytes = StreamCopy.getByteArrayFromInputStream(res.getResourceAsStream());
				} catch (IOException e) {
					log.warn(this, "Could not read icon " + res.getSimpleName() + ".", e);
				}
			}
			return bytes;
		}

		@Override
		public String getImageMimeType() {
			return imageMimeType;
		}

		public IResource getResource() {
			return res;
		}
		
	}
	
	static class ClassLoaderStreamResource implements IResource {
		
		private String res;

		public ClassLoaderStreamResource(String res) {
			this.res = res;
		}
		
		@Override
		public String getAbsolutePath() {
			return null;
		}
	
		@Override
		public String getSimpleName() {
			return res;
		}
	
		@Override
		public InputStream getResourceAsStream() throws IOException {
			return Icons2.class.getResourceAsStream(res);
		}
		
	}
	
}
