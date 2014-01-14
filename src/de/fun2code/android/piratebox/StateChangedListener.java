package de.fun2code.android.piratebox;

/**
 * State changed listener interface
 * 
 * @author joschi
 *
 */
public interface StateChangedListener {
	/**
	 * Is called when the access point was enabled
	 * 
	 * @param isAutomatic	@{code true} if the access point was started by the app,
	 * 						otherwise {@code false}
	 */
	public void apEnabled(boolean isAutomatic);
	
	/**
	 * Is called when the access point was disabled
	 * 
	 * @param isAutomatic	@{code true} if the access point was started by the app,
	 * 						otherwise {@code false}
	 */
	public void apDisabled(boolean isAutomatic);
	
	/**
	 * Is called when the network has been configured as PirateBox
	 */
	public void networkUp();
	
	/**
	 * Is called when the non-PirateBox network configuration has been restored 
	 */
	public void networkDown();
	
	/**
	 * Called when the web server has been started
	 * 
	 * @param success	state of the web server, {@code true} if the server is
	 * 					stopped, otherwiese {@code false}
	 */
	public void serverUp(boolean success);
	
	/**
	 * Called when the web server has been stopped
	 * 
	 * @param success	state of the web server, {@code true} if the server is
	 * 					stopped, otherwiese {@code false}
	 */
	public void serverDown(boolean success);
}
