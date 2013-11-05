package org.eclipse.smarthome.core.events.types;


public class BindingItemConfigEvent extends AbstractBindingEvent {

	private String item;

	public BindingItemConfigEvent(String bindingType, String item, String config) {
		this.bindingType = bindingType;
		this.item = item;
		value = config;
	}
	
	
	public String getItem() {
		return item;
	}
	
}
