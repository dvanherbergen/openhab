/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.binding;

import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;

import org.openhab.core.events.EventPublisher;
import org.openhab.core.internal.BindingManager;
import org.openhab.core.items.Item;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Interface which every Binding should implement. Normally you shouldn't
 * implement this interface directly, but rather extend the
 * {@code AbstractBinding}.
 */
public interface Binding<T extends BindingItemConfigProvider> {

	/**
	 * Return the binding id in lowercase. This key is used to determine which
	 * binding properties and which item configurations are for this binding.
	 * e.g. knx, hue.
	 */
	public void getId();

	/**
	 * Process item configuration for a binding. This method is called for each
	 * configured item in the .items file after the binding was initialized with
	 * the binding properties. Whenever an item is updated or removed, this
	 * method will be called again. When an item configuration is removed from
	 * the .items file, this method will be called with a null configuration
	 * string.
	 * 
	 * If the item configuration will not be handled by the binding, this method
	 * MUST throw a BindingItemConfigException
	 * 
	 * This method delegates the actual work to the BindingItemConfigProvider. 
	 * 
	 * @param Item item for which to process the configuration.
	 * @param configuration
	 *            configuration string or null if the item was removed.
	 * @throws BindingItemConfigException
	 *             when the configuration string is invalid or when the binding
	 *             cannot/does not provide a binding for the item.
	 */
	public void processItemConfiguration(Item item, String configuration) throws BindingItemConfigException;

	/**
	 * Called when the binding configuration properties have changed or when the
	 * binding is first registered with the {@link BindingManager}.
	 * 
	 * @param bindingProperties
	 *            binding specific properties.
	 */
	public void processBindingProperties(Properties bindingProperties) throws BindingException;

	/**
	 * Process a Command received from the EventBus.
	 * 
	 * @param itemName
	 * @param command
	 */
	public void receiveCommand(String itemName, Command command);

	/**
	 * Process a Status update received from the EventBus.
	 * 
	 * @param itemName
	 * @param command
	 */
	public void receiveUpdate(String itemName, State state);

	/**
	 * Set the EventPublisher. The event publisher is injected by the
	 * BindingManager.
	 * 
	 * @param eventPublisher
	 */
	public void setEventPublisher(EventPublisher eventPublisher);

	/**
	 * Set the threadpool executor to use for submitting asynchronous tasks to.
	 * All bindings share a single executor threadpool to minimize resource
	 * consumption.
	 * 
	 * @param service
	 *            ScheduledThreadPoolExecutorService.
	 */
	public void setScheduledExecutorService(ScheduledExecutorService service);

	/**
	 * Stops scheduled services which have been submitted from the binding.
	 */
	public void stopScheduledServices();

	/**
	 * Get the item configuration provider for this Binding.
	 * 
	 * @return BindingItemConfigProvider for parsing item configurations.
	 */
	public T getBindingItemConfigProvider();
}
