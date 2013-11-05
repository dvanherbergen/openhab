package org.eclipse.smarthome.core.internal.binding;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.api.binding.ActiveBinding;
import org.eclipse.smarthome.api.binding.Binding;
import org.eclipse.smarthome.api.binding.BindingConfigException;
import org.eclipse.smarthome.api.services.threading.ThreadPoolId;
import org.eclipse.smarthome.api.services.threading.ThreadPoolService;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.events.InternalEventPublisher;
import org.eclipse.smarthome.core.events.types.AbstractBindingEvent;
import org.eclipse.smarthome.core.events.types.BindingItemConfigEvent;
import org.eclipse.smarthome.core.events.types.BindingPropertiesChangedEvent;
import org.eclipse.smarthome.core.events.types.BindingStatusEvent;
import org.eclipse.smarthome.core.events.types.BindingStatusEvent.Status;
import org.eclipse.smarthome.core.events.types.SystemEvent;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The BindingManager is responsible for integrating all {@link Binding}s which
 * are available on the OSGI runtime with the Eclipse SmartHome runtime. Every
 * OSGI runtime can have only one BindingManager. It is responsible for
 * delegating events between the SmartHome event bus and Bindings.
 * 
 * A BindingManager can run on the main SmartHome runtime or on a remote
 * SmartHome node.
 * 
 * @author Davy Vanherbergen
 * 
 * @since 1.4.0
 */
public class BindingManager implements EventSubscriber {

	private static final Logger log = LoggerFactory.getLogger(BindingManager.class);

	private ConcurrentHashMap<String, Binding> bindings = new ConcurrentHashMap<String, Binding>();

	private ConcurrentHashMap<Binding, Future<?>> activeBindingThreads = new ConcurrentHashMap<Binding, Future<?>>();

	private ConcurrentHashMap<Binding, CopyOnWriteArrayList<String>> bindingItems = new ConcurrentHashMap<Binding, CopyOnWriteArrayList<String>>();

	private ScheduledExecutorService scheduledExecutorService;

	private InternalEventPublisher eventPublisher;

	/**
	 * Remove a binding from the BindingManager. This method is called by DS
	 * when the binding is unloaded. Unregistering the binding will stop the
	 * refresh background thread if available and disconnect the binding from
	 * receiving further events.
	 * 
	 * @param binding
	 *            Binding to stop managing.
	 */
	public void unregisterBinding(Binding binding) {

		log.debug("Unregistering '{}' binding", binding.getBindingType());

		Future<?> future = activeBindingThreads.get(binding);
		if (future != null && !future.isDone()) {
			future.cancel(true);
		}
		activeBindingThreads.remove(binding);
		bindings.remove(binding.getBindingType());
		bindingItems.remove(binding);

		eventPublisher.postSystemEvent(new BindingStatusEvent(binding, Status.REMOVED));
	}

	/**
	 * Add new bindings which have been published in the OSGI runtime.
	 * 
	 * @param binding
	 *            Binding which needs to be managed.
	 */
	public void registerBinding(Binding binding) {

		log.debug("Registering '{}' binding", binding.getBindingType());
		bindings.put(binding.getBindingType(), binding);
		eventPublisher.postSystemEvent(new BindingStatusEvent(binding, Status.NEW));
	}

	/**
	 * Set the thread pool service to use for running background refresh threads
	 * in bindings.
	 * 
	 * @param threadPoolService
	 *            ScheduledThreadPoolExecutor.
	 */
	public void setThreadPoolService(ThreadPoolService threadPoolService) {
		this.scheduledExecutorService = threadPoolService.getScheduledExecutor(ThreadPoolId.SCHEDULED_EXECUTOR_POOL);
	}
	
	/**
	 * Unsetter for DS.
	 * @param threadPoolService
	 */
	public void unsetThreadPoolService(ThreadPoolService threadPoolService) {
		this.scheduledExecutorService = null;
	}

	@Override
	public void receiveCommand(String itemName, Command command) {

		for (Binding binding : bindingItems.keySet()) {
			if (bindingItems.get(binding).contains(itemName)) {
				binding.processCommand(itemName, command);
			}
		}
	}

	@Override
	public void receiveUpdate(String itemName, State newStatus) {

		for (Binding binding : bindingItems.keySet()) {
			if (bindingItems.get(binding).contains(itemName)) {
				binding.processUpdate(itemName, newStatus);
			}
		}
	}

	@Override
	public void receiveSystemEvent(SystemEvent systemEvent) {

		if (!(systemEvent instanceof AbstractBindingEvent)) {
			return;
		}

		AbstractBindingEvent bindingEvent = (AbstractBindingEvent) systemEvent;
		Binding binding = bindings.get(bindingEvent.getBindingType());
		if (binding == null) {
			return;
		}

		try {

			if (systemEvent instanceof BindingPropertiesChangedEvent) {
				BindingPropertiesChangedEvent propertiesChangedEvent = (BindingPropertiesChangedEvent) systemEvent;
				configureBindingProperties(binding, propertiesChangedEvent.getProperties());
				return;
			}

			if (systemEvent instanceof BindingItemConfigEvent) {
				BindingItemConfigEvent itemConfigEvent = (BindingItemConfigEvent) systemEvent;
				String itemName = itemConfigEvent.getItem();
				binding.processItemConfig(itemName, systemEvent.getValue());

				CopyOnWriteArrayList<String> items = bindingItems.get(binding);
				if (items == null) {
					items = new CopyOnWriteArrayList<>();
					bindingItems.put(binding, items);
				}

				if (systemEvent.getValue() == null && items.contains(itemName)) {
					// item no longer configured or used
					items.remove(itemName);
				}

				if (systemEvent.getValue() != null && !items.contains(itemName)) {
					items.add(itemName);
				}
			}

		} catch (BindingConfigException e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * Configure a binding with properties from the cfg file. If the binding is
	 * an active binding and there is no refresh thread running, it is started
	 * after the properties have been processed.
	 * 
	 * @param binding
	 *            Binding to which to add the properties.
	 * @param config
	 *            Properties to add.
	 */
	private void configureBindingProperties(Binding binding, Properties config) {

		try {
			binding.processBindingProperties(config);
			eventPublisher.postSystemEvent(new BindingStatusEvent(binding, Status.PROPERTIES_LOADED));

			// schedule active binding thread...

			if (binding instanceof ActiveBinding && !activeBindingThreads.containsKey(binding)) {
				final ActiveBinding activeBinding = (ActiveBinding) binding;
				Future<?> f = scheduledExecutorService.schedule(new Runnable() {
					@Override
					public void run() {
						if (activeBinding.isEnabled()) {
							activeBinding.execute();
						}
					}
				}, activeBinding.getScheduleInterval(), TimeUnit.MILLISECONDS);
				activeBindingThreads.put(binding, f);
			}

		} catch (BindingConfigException e) {
			log.error("Error configuring '{}' binding properties: {}", binding.getBindingType(), e.getMessage());
		}
	}

	public void setEventPublisher(InternalEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}
	public void unsetEventPublisher(InternalEventPublisher eventPublisher) {
		this.eventPublisher = null;
	}	
	
}
