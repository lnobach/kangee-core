package de.roo.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.roo.util.resourceAccess.IResource;
import de.roo.util.resourceAccess.IResourceManager;

public class FilesystemResourceManager implements IResourceManager {

	private File root;

	public FilesystemResourceManager(File root) {
		this.root = root;
	}

	@Override
	public List<IResource> getAllResourcesInDirName(String dirName) {
		
		File dir2list = new File(root, dirName);
		if (!dir2list.exists() || !dir2list.isDirectory()) return null;
		List<IResource> files = new ArrayList<IResource>(100);
		listTree(dir2list, files);
		return files;
		
	}
	
	private void listTree(File dir, List<IResource> files) {
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) listTree(f, files);
			else if (f.isFile()) files.add(new ResourceImpl(f));
		}
	}

	class ResourceImpl implements IResource {

		private File f;

		public ResourceImpl(File f) {
			this.f = f;
		}

		@Override
		public String getAbsolutePath() {
			return f.getAbsolutePath();
		}

		@Override
		public String getSimpleName() {
			return f.getName();
		}

		@Override
		public InputStream getResourceAsStream() throws IOException {
			return new FileInputStream(f);
		}
		
	}

}
