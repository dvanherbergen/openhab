package org.eclipse.smarthome.internal.binding;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.binding.ActiveBinding;
import org.eclipse.smarthome.binding.Binding;
import org.eclipse.smarthome.services.threading.ThreadPoolId;
import org.eclipse.smarthome.services.threading.ThreadPoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The BindingManager is responsible for integrating all {@link Binding}s which
 * are available on the OSGI runtime with the Eclipse SmartHome runtime. Every
 * OSGI runtime can have only one BindingManager. It is responsible for
 * delegating events between the SmartHome event bus and Bindings.
 * 
 * @author Davy Vanherbergen
 * 
 * @since 1.4.0
 */
public class BindingManager {

	private static final Logger log = LoggerFactory.getLogger(BindingManager.class);
	
	private ConcurrentHashMap<String, Binding> bindings = new ConcurrentHashMap<String, Binding>();

	private ConcurrentHashMap<Binding, Future<?>> activeBindingThreads = new ConcurrentHashMap<Binding, Future<?>>();

	private ThreadPoolService threadPoolService;
	
	private ScheduledExecutorService scheduledExecutorService;

	private ConfigurationManager configManager;
	
	public void unregisterBinding(Binding binding) {

		log.debug("Unregistering '{}' binding", binding.getBindingType());
		
		Future<?> future = activeBindingThreads.get(binding);
		if (future != null && !future.isDone()) {
			future.cancel(true);
		}

		bindings.remove(binding);
	}

	public void registerBinding(Binding binding) {
		
		log.debug("Registering '{}' binding", binding.getBindingType());
		bindings.put(binding.getBindingType(), binding);
		
		// update binding with properties
		
		
		
		// update binding with item configurations
		// TODO
		
		
		// schedule active binding thread...
		
		if (binding instanceof ActiveBinding) {
			final ActiveBinding activeBinding = (ActiveBinding) binding; 
			scheduledExecutorService.schedule(new Runnable() {
				@Override
				public void run() {
					if (activeBinding.isEnabled()) {
						activeBinding.execute();		
					}
				}}, activeBinding.getScheduleInterval(), TimeUnit.MILLISECONDS);
		}
	}

	public void setThreadPoolService(ThreadPoolService threadPoolService) {
		this.threadPoolService = threadPoolService;
		this.scheduledExecutorService = this.threadPoolService.getScheduledExecutor(ThreadPoolId.SCHEDULED_EXECUTOR_POOL);
	}

	public void unsetThreadPoolService(ThreadPoolService threadPoolService) {
		this.threadPoolService = null;
	}

	public void setConfigurationManager(ConfigurationManager configManager) {
		this.configManager = configManager;
	}
	
	public void unsetConfigurationManager(ConfigurationManager configManager) {
		this.configManager = null;
	}
}
