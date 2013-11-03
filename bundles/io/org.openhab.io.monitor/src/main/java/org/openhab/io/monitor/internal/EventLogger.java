/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.monitor.internal;

import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.events.types.SystemEvent;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventLogger implements EventSubscriber {

	static private Logger logger = LoggerFactory.getLogger("runtime.busevents");

	public void receiveCommand(String itemName, Command command) {
		logger.info("{} received command {}", itemName, command);
	}

	public void receiveUpdate(String itemName, State newStatus) {
		logger.info("{} state updated to {}", itemName, newStatus);
	}

	@Override
	public void receiveSystemEvent(SystemEvent systemEvent) {
		logger.debug("'{}' event received for '{}' ", systemEvent.getClass().getSimpleName(), systemEvent.getNode());		
	}

}
