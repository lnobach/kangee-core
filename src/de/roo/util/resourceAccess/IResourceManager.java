package de.roo.util.resourceAccess;

import java.util.List;

public interface IResourceManager {
	
	/**
	 * Returns null if name is not a valid directory
	 * @param name
	 * @return
	 */
	public List<IResource> getAllResourcesInDirName(String dirName);
	
}
