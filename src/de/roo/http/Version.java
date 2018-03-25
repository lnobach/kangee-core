package de.roo.http;

/**
 * 
 * @author Leo Nobach
 *
 */
public class Version {
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + rev;
		result = prime * result + ver;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Version other = (Version) obj;
		if (rev != other.rev)
			return false;
		if (ver != other.ver)
			return false;
		return true;
	}

	int ver;
	int rev;
	
	/**
	 * Default Version: HTTP/1.0
	 */
	public Version() {
		ver = 1;
		rev = 0;
	}
	
	/**
	 * Returns null if the version is not supported
	 * @param version
	 */
	public Version(String versionStr) {
		if ("HTTP/1.0".equals(versionStr)) {
			this.ver = 1;
			this.rev = 0;
		}
		else if ("HTTP/1.1".equals(versionStr)) {
			this.ver = 1;
			this.rev = 1;
		}
		else throw new IllegalArgumentException(versionStr + " is not a valid HTTP version");
	}
	
	public String toString() {
		return "HTTP/" + ver + "." + rev;
	}
}
