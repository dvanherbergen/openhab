package org.eclipse.smarthome.core.runtime.internal.events;

import static org.eclipse.smarthome.core.runtime.internal.events.EventConstants.TOPIC_PREFIX;
import static org.eclipse.smarthome.core.runtime.internal.events.EventConstants.TOPIC_SEPERATOR;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.smarthome.core.events.AbstractEventBus;
import org.eclipse.smarthome.core.events.types.ConfigurationEvent;
import org.eclipse.smarthome.core.events.types.ConfigurationEventType;
import org.eclipse.smarthome.core.events.types.SystemEvent;
import org.eclipse.smarthome.core.events.types.SystemEventType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event Bus based on OSGI admin service. Used in main SmartHome runtime.
 * 
 * @author Kai Kreuzer
 * @author Davy Vanherbergen
 * 
 * @since 1.4.0
 */
public class EventAdminEventBus extends AbstractEventBus implements EventHandler {

	private static final Logger logger = LoggerFactory.getLogger(EventAdminEventBus.class);

	private EventAdmin eventAdmin;

	@Override
	public void handleEvent(Event event) {

		final String itemName = (String) event.getProperty("item");

		String topic = event.getTopic();
		String[] topicParts = topic.split(TOPIC_SEPERATOR);

		if (!(topicParts.length > 2) || !topicParts[0].equals(TOPIC_PREFIX)) {
			return; // we have received an event with an invalid topic
		}
		String operation = topicParts[1];

		if (operation.equals(EventType.UPDATE.toString())) {
			State newState = (State) event.getProperty("state");
			if (newState != null) {
				notifySubscribers(itemName, newState);
			}
			return;
		}

		if (operation.equals(EventType.COMMAND.toString())) {
			Command command = (Command) event.getProperty("command");
			if (command != null) {
				if (event.containsProperty("sync")) {
					notifySubscribersAndWait(itemName, command);
				} else {
					notifySubscribers(itemName, command);
				}
			}
			return;
		}

		logger.warn(">>>> {} <<<<", topic);
		
		if (operation.equals(EventType.SYSTEM.toString())) {
			SystemEventType type = (SystemEventType) event.getProperty("type");
			String node = (String) event.getProperty("node");
			String service = (String) event.getProperty("service");
			SystemEvent systemEvent = new SystemEvent(type, node, service);
			notifySubscribers(systemEvent);
			return;
		}

		if (operation.equals(EventType.CONFIG.toString())) {
			ConfigurationEventType type = (ConfigurationEventType) event.getProperty("type");
			String node = (String) event.getProperty("node");
			String service = (String) event.getProperty("service");
			String item = (String) event.getProperty("item");
			String value = (String) event.getProperty("value");
			ConfigurationEvent configEvent = new ConfigurationEvent(type, node,
					service, item, value);
			notifySubscribers(configEvent);
		}
	}

	public void setEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
	}

	public void unsetEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = null;
	}

	@Override
	public void sendCommand(String itemName, Command command) {
		if (command != null) {
			if (eventAdmin != null)
				eventAdmin.sendEvent(createSyncCommandEvent(itemName, command));
		} else {
			logger.warn("given command is NULL, couldn't send command to '{}'", itemName);
		}
	}

	@Override
	public void postCommand(String itemName, Command command) {
		if (command != null) {
			if (eventAdmin != null)
				eventAdmin.postEvent(createCommandEvent(itemName, command));
		} else {
			logger.warn("given command is NULL, couldn't post command to '{}'", itemName);
		}
	}

	@Override
	public void postUpdate(String itemName, State newState) {
		if (newState != null) {
			if (eventAdmin != null)
				eventAdmin.postEvent(createUpdateEvent(itemName, newState));
		} else {
			logger.warn("given new state is NULL, couldn't post update for '{}'", itemName);
		}
	}

	private Event createUpdateEvent(String itemName, State newState) {
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("item", itemName);
		properties.put("state", newState);
		return new Event(createTopic(EventType.UPDATE, itemName), properties);
	}

	private Event createCommandEvent(String itemName, Command command) {
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("item", itemName);
		properties.put("command", command);
		return new Event(createTopic(EventType.COMMAND, itemName), properties);
	}

	private Event createSystemEvent(SystemEvent sysEvent) {
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("type", sysEvent.getType());
		properties.put("node", sysEvent.getNode());
		properties.put("service", sysEvent.getService() == null ? "" : sysEvent.getService());
		return new Event(createTopic(EventType.SYSTEM, sysEvent.getNode()), properties);
	}

	/**
	 * Create a new synchronous command event. This command is to be executed
	 * synchronously in the thread of the command sender.
	 * 
	 * @param itemName
	 *            name of the item for which the command is intended
	 * @param command
	 *            openHAB command
	 * @return Event which can be sent on the event bus.
	 */
	private Event createSyncCommandEvent(String itemName, Command command) {
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("item", itemName);
		properties.put("command", command);
		properties.put("sync", Boolean.TRUE);
		return new Event(createTopic(EventType.COMMAND, itemName), properties);
	}

	private String createTopic(EventType type, String subType) {
		return TOPIC_PREFIX + TOPIC_SEPERATOR + type + TOPIC_SEPERATOR + subType;
	}

	@Override
	public void postSystemEvent(SystemEvent event) {
		if (event != null && eventAdmin != null) {
			eventAdmin.postEvent(createSystemEvent(event));
		}
	}

	private Event createConfigurationEvent(ConfigurationEvent configEvent) {
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("type", configEvent.getType());
		properties.put("node", configEvent.getNode());
		properties.put("service", configEvent.getService() == null ? "" : configEvent.getService());
		properties.put("item", configEvent.getItemName() == null ? "" : configEvent.getItemName());
		properties.put("value", configEvent.getValue() == null ? "" : configEvent.getValue());
		return new Event(createTopic(EventType.CONFIG, configEvent.getNode()), properties);
	}

	@Override
	public void postConfigurationEvent(ConfigurationEvent event) {
		if (event != null && eventAdmin != null) {
			eventAdmin.postEvent(createConfigurationEvent(event));
		}
	}
}
