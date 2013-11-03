package org.openhab.core.binding;

import java.util.List;
import java.util.Properties;

import org.eclipse.smarthome.api.binding.BindingConfigException;
import org.eclipse.smarthome.api.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

public abstract class AbstractBinding<T extends BindingProvider> {

	// TODO only still exists here to stop compile errors...
	
	
	protected EventPublisher eventPublisher;
	
	protected List<T> providers;
	
	public String getBindingType() {
		// TODO Auto-generated method stub
		return null;
	}

	public void processCommand(String itemName, Command command) {
		internalReceiveCommand(itemName, command);
	}

	public void internalReceiveUpdate(String itemName, State state) {
		
	}
	
	protected abstract void internalReceiveCommand(String itemName, Command command);
	
	public void processUpdate(String itemName, State state) {
		internalReceiveUpdate(itemName, state);		
	}
	

	public void processBindingProperties(Properties config) throws BindingConfigException {
			
	}

	public void processItemConfig(Item item, String itemConfig) throws BindingConfigException {
		// TODO Auto-generated method stub
		
	}

	public void setEventPublisher(EventPublisher publisher) {
		// TODO Auto-generated method stub
		
	}
	
}
