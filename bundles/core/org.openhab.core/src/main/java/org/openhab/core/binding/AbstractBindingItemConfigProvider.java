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
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.openhab.core.items.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This abstract class serves as a basis for implementations of binding
 * providers that retrieve binding information from the items configuration
 * file(s), i.e. they register as {@link BindingConfigReader}s.
 * </p>
 * 
 * <p>
 * This class takes care of tracking all changes in the binding config strings
 * and makes sure that all listeners are correctly notified of any change.
 * <p>
 * 
 * @author Kai Kreuzer
 * @since 0.6.0
 * 
 */
public abstract class AbstractBindingItemConfigProvider implements BindingItemConfigProvider {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/** caches binding configurations. maps itemNames to {@link BindingItemConfig}s */
	protected Map<String, BindingItemConfig> bindingConfigs = new ConcurrentHashMap<String, BindingItemConfig>(
			new WeakHashMap<String, BindingItemConfig>());

	protected void addBindingConfig(Item item, BindingItemConfig config) {
		bindingConfigs.put(item.getName(), config);
	}

	/**
	 * @{inheritDoc
	 */
	public boolean providesBindingFor(String itemName) {
		return bindingConfigs.get(itemName) != null;
	}

	@Override
	public Collection<String> getItemNames() {
		return bindingConfigs.keySet();
	}

}