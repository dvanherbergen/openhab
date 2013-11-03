package org.eclipse.smarthome.core.events.types;


public class AbstractBindingEvent extends SystemEvent {

	protected String bindingType;

	public String getBindingType() {
		return bindingType;
	}
	
	
}
