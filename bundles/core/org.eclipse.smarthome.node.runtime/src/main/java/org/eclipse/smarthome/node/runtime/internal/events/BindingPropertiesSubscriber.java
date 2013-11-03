package org.eclipse.smarthome.node.runtime.internal.events;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.smarthome.api.binding.Binding;
import org.eclipse.smarthome.api.binding.BindingConfigException;
import org.eclipse.smarthome.api.events.EventPublisher;
import org.openhab.io.transport.mqtt.MqttMessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BindingPropertiesSubscriber, listens to MQTT messages containing binding
 * properties which are defined in the central .cfg file. When properties are
 * received, the subscriber updates the binding with these properties. The
 * subscriber listens to messages on the topic
 * /${node}/${bindingType}/config messages.
 * 
 * @author Davy Vanherbergen
 */
public class BindingPropertiesSubscriber implements MqttMessageConsumer {

	private static final Logger log = LoggerFactory.getLogger(BindingPropertiesSubscriber.class);

	private Binding binding;
	
	private String topic;

	public BindingPropertiesSubscriber(String node, Binding binding) {
		this.binding = binding;
		topic = "/" + node + "/" + binding.getBindingType() + "/config";
	}

	@Override
	public void processMessage(String topic, byte[] payload) {

		log.debug("Received binding properties for '{}'", binding.getBindingType());
		
		Properties config = new Properties();
		
		try {
			config.load(new ByteArrayInputStream(payload));
			binding.processBindingProperties(config);
		} catch (IOException e) {			
			log.error("Error reading properties content from message: {}", new String(payload));
		} catch (BindingConfigException bce) {			
			log.error("Error processing '{}' binding properties: {}", binding.getBindingType(), bce.getMessage());
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
