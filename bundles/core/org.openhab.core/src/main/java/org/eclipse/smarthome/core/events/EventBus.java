package org.eclipse.smarthome.core.events;

/**
 * Default event bus interface. For the main runtime, this is implemented using
 * the eventAdmin service. For remote nodes, this is implemented by an MQTT
 * based event bus.
 * 
 * @author Davy Vanherbergen
 * @since 1.4.0
 */
public interface EventBus extends SystemEventPublisher, ConfigurationEventPublisher {

	/**
	 * Used by Declarative Services to add any published EventSubscriber.
	 * 
	 * @param subscriber
	 *            to add.
	 */
	public void addEventSubscriber(EventSubscriber subscriber);

	/**
	 * Used by Declarative Services to remove a published EventSubscriber.
	 * 
	 * @param subscriber
	 *            to remove.
	 */
	public void removeEventSubscriber(EventSubscriber subscriber);

	/**
	 * Used by Declarative Services to add any published SystemEventSubscriber.
	 * 
	 * @param subscriber
	 *            to add.
	 */
	public void addSystemEventSubscriber(SystemEventSubscriber subscriber);

	/**
	 * Used by Declarative Services to remove a published SystemEventSubscriber.
	 * 
	 * @param subscriber
	 *            to remove.
	 */
	public void removeSystemEventSubscriber(SystemEventSubscriber subscriber);

	/**
	 * Used by Declarative Services to add any published
	 * ConfigurationEventSubscriber.
	 * 
	 * @param subscriber
	 *            to add.
	 */
	void addConfigurationEventSubscriber(ConfigurationEventSubscriber subscriber);

	/**
	 * Used by Declarative Services to remove a published
	 * ConfigurationEventSubscriber.
	 * 
	 * @param subscriber
	 *            to remove.
	 */
	void removeConfigurationEventSubscriber(ConfigurationEventSubscriber subscriber);
}
