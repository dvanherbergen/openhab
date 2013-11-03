package org.eclipse.smarthome.core.events;


/**
 * Default event bus interface. For the main runtime, this is implemented using
 * the eventAdmin service. For remote nodes, this is implemented by an MQTT
 * based event bus.
 * 
 * @author Davy Vanherbergen
 */
public interface EventBus extends InternalEventPublisher {

	public void addEventSubscriber(EventSubscriber subscriber);

	public void removeEventSubscriber(EventSubscriber subscriber);

}
