package org.openhab.core.internal;

import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;

import org.openhab.core.binding.Binding;
import org.openhab.core.events.EventPublisher;

/**
 * The BindingManager is responsible for integrating all {@link Binding}s which
 * are available on the OSGI runtime with the Eclipse SmartHome runtime. Every
 * OSGI runtime can have only one BindingManager. It is responsible for
 * delegating events from the SmartHome event bus to and from the Bindings.
 * 
 * @author Davy Vanherbergen
 * 
 * @since 1.4.0
 */
public class BindingManager {

	private EventPublisher eventPublisher;

	private ScheduledExecutorService scheduledExecutorService;

	private CopyOnWriteArrayList<Binding<?>> bindings = new CopyOnWriteArrayList<Binding<?>>();

	public void setEventPublisher(EventPublisher publisher) {
		eventPublisher = publisher;
	}

	public void unsetEventPublisher(EventPublisher publisher) {
		eventPublisher = null;
	}

	public void addBinding(Binding<?> binding) {
		if (!bindings.contains(binding)) {
			binding.setEventPublisher(eventPublisher);
			binding.setScheduledExecutorService(scheduledExecutorService);
			bindings.add(binding);
		}
	}

	public void removeBinding(Binding<?> binding) {
		binding.stopScheduledServices();
		if (bindings.contains(binding)) {
			bindings.remove(binding);
		}
	}

	public void distributeBindingConfiguration(Properties p) {
		// TODO Auto-generated method stub

	}

	public void distributeBindingItemConfiguration() {
		// TODO Auto-generated method stub

	}

}
