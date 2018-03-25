package de.roo.logging;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * 
 * @author Leo Nobach
 *
 */
public class LogUncaughtExceptionHandler implements UncaughtExceptionHandler {

	private ILog log;
	private boolean warnInsteadOfError;
	static final String MSG = "Uncaught exception occured.";

	public LogUncaughtExceptionHandler(ILog log, boolean warnInsteadOfError) {
		this.log = log;
		this.warnInsteadOfError = warnInsteadOfError;
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		if (warnInsteadOfError)
			log.warn(t, MSG, e);
		else
			log.error(t, MSG, e);
	}
	
}
