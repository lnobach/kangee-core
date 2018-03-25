package de.roo.engine.setup;

/**
 * 
 * @author Leo Nobach
 *
 */
public interface ISetupMethod {

	public void setup(ISetupContext ctx, ISetupFollower follower) throws SetupException;
	
}
