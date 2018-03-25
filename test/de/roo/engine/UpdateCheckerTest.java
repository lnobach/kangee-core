package de.roo.engine;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;

import de.roo.engine.UpdateChecker.UpdateInfo;
import de.roo.logging.ConsoleLog;
import de.roo.logging.ILog;
import de.roo.util.VersionInfo;

public class UpdateCheckerTest {

	static final URL updateDownloadURL;
	
	static {
		try {
			updateDownloadURL = new URL("http://getkangee.com/getkangee");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void testUpdateInfoMinorRevs() {
		UpdateInfo info = new UpdateInfo(new VersionInfo(16, 4), new VersionInfo(16, 3), updateDownloadURL);
		Assert.assertEquals(info.shouldUpdate(), true);
		
		UpdateInfo info2 = new UpdateInfo(new VersionInfo(16, 3), new VersionInfo(16, 3), updateDownloadURL);
		Assert.assertEquals(info2.shouldUpdate(), false);
		
		UpdateInfo info3 = new UpdateInfo(new VersionInfo(16, 2), new VersionInfo(16, 3), updateDownloadURL);
		Assert.assertEquals(info3.shouldUpdate(), false);
	}
	
	@Test
	public void testUpdateInfoMajorRevs() {
		UpdateInfo info = new UpdateInfo(new VersionInfo(16, 0), new VersionInfo(15, 0), updateDownloadURL);
		Assert.assertEquals(info.shouldUpdate(), true);
		
		UpdateInfo info2 = new UpdateInfo(new VersionInfo(15, 0), new VersionInfo(15, 0), updateDownloadURL);
		Assert.assertEquals(info2.shouldUpdate(), false);
		
		UpdateInfo info3 = new UpdateInfo(new VersionInfo(14, 0), new VersionInfo(15, 0), updateDownloadURL);
		Assert.assertEquals(info3.shouldUpdate(), false);
	}
	
	@Test
	public void testUpdateInfoVarious() {
		UpdateInfo info = new UpdateInfo(new VersionInfo(16, 3), new VersionInfo(15, 2), updateDownloadURL);
		Assert.assertEquals(info.shouldUpdate(), true);
		
		UpdateInfo info2 = new UpdateInfo(new VersionInfo(16, 1), new VersionInfo(15, 5), updateDownloadURL);
		Assert.assertEquals(info2.shouldUpdate(), true);
		
		UpdateInfo info3 = new UpdateInfo(new VersionInfo(13, 3), new VersionInfo(14, 2), updateDownloadURL);
		Assert.assertEquals(info3.shouldUpdate(), false);
		
		UpdateInfo info4 = new UpdateInfo(new VersionInfo(16, 3), new VersionInfo(15, 23), updateDownloadURL);
		Assert.assertEquals(info4.shouldUpdate(), true);
		
		UpdateInfo info5 = new UpdateInfo(new VersionInfo(16, 1), new VersionInfo(15, 2), updateDownloadURL);
		Assert.assertEquals(info5.shouldUpdate(), true);
		
		UpdateInfo info6 = new UpdateInfo(new VersionInfo(0, 0), new VersionInfo(1, 0), updateDownloadURL);
		Assert.assertEquals(info6.shouldUpdate(), false);
	}
	
	@Test
	public void testUpdateRequestFromServer() throws UpdateCheckException {
		UpdateChecker chk = new UpdateChecker("http://getkangee.com/api/updateTest.xml", "http://getkangee.com/getkangee");
		ILog log = new ConsoleLog();
		UpdateInfo info = chk.checkStrict(log);
		System.out.println("Update info generated: " + info);
	}
	
	
}
