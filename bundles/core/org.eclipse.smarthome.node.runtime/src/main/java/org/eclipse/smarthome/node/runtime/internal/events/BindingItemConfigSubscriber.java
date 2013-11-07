package org.eclipse.smarthome.node.runtime.internal.events;

import org.eclipse.smarthome.binding.Binding;
import org.eclipse.smarthome.binding.BindingConfigException;
import org.eclipse.smarthome.events.EventPublisher;
import org.openhab.io.transport.mqtt.MqttMessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BindingItemConfigSubscriber, listens to MQTT messages containing binding
 * specific item configurations which are defined in the central .items file(s).
 * When configurations are received, the subscriber updates the binding with
 * these item configurations. The subscriber listens to messages on the topic
 * /${node}/${bindingType}/items/${item}/config messages.
 * 
 * @author Davy Vanherbergen
 */
public class BindingItemConfigSubscriber implements MqttMessageConsumer {

	private static final Logger log = LoggerFactory.getLogger(BindingItemConfigSubscriber.class);

	private Binding binding;

	private String topic;

	public BindingItemConfigSubscriber(String node, Binding binding) {
		this.binding = binding;
		topic = "/" + node + "/" + binding.getBindingType() + "/items/+/config";
	}

	@Override
	public void processMessage(String topic, byte[] payload) {

		log.debug("Received binding properties for '{}'", binding.getBindingType());

		String itemName = topic.split("/")[3];
		String configString = new String(payload);

		try {
			binding.processItemConfig(itemName, configString);
		} catch (BindingConfigException e) {
			log.error("Error processing item configuration '{}' for binding '{}' : {}", new Object[] {configString, binding.getBindingType(), e.getMessage()});
		}
	}

	@Override
	public String getTopic() {
		return topic;
	}

	@Override
	public void setTopic(String topic) {
		this.topic = topic;
	}

	@Override
	public void setEventPublisher(EventPublisher eventPublisher) {
		// not used.		
	}

}
