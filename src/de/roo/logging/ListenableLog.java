package de.roo.logging;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author Leo Nobach
 *
 */
public class ListenableLog extends ObjectLog {

	@Override
	void addLogObj(LogEntry e) {
		for (ILogListener l : listeners) l.newEntryArrived(e, this);
	}
	
	public static interface ILogListener {
		public void newEntryArrived(LogEntry e, ListenableLog log);
	}
	
	public List<ILogListener> listeners = new LinkedList<ILogListener>();
	
	public void addListener(ILogListener l) {
		listeners.add(l);
	}
	
	public void removeListener(ILogListener l) {
		listeners.remove(l);
	}

}
