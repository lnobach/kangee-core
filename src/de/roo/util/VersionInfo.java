package de.roo.util;

public class VersionInfo implements Comparable<VersionInfo>{

	int majorRev;
	int minorRev;
	
	public VersionInfo(int majorRev, int minorRev) {
		super();
		this.majorRev = majorRev;
		this.minorRev = minorRev;
	}


	public int getMinorRev() {
		return minorRev;
	}

	
	public int getMajorRev() {
		return majorRev;
	}

	public String toString() {
		return majorRev + "." + minorRev;
	}

	@Override
	public int compareTo(VersionInfo other) {
		if (this.majorRev == other.majorRev) return this.minorRev - other.minorRev;
		return this.majorRev - other.majorRev;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + majorRev;
		result = prime * result + minorRev;
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
		VersionInfo other = (VersionInfo) obj;
		if (majorRev != other.majorRev)
			return false;
		if (minorRev != other.minorRev)
			return false;
		return true;
	}
	
}
