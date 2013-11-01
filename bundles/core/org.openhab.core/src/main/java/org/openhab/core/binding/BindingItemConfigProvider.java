/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.binding;

import java.util.Collection;

import org.openhab.core.items.Item;

/**
 * BindingItemConfigProvider is used to validates and parse binding specific
 * item configurations in the *.items file into {@link BindingItemConfig}
 * configurations which are made available for use by the binding.
 */
public interface BindingItemConfigProvider {

	/**
	 * Indicates whether this binding provider contains a binding for the given
	 * <code>itemName</code>
	 * 
	 * @param itemName
	 *            the itemName to check
	 * 
	 * @return <code>true</code> if this provider contains an adequate mapping
	 *         for <code>itemName</code> and <code>false</code> otherwise.
	 */
	boolean providesBindingFor(String itemName);

	/**
	 * Returns all items which are mapped to this binding
	 * 
	 * @return items which are mapped to this binding
	 */
	Collection<String> getItemNames();

	/**
	 * Validates if the type of <code>item</code> is valid for this binding.
	 * 
	 * @param item
	 *            the item whose type is validated
	 * @param bindingConfig
	 *            the config string which could be used to refine the validation
	 * 
	 * @throws BindingItemConfigException
	 *             if the type of <code>item</code> is invalid for this binding
	 */
	public void validateItemType(Item item, String bindingConfig) throws BindingItemConfigException;

	/**
	 * This method is called for whenever an item configuration needs to be processed. 
	 * This could be during the initial configuration initialization when the binding has been 
	 * was registered with the BindingManager and all configurations need to be processed
	 * or when a item configuration string was updated.
	 * 
	 * @param item
	 *            the item for which the configuration is defined
	 * @param bindingConfig
	 *            the configuration string that must be processed
	 * 
	 * @throws BindingItemConfigException
	 *             if the configuration string is not valid
	 */
	public void processBindingConfiguration(Item item, String bindingConfig) throws BindingItemConfigException;
}
