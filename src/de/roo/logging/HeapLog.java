package de.roo.logging;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 
 * @author Leo Nobach
 *
 */
public class HeapLog extends HistoryLog {

	Object treeLock = new Object();
	
	int capacity;
	Queue<LogEntry> q;

	public HeapLog(int capacity) {
		this.capacity = capacity;
		q = new ArrayBlockingQueue<LogEntry>(capacity);
	}
	
	@Override
	void addLogObj(LogEntry e) {
		synchronized(treeLock) {
			if (q.size() >= capacity) q.remove();
			q.add(e);
			super.addLogObj(e);
		}
	}
	
	@Override
	public Collection<LogEntry> getEntryHistory() {
		synchronized(treeLock) {
			return Collections.unmodifiableCollection(q);
		}
	}
	
	@Override
	public void dumpTo(ILog log) {
		synchronized(treeLock) {
			for (LogEntry e : q) e.writeTo(log);
		}
	}

	@Override
	public void writeTo(Writer w) throws IOException {
		for (LogEntry e : getEntryHistory()) {
			w.write(e.toString() + "\n");
		}
	}
	
}
