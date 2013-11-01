/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.binding;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class which should be extended by all Bindings.
 */
public abstract class AbstractBinding<T extends AbstractBindingItemConfigProvider> implements
		Binding<BindingItemConfigProvider> {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private EventPublisher eventPublisher;

	private ScheduledExecutorService scheduledExecutorService;

	private List<Future<?>> scheduledServices = new ArrayList<Future<?>>();

	private T bindingItemConfigProvider;

	@Override
	public final void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	@Override
	public final void setScheduledExecutorService(ScheduledExecutorService service) {
		scheduledExecutorService = service;
	}

	@Override
	public final void stopScheduledServices() {
		for (Future<?> f : scheduledServices) {
			if (!f.isDone()) {
				f.cancel(true);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public final T getBindingItemConfigProvider() {

		if (bindingItemConfigProvider != null) {
			return bindingItemConfigProvider;
		}

		// create new instance of the bindingItemConfigProvider
		// we determine the type of item config provider from
		// the return type of the method we are in.

		try {

			Method method = this.getClass().getMethod("getBindingItemConfigProvider");
			ParameterizedType providerType = (ParameterizedType) method.getGenericReturnType();
			Class<T> providerClass = (Class<T>) providerType.getActualTypeArguments()[0];
			bindingItemConfigProvider = providerClass.newInstance();

		} catch (Exception e) {
			logger.error("Could not instantiate binding item provider class for binding.", e);
		}

		return bindingItemConfigProvider;
	}

	
	
	@Override
	public final void processItemConfiguration(Item item, String configuration) throws BindingItemConfigException {				
		getBindingItemConfigProvider().validateItemType(item, configuration);
		getBindingItemConfigProvider().processBindingConfiguration(item, configuration);
	}

	@Override
	public void receiveCommand(String itemName, Command command) {
		// doesn't do anything by default

	}

	@Override
	public void receiveUpdate(String itemName, State state) {
		// doesn't do anything by default
	}

	/**
	 * Post command to the EventBus from the binding.
	 * 
	 * @param itemName
	 * @param command
	 */
	protected final void postCommand(String itemName, Command command) {
		eventPublisher.postCommand(itemName, command);
	}

	/**
	 * Post a status updates to the EventBus.
	 * 
	 * @param itemName
	 * @param state
	 */
	protected final void postUpdate(String itemName, State state) {
		eventPublisher.postUpdate(itemName, state);
	}

	/**
	 * Submit a service for immediate asynchronous execution.
	 * 
	 * @param service
	 *            Runnable.
	 */
	protected final void submitTask(Runnable service) {
		scheduledExecutorService.submit(service);
	}

	/**
	 * Submit a task for repeating asynchronous execution. Repeating tasks are
	 * executed by a thread pool which is shared across all bindings.
	 * 
	 * @param service
	 *            Runnable.
	 */
	protected final Future<?> submitRepeatingTask(Runnable service, int period, TimeUnit unit) {
		Future<?> f = scheduledExecutorService.scheduleAtFixedRate(service, 0, period, unit);
		scheduledServices.add(f);
		return f;
	}

	
}
