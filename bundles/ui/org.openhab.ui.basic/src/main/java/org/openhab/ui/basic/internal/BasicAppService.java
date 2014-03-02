package org.openhab.ui.basic.internal;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.StateChangeListener;
import org.openhab.core.types.State;
import org.openhab.ui.basic.internal.bean.ItemCommand;
import org.openhab.ui.basic.internal.bean.ItemState;
import org.openhab.ui.items.ItemUIRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicAppService implements StateChangeListener {

	private static final Logger logger = LoggerFactory
			.getLogger(BasicAppService.class);
	
	private List<BasicAppListener> listeners = new CopyOnWriteArrayList<BasicAppListener>();

	private ItemUIRegistry registry;

	private EventPublisher eventPublisher;

	public void activate() {
		logger.info("BasicAppService activating");
		for (Item item : registry.getItems()) {		
			if (item instanceof GenericItem) {
				logger.info("Adding state change listener to item {}", item.getName());
				((GenericItem) item).addStateChangeListener(this);
			}
		}
		logger.info("BasicAppService activated");
	}
	
	public void deactivate() {
		for (Item item : registry.getItems()) {		
			if (item instanceof GenericItem) {
				logger.info("Removing state change listener from item {}", item.getName());
				((GenericItem) item).removeStateChangeListener(this);
			}
		}
	}
	
	public void registerListener(BasicAppListener listener) {

		listeners.add(listener);

		// send the initial item states upon connection
		Collection<Item> items = registry.getItems();
		for (Item item : items) {
			listener.publishState(new ItemState(item));
		}
	}

	public void unregisterListener(BasicAppListener listener) {
		listeners.remove(listener);
	}

	public void postCommand(ItemCommand cmd) {
		eventPublisher.postCommand(cmd.getItemName(), cmd.getCommand());
	}

	public void setItemUIRegistry(ItemUIRegistry itemUIRegistry) {
		this.registry = itemUIRegistry;
	}

	public void unsetItemUIRegistry(ItemUIRegistry itemUIRegistry) {
		this.registry = null;
	}

	public void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	public void unsetEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = null;
	}

	@Override
	public void stateChanged(Item item, State oldState, State newState) {
		ItemState state = new ItemState(item.getName(), newState);
		for (BasicAppListener listener : listeners) {
			listener.publishState(state);
		}
	}

	@Override
	public void stateUpdated(Item item, State state) {
		// ignore
	}

}
