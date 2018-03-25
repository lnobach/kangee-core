package de.roo.engine;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import de.roo.logging.ILog;

/**
 * 
 * @author Leo Nobach
 *
 */
public class Locker {

	private File lockFile;
	private FileChannel chan;
	private FileLock lock;

	public Locker(File basedir) {
		this.lockFile = new File(basedir, "kangee.lock");
	}
	
	/**
	 * Returns whether the file could be locked -> application is singleton.
	 * @param log
	 * @return
	 */
	public boolean tryLock(ILog log) {
		try {
			FileChannel chan = new RandomAccessFile(lockFile, "rw").getChannel();
			FileLock lock = chan.tryLock();
			return lock != null;
		} catch (IOException e) {
			log.error(this, "Error occurred while trying to lock file.");
			return false;
		}
		
	}
	
	/**
	 * Returns whether the file was previously locked by the app.
	 * @param log
	 * @return
	 */
	public boolean unlock(ILog log) {
		if (chan == null || lock == null) return false;
		try {
			lock.release();
			chan.close();
			lockFile.delete();
			return true;
		} catch (IOException e) {
			log.warn(this, "Error occured while releasing file lock.");
			return false;
		}
	}
	
}
