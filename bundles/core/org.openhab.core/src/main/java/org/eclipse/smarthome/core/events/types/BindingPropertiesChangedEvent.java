package org.eclipse.smarthome.core.events.types;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.smarthome.api.binding.BindingConfigException;

public class BindingPropertiesChangedEvent extends SystemEvent {

	public Properties getProperties() throws BindingConfigException {

		Properties props = new Properties();

		if (getValue() == null) {
			return props;
		}
		try {
			props.load(new ByteArrayInputStream(getValue().getBytes()));
		} catch (IOException e) {
			throw new BindingConfigException("Error parsing properties: " + e.getMessage());
		}
		return props;
	}

}
