package de.roo.logging;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

/**
 * 
 * @author Leo Nobach
 *
 */
public abstract class HistoryLog extends ListenableLog {

	public abstract Collection<LogEntry> getEntryHistory();
	
	public abstract void dumpTo(ILog log);
	
	public abstract void writeTo(Writer w) throws IOException;
	
}
