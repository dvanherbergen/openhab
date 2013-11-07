package org.eclipse.smarthome.core.events.types;

public class SystemEvent {

	protected String node;
	
	protected String value;

	public String getNode() {
		return node;
	}

	public String getValue() {
		if (value != null && value.trim().length() == 0) {
			return null;
		}
		return value;
	}
	
}
