package de.roo.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * 
 * @author Leo Nobach
 *
 */
public abstract class ObjectLog implements ILog {

	@Override
	public void dbg(Object src, Object msg) {
		addLogObj(new LogEntry(LogType.Debug, src, msg));
	}

	@Override
	public void error(Object src, Object msg) {
		addLogObj(new LogEntry(LogType.Error, src, msg));
	}

	@Override
	public void error(Object src, Object msg, Throwable ex) {
		addLogObj(new LogEntry(LogType.Error, src, msg, ex));
	}

	@Override
	public void warn(Object src, Object msg) {
		addLogObj(new LogEntry(LogType.Warn, src, msg));
	}
	
	@Override
	public void warn(Object src, Object msg, Throwable ex) {
		addLogObj(new LogEntry(LogType.Warn, src, msg, ex));
	}
	
	abstract void addLogObj(LogEntry e);
	
	public enum LogType {
		Debug,
		Warn,
		Error;
	}
	
public static class LogEntry {
		
		private Object message;
		private Object source;
		private Throwable ex;
		private LogType type;
		
		public Object getMessage() {
			return message;
		}

		public Object getSource() {
			return source;
		}
		
		public Throwable getException() {
			return ex;
		}
		
		public LogType getType() {
			return type;
		}
		
		public LogEntry(LogType type, Object source, Object message) {
			this(type, source, message, null);
		}
		
		public LogEntry(LogType type, Object source, Object message, Throwable ex) {
			this.type = type;
			this.source = source;
			this.message = message;
			this.ex = ex;
		}
		
		public void writeTo(ILog log) {
			if (type == LogType.Warn) {
				if (ex == null) log.warn(source, message);
				else log.warn(source, message, ex);
			} else if (type == LogType.Debug) {
				log.dbg(source, message);
			} else {
				if (ex == null) log.error(source, message);
				else log.error(source, message, ex);
			}
		}
		
		public String toString() {
			StringBuilder b = new StringBuilder();
			
			if (type == LogType.Warn) b.append("WRN");
			else if (type == LogType.Debug) b.append("DBG");
			else b.append("ERR");
			
			b.append(": ");
			b.append(message);
			
			if (ex != null) {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				ex.printStackTrace(new PrintStream(os));
				b.append("\n");
				b.append(os);
			}
			return b.toString();
		}
		
	}
	
}
