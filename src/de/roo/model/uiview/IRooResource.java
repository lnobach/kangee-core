package de.roo.model.uiview;

import java.io.File;
import java.util.List;

public interface IRooResource {

	/**
	 * May be null
	 * @return
	 */
	public abstract IRooModel getParent();

	public abstract File getFile();

	public abstract String getIdentifier();

	public abstract void removeLoad(IAbstractLoad upload);

	public abstract void addResourceListener(IResourceListener l);

	public abstract void removeResourceListener(IResourceListener l);

	public abstract int getNumLoads();

	public abstract IAbstractLoad getLoadAt(int index);

	public abstract List<? extends IAbstractLoad> getLoadsList();

	public abstract String getHttpFileName();

	public abstract String getMIMEType();
	
	public abstract String getPlainFileName();
	
	public interface IResourceListener {
		
		/**
		 * All resource changes EXCEPT adding and removing of uploads
		 * @param resource
		 */
		public void resourceChanged(IRooResource resource);
		
		public void resourceRemoved(IRooResource resource);

		public void loadAdded(IRooResource res, IAbstractLoad upl, int idx);
		
		public void loadRemoved(IRooResource res, IAbstractLoad upl, int idx);
		
	}

}