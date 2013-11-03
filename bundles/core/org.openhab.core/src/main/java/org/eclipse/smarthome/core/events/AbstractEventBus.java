package org.eclipse.smarthome.core.events;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

import org.eclipse.smarthome.api.services.threading.ThreadPoolId;
import org.eclipse.smarthome.api.services.threading.ThreadPoolService;
import org.eclipse.smarthome.core.events.types.SystemEvent;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Base event bus implementation for distributing received events to subscribers
 * in a non-blocking fashion.
 * 
 * @author Davy Vanherbergen
 */
public abstract class AbstractEventBus implements EventBus {

	private CopyOnWriteArrayList<EventSubscriber> subscribers = new CopyOnWriteArrayList<>();

	private ExecutorService executorService;

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

	/**
	 * Use Declared Services to get the ThreadPoolService and create the
	 * threadpool for distributing events amongst the event bus subscribers.
	 * 
	 * @param threadPoolService
	 *            System ThreadPoolService
	 */
	public void setThreadPoolService(ThreadPoolService threadPoolService) {
		executorService = threadPoolService.getExecutor(ThreadPoolId.EVENT_EXECUTOR_POOL);
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

		executorService.submit(new Runnable() {
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

		executorService.submit(new Runnable() {
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
	 * Notify all subscribers with the given system event.
	 * 
	 * @param sysEvent
	 *            system event.
	 */
	protected final void notifySubscribers(final SystemEvent sysEvent) {

		executorService.submit(new Runnable() {
			@Override
			public void run() {
				for (EventSubscriber s : subscribers) {
					s.receiveSystemEvent(sysEvent);
				}
			}
		});
	}
	
	
}
