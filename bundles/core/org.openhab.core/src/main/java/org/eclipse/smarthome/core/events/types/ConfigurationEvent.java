package org.eclipse.smarthome.core.events.types;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.smarthome.core.util.NodeNameHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration Event. Used to transmit (binding) service configurations on the
 * event bus. Events can be used to transmit current or updated configurations.
 * 
 * Example configuration events are binding service configurations or binding
 * item configurations.
 * 
 * @author Davy Vanherbergen
 * @since 1.4.0
 */
public class ConfigurationEvent {

	private static final Logger log = LoggerFactory.getLogger(ConfigurationEvent.class);

	private ConfigurationEventType type;

	private String node;

	private String service;

	private String itemName;

	private String value;

	/**
	 * Create a new configuration event of the given type.
	 * 
	 * @param type
	 *            of configuration event.
	 * @param node
	 *            for which this config is valid.
	 * @param service
	 *            for which this config is valid.
	 * @param itemName
	 *            name of the item for which this config is valid.
	 * @param value
	 *            configuration value.
	 */
	public ConfigurationEvent(ConfigurationEventType type, String node, String service, String itemName, String value) {
		if (type == null) {
			throw new IllegalArgumentException("Missing type.");
		}
		this.type = type;
		this.node = node;
		this.service = service;
		this.itemName = itemName;
		this.value = value;
	}

	/**
	 * Create a new configuration event of the given type.
	 * 
	 * @param type
	 *            of configuration event.
	 * @param service
	 *            for which this config is valid.
	 * @param itemName
	 *            name of the item for which this config is valid.
	 * @param value
	 *            configuration value.
	 */
	public ConfigurationEvent(ConfigurationEventType type, String service, String itemName, String value) {
		this(type, NodeNameHelper.getName(), service, itemName, value);
	}

	/**
	 * Create a new configuration event of the given type.
	 * 
	 * @param type
	 *            of event that occurred.
	 * @param service
	 *            for which the event occurred.
	 * @param value
	 *            configuration value.
	 */
	public ConfigurationEvent(ConfigurationEventType type, String service, String value) {
		this(type, service, null, value);
	}

	/**
	 * Get the name of the node for which this configuration is valid.
	 * 
	 * @return name of the node.
	 */
	public String getNode() {
		return node;
	}

	/**
	 * Get the service pid to which this configuration event relates.
	 * 
	 * @return service id.
	 */
	public String getService() {
		return service;
	}

	/**
	 * Get the type of configuration this event represents.
	 * 
	 * @return type of this event.
	 */
	public ConfigurationEventType getType() {
		return type;
	}

	/**
	 * Get the item name to which this configuration applies.
	 * 
	 * @return item name or null of the config is not applicable to an item.
	 */
	public String getItemName() {
		return itemName;
	}

	/**
	 * Get the configuration value of this event. A value of null indicates the
	 * configuration is no longer available.
	 * 
	 * @return string value of the configuration.
	 */
	public String getValue() {
		if (value != null && value.trim().length() == 0) {
			return null;
		}
		return value;
	}

	/**
	 * Convert the value of the current event to a Properties set. This method
	 * will only work if the value contains a string representation of a
	 * Properties object. Currently only supported for a service configuration
	 * event.
	 * 
	 * @return (empty) properties object.
	 */
	public Properties getProperties() {

		Properties props = new Properties();

		if (getValue() == null) {
			return props;
		}
		try {
			props.load(new ByteArrayInputStream(getValue().getBytes()));
		} catch (IOException e) {
			log.error("Error parsing properties from event value: '{}'", getValue());
			return props;
		}
		return props;
	}

	@Override
	public String toString() {
		return "ConfigurationEvent [type=" + type + ", node=" + node + ", service=" + service + ", itemName="
				+ itemName + "]";
	}
}
