package org.eclipse.smarthome.node.runtime.internal.events;

import org.eclipse.smarthome.core.events.AbstractEventBus;
import org.eclipse.smarthome.core.events.types.SystemEvent;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Mqtt Eventbus. Used for linking SmartHome nodes to the main SmartHome
 * runtime.
 * 
 * @author Davy Vanherbergen
 */
public class MqttEventBus extends AbstractEventBus {

	
	/**
	 * Start Mqtt connection and setup default listeners for states and commands.
	 */
	public void activate() {
		
	}
	
	
	@Override
	public void sendCommand(String itemName, Command command) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postSystemEvent(SystemEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCommand(String itemName, Command command) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postUpdate(String itemName, State newState) {
		// TODO Auto-generated method stub

	}

}
