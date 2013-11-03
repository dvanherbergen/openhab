package org.eclipse.smarthome.api.events;

import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * An EventPublisher is used to send commands or status updates to the SmartHome event bus.
 */
public interface EventPublisher {

	/**
	 * Initiate asynchronous sending of a command.
	 * This method returns immediately to the caller.
	 * 
	 * @param itemName name of the item to send the command for
	 * @param command the command to send
	 */
	public void postCommand(String itemName, Command command);

	/**
	 * Initiate asynchronous sending of a status update.
	 * This method returns immediately to the caller.
	 * 
	 * @param itemName name of the item to send the update for
	 * @param newState the new state to send
	 */
	public void postUpdate(String itemName, State newState);
		
}
