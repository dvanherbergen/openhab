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
 * System event type. Defines the available types of system events which are
 * sent/received on the event bus.
 * 
 * @author Davy Vanherbergen
 * @since 1.4.0
 */
public enum SystemEventType {

	/** A new binding was added to a BindingManager. */
	BINDING_ADDED,
	/** A binding is unloaded from a BindingManager. */
	BINDING_REMOVED,
	/** A binding has been initialized with the binding configuration properties. */
	BINDING_PROPERTIES_LOADED,
	/** A binding has been initialized with the item configurations. */
	BINDING_ITEMS_LOADED,
	/** A remote node is starting up. */
	REMOTE_NODE_STARTING,
	/** A remote node has completed startup (including the loading of configuration for all bindings) */
	REMOTE_NODE_STARTED,
	/** A remote node has been stopped. */
	REMOTE_NODE_STOPPED,
	/** The master node is starting up. */
	MASTER_NODE_STARTING,
	/** The master node has completed startup (including the loading of configuration for all bindings) */
	MASTER_NODE_STARTED,
	/** The master node and all known slave nodes have fully started. */
	SYSTEM_STARTED,
	/** The master node has initiated a system shutdown. */
	SYSTEM_SHUTDOWN_INITIATED;

	// TODO this is an initial list of which unused types may need to be cleanup up afterwards
}
