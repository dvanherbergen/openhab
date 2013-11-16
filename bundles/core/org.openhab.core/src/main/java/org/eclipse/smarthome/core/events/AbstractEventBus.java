package org.eclipse.smarthome.core.events;

import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.events.types.ConfigurationEvent;
import org.eclipse.smarthome.core.events.types.SystemEvent;
import org.eclipse.smarthome.services.threading.ThreadPoolService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Base event bus implementation for distributing received events to subscribers
 * in a non-blocking fashion.
 * 
 * @author Davy Vanherbergen
 * @since 1.4.0
 */
public abstract class AbstractEventBus implements EventBus {

	private CopyOnWriteArrayList<EventSubscriber> subscribers = new CopyOnWriteArrayList<>();
	
	private CopyOnWriteArrayList<SystemEventSubscriber> systemSubscribers = new CopyOnWriteArrayList<>();
	
	private CopyOnWriteArrayList<ConfigurationEventSubscriber> configSubscribers = new CopyOnWriteArrayList<>();

	private ThreadPoolService threadPoolService;

	@Override
	public final void addEventSubscriber(EventSubscriber subscriber) {
		if (!subscribers.contains(subscriber)) {
			subscribers.add(subscriber);
		}
	}
	
	@Override
	public final void removeEventSubscriber(EventSubscriber subscriber) {
		subscribers.remove(subscriber);
	}
	
	@Override
	public final void addSystemEventSubscriber(SystemEventSubscriber subscriber) {
		if (!systemSubscribers.contains(subscriber)) {
			systemSubscribers.add(subscriber);
		}
	}
	
	@Override
	public final void removeSystemEventSubscriber(SystemEventSubscriber subscriber) {
		systemSubscribers.remove(subscriber);
	}
	
	@Override
	public final void addConfigurationEventSubscriber(ConfigurationEventSubscriber subscriber) {
		if (!configSubscribers.contains(subscriber)) {
			configSubscribers.add(subscriber);
		}
	}
	
	@Override
	public final void removeConfigurationEventSubscriber(ConfigurationEventSubscriber subscriber) {
		configSubscribers.remove(subscriber);
	}

	/**
	 * Use Declared Services to get the ThreadPoolService.
	 * 
	 * @param threadPoolService
	 *            System ThreadPoolService
	 */
	public void setThreadPoolService(ThreadPoolService threadPoolService) {
		this.threadPoolService = threadPoolService;
	}

	/**
	 * Notify all subscribers with the State event for the given item.
	 * 
	 * @param itemName
	 *            name of the item for which a new state was received.
	 * @param newStatus
	 *            new status of the item.
	 */
	protected final void notifySubscribers(final String itemName, final State newStatus) {

		threadPoolService.submit(new Runnable() {
			@Override
			public void run() {
				for (EventSubscriber s : subscribers) {
					s.receiveUpdate(itemName, newStatus);
				}
			}
		});
	}

	/**
	 * Notify all subscribers with a command for the given item.
	 * 
	 * @param itemName
	 *            name of the item for which a command was received.
	 * @param command
	 *            command to be processed by the item.
	 */
	protected final void notifySubscribers(final String itemName, final Command command) {

		threadPoolService.submit(new Runnable() {
			@Override
			public void run() {
				for (EventSubscriber s : subscribers) {
					s.receiveCommand(itemName, command);
				}
			}
		});
	}

	/**
	 * Notify all subscribers with a command for the given item. Commands are
	 * sent in a blocking manner and the method will not return until all
	 * subscribers have processed the command.
	 * 
	 * @param itemName
	 *            name of the item for which a command was received.
	 * @param command
	 *            command to be processed by the item.
	 */
	protected final void notifySubscribersAndWait(final String itemName, final Command command) {

		for (EventSubscriber s : subscribers) {
			s.receiveCommand(itemName, command);
		}
	}
	
	
	/**
	 * Notify all system event subscribers with the given system event.
	 * 
	 * @param sysEvent
	 *            system event.
	 */
	protected final void notifySubscribers(final SystemEvent sysEvent) {

		threadPoolService.submit(new Runnable() {
			@Override
			public void run() {
				for (SystemEventSubscriber s : systemSubscribers) {
					s.receiveSystemEvent(sysEvent);
				}
			}
		});
	}	
	
	
	/**
	 * Notify all system event subscribers with the given configuration event.
	 * 
	 * @param event
	 *            Config event.
	 */
	protected final void notifySubscribers(final ConfigurationEvent event) {

		threadPoolService.submit(new Runnable() {
			@Override
			public void run() {
				for (ConfigurationEventSubscriber s : configSubscribers) {
					s.receiveConfigurationEvent(event);
				}
			}
		});
	}	
	
}
