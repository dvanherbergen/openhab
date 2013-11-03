package org.eclipse.smarthome.core.events.types;

import org.eclipse.smarthome.api.binding.Binding;

public class BindingStatusEvent extends AbstractBindingEvent {

	public static enum Status {
		NEW,
		PROPERTIES_LOADED,
		ITEMS_LOADED,
		REMOVED;
	}

	public BindingStatusEvent(Binding binding, Status bindingStatus) {
		this.bindingType = binding.getBindingType();
		this.value = bindingStatus.toString();
	}
	
}
