/**
 * 
 */
package de.roo.engine.setup;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface ISetupFollower {
	
	void setCurrentJob(String status);
	
	//void makeProblemDialog(String dialog);
	
	//void makeProblemDialog(String dialog, Throwable ex);
	
	void setupStarted();

	void setupFinished();

	void setupFailed(SetupException e);
	
}