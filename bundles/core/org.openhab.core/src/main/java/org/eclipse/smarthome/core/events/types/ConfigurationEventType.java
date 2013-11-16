/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.events.types;

/**
 * Configuration event type. Determines the type of configuration which is
 * contained in a ConfigurationEvent.
 * 
 * @author Davy Vanherbergen
 * @since 1.4.0
 */
public enum ConfigurationEventType {

	/** Service Configuration Properties (from *.cfg) */
	SERVICE_CONFIG,
	/** Item Configuration Properties (from *.items) */
	ITEM_CONFIG;

}
