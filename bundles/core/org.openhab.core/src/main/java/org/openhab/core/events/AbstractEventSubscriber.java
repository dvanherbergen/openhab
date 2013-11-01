/**
 * openHAB, the open Home Automation Bus.
 * Copyright (C) 2010-2013, openHAB.org <admin@openhab.org>
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with Eclipse (or a modified version of that library),
 * containing parts covered by the terms of the Eclipse Public License
 * (EPL), the licensors of this Program grant you additional permission
 * to convey the resulting work.
 */
package org.openhab.core.events;

import static org.openhab.core.events.EventConstants.TOPIC_PREFIX;
import static org.openhab.core.events.EventConstants.TOPIC_SEPERATOR;

import java.util.concurrent.ExecutorService;

import org.openhab.core.internal.CoreActivator;
import org.openhab.core.threading.ThreadPoolService;
import org.openhab.core.types.Command;
import org.openhab.core.types.EventType;
import org.openhab.core.types.State;
import org.openhab.core.types.SystemEventType;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

abstract public class AbstractEventSubscriber implements EventSubscriber, EventHandler {
	
	private ExecutorService executorService;
	
	/**
	 * {@inheritDoc}
	 */
	public void handleEvent(Event event) {  
		
		if (executorService == null) {
			executorService = CoreActivator.threadPoolServiceTracker.getService().getExecutor(ThreadPoolService.EVENT_POOL_EXECUTOR);
		}
		
		final String itemName = (String) event.getProperty("item");
		
		String topic = event.getTopic();
		String[] topicParts = topic.split(TOPIC_SEPERATOR);
		
		if(!(topicParts.length > 2) || !topicParts[0].equals(TOPIC_PREFIX)) {
			return; // we have received an event with an invalid topic
		}
		String operation = topicParts[1];
		
		if(operation.equals(EventType.UPDATE.toString())) {
			final State newState = (State) event.getProperty("state");
			if(newState!=null) {
				executorService.submit(new Runnable() {
					@Override
					public void run() {
						receiveUpdate(itemName, newState);						
					}					
				});
			}
		}
		
		if(operation.equals(EventType.COMMAND.toString())) {
			final Command command = (Command) event.getProperty("command");
			if(command!=null) {
				if (event.containsProperty("sync")) {
					receiveCommand(itemName, command);
				} else {
					executorService.submit(new Runnable() {
						@Override
						public void run() {
							receiveCommand(itemName, command);							
						}
					});				
				}
			}
		}
		
		if(operation.equals(EventType.SYSTEM.toString())) {
			final SystemEventType systemEvent = (SystemEventType) event.getProperty("event");
			if (systemEvent != null) {
				executorService.submit(new Runnable() {
					@Override
					public void run() {
						receiveSystemEvent(systemEvent);							
					}
				});				
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void receiveCommand(String itemName, Command command) {
		// default implementation: do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	public void receiveUpdate(String itemName, State newState) {
		// default implementation: do nothing
	}

	@Override
	public void receiveSystemEvent(SystemEventType sysEvent) {
		// default implementation: do nothing
	}
}
