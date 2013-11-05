/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.model.item.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.core.events.InternalEventPublisher;
import org.eclipse.smarthome.core.events.types.BindingItemConfigEvent;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.GroupFunction;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemFactory;
import org.openhab.core.items.ItemProvider;
import org.openhab.core.items.ItemsChangeListener;
import org.openhab.core.library.types.ArithmeticGroupFunction;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.openhab.model.ItemsStandaloneSetup;
import org.openhab.model.core.EventType;
import org.openhab.model.core.ModelRepository;
import org.openhab.model.core.ModelRepositoryChangeListener;
import org.openhab.model.items.ItemModel;
import org.openhab.model.items.ModelBinding;
import org.openhab.model.items.ModelGroupFunction;
import org.openhab.model.items.ModelGroupItem;
import org.openhab.model.items.ModelItem;
import org.openhab.model.items.ModelNormalItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ItemProvider implementation which computes *.item file based item
 * configurations.
 * 
 * @author Kai Kreuzer
 * @author Thomas.Eichstaedt-Engelen
 * @author Davy Vanherbergen
 */
public class GenericItemProvider implements ItemProvider, ModelRepositoryChangeListener {

	private static final Logger logger = LoggerFactory.getLogger(GenericItemProvider.class);

	/** to keep track of all item change listeners */
	private Collection<ItemsChangeListener> listeners = new HashSet<ItemsChangeListener>();

	private ModelRepository modelRepository = null;

	private Collection<ItemFactory> itemFactorys = new ArrayList<ItemFactory>();

	private InternalEventPublisher eventPublisher;

	public GenericItemProvider() {
		// make sure that the DSL is correctly registered with EMF before we
		// start
		new ItemsStandaloneSetup().createInjectorAndDoEMFRegistration();
	}

	public void setEventPublisher(InternalEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}
	public void unsetEventPublisher(InternalEventPublisher eventPublisher) {
		this.eventPublisher = null;
	}

	public void setModelRepository(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
		modelRepository.addModelRepositoryChangeListener(this);
	}

	public void unsetModelRepository(ModelRepository modelRepository) {
		modelRepository.removeModelRepositoryChangeListener(this);
		this.modelRepository = null;
	}

	/**
	 * Add another instance of an {@link ItemFactory}. Used by Declarative
	 * Services.
	 * 
	 * @param factory
	 *            The {@link ItemFactory} to add.
	 */
	public void addItemFactory(ItemFactory factory) {
		itemFactorys.add(factory);
		dispatchBindingsPerItemType(factory.getSupportedItemTypes());
	}

	/**
	 * Removes the given {@link ItemFactory}. Used by Declarative Services.
	 * 
	 * @param factory
	 *            The {@link ItemFactory} to remove.
	 */
	public void removeItemFactory(ItemFactory factory) {
		itemFactorys.remove(factory);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Item> getItems() {
		List<Item> items = new ArrayList<Item>();
		for (String name : modelRepository.getAllModelNamesOfType("items")) {
			items.addAll(getItemsFromModel(name));
		}
		return items;
	}

	private Collection<Item> getItemsFromModel(String modelName) {
		logger.debug("Read items from model '{}'", modelName);

		List<Item> items = new ArrayList<Item>();
		if (modelRepository != null) {
			ItemModel model = (ItemModel) modelRepository.getModel(modelName);
			if (model != null) {
				for (ModelItem modelItem : model.getItems()) {
					Item item = createItemFromModelItem(modelItem);
					if (item != null) {
						for (String groupName : modelItem.getGroups()) {
							item.getGroupNames().add(groupName);
						}
						items.add(item);
					}
				}
			}
		}
		return items;
	}

	private void processBindingConfigsFromModel(String modelName) {
		logger.debug("Processing binding configs for items from model '{}'", modelName);

		if (modelRepository != null) {
			ItemModel model = (ItemModel) modelRepository.getModel(modelName);
			if (model == null) {
				return;
			}

			// TODO add handling of removed items
			// TODO add support for only sending changed items?

			// create items and read new binding configuration
			for (ModelItem modelItem : model.getItems()) {
				Item item = createItemFromModelItem(modelItem);
				if (item != null) {
					internalDispatchBindings(modelName, item, modelItem.getBindings());
				}
			}
		}
	}

	private Item createItemFromModelItem(ModelItem modelItem) {
		Item item = null;
		if (modelItem instanceof ModelGroupItem) {
			ModelGroupItem modelGroupItem = (ModelGroupItem) modelItem;
			String baseItemType = modelGroupItem.getType();
			GenericItem baseItem = createItemOfType(baseItemType, modelGroupItem.getName());
			if (baseItem != null) {
				ModelGroupFunction function = modelGroupItem.getFunction();
				if (function == null) {
					item = new GroupItem(modelGroupItem.getName(), baseItem);
				} else {
					item = applyGroupFunction(baseItem, modelGroupItem, function);
				}
			} else {
				item = new GroupItem(modelGroupItem.getName());
			}
		} else {
			ModelNormalItem normalItem = (ModelNormalItem) modelItem;
			String itemName = normalItem.getName();
			item = createItemOfType(normalItem.getType(), itemName);
		}
		return item;
	}

	private GroupItem applyGroupFunction(GenericItem baseItem, ModelGroupItem modelGroupItem,
			ModelGroupFunction function) {
		List<State> args = new ArrayList<State>();
		for (String arg : modelGroupItem.getArgs()) {
			State state = TypeParser.parseState(baseItem.getAcceptedDataTypes(), arg);
			if (state == null) {
				logger.warn("State '{}' is not valid for group item '{}' with base type '{}'", new Object[] { arg,
						modelGroupItem.getName(), modelGroupItem.getType() });
				args.clear();
				break;
			} else {
				args.add(state);
			}
		}

		GroupFunction groupFunction = null;
		switch (function) {
		case AND:
			if (args.size() == 2) {
				groupFunction = new ArithmeticGroupFunction.And(args.get(0), args.get(1));
				break;
			} else {
				logger.error("Group function 'AND' requires two arguments. Using Equality instead.");
			}
		case OR:
			if (args.size() == 2) {
				groupFunction = new ArithmeticGroupFunction.Or(args.get(0), args.get(1));
				break;
			} else {
				logger.error("Group function 'OR' requires two arguments. Using Equality instead.");
			}
		case NAND:
			if (args.size() == 2) {
				groupFunction = new ArithmeticGroupFunction.NAnd(args.get(0), args.get(1));
				break;
			} else {
				logger.error("Group function 'NOT AND' requires two arguments. Using Equality instead.");
			}
			break;
		case NOR:
			if (args.size() == 2) {
				groupFunction = new ArithmeticGroupFunction.NOr(args.get(0), args.get(1));
				break;
			} else {
				logger.error("Group function 'NOT OR' requires two arguments. Using Equality instead.");
			}
		case AVG:
			groupFunction = new ArithmeticGroupFunction.Avg();
			break;
		case SUM:
			groupFunction = new ArithmeticGroupFunction.Sum();
			break;
		case MIN:
			groupFunction = new ArithmeticGroupFunction.Min();
			break;
		case MAX:
			groupFunction = new ArithmeticGroupFunction.Max();
			break;
		default:
			logger.error("Unknown group function '" + function.getName() + "'. Using Equality instead.");
		}

		if (groupFunction == null) {
			groupFunction = new GroupFunction.Equality();
		}

		return new GroupItem(modelGroupItem.getName(), baseItem, groupFunction);
	}

	private void dispatchBindingsPerItemType(String[] itemTypes) {
		if (modelRepository != null) {
			for (String modelName : modelRepository.getAllModelNamesOfType("items")) {
				ItemModel model = (ItemModel) modelRepository.getModel(modelName);
				if (model != null) {
					for (ModelItem modelItem : model.getItems()) {
						for (String itemType : itemTypes) {
							if (itemType.equals(modelItem.getType())) {
								Item item = createItemFromModelItem(modelItem);
								internalDispatchBindings(modelName, item, modelItem.getBindings());
							}
						}
					}
				} else {
					logger.debug("Model repository returned NULL for model named '{}'", modelName);
				}
			}
		} else {
			logger.warn("ModelRepository is NULL > dispatch bindings aborted!");
		}
	}

	/**
	 * Send out the item binding config strings on the event bus.  If for an item no autoupdate configuration string
	 * exists, a default one is generated. This will make sure the autoupdate binding receives the commands for all 
	 * the items.
	 * 
	 * @param modelName
	 * @param item
	 * @param bindings
	 */
	private void internalDispatchBindings(String modelName, Item item, EList<ModelBinding> bindings) {

		// TODO add node information
		
		boolean hasAutoUpdateConfig = false;
		
		for (ModelBinding binding : bindings) {

			String bindingType = binding.getType();
			if (bindingType.equals("autoupdate")) {
				hasAutoUpdateConfig = true;
			}
			String name = item.getName();
			String config = binding.getConfiguration();
			BindingItemConfigEvent itemConfigEvent = new BindingItemConfigEvent(bindingType, name, config);
			eventPublisher.postSystemEvent(itemConfigEvent);

		}
		
		if (!hasAutoUpdateConfig) {
			// if no autoupdate was configured for the item, we will use a default one.
			BindingItemConfigEvent itemConfigEvent = new BindingItemConfigEvent("autoupdate", item.getName(), "True");
			eventPublisher.postSystemEvent(itemConfigEvent);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addItemChangeListener(ItemsChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeItemChangeListener(ItemsChangeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Dispatches all binding configs and fires all {@link ItemsChangeListener}s
	 * if {@code modelName} ends with "items".
	 */
	@Override
	public void modelChanged(String modelName, EventType type) {
		if (modelName.endsWith("items")) {

			processBindingConfigsFromModel(modelName);

			for (ItemsChangeListener listener : listeners) {
				listener.allItemsChanged(this, null);
			}
		}
	}

	/**
	 * Creates a new item of type {@code itemType} by utilizing an appropriate
	 * {@link ItemFactory}.
	 * 
	 * @param itemType
	 *            The type to find the appropriate {@link ItemFactory} for.
	 * @param itemName
	 *            The name of the {@link Item} to create.
	 * 
	 * @return An Item instance of type {@code itemType}.
	 */
	private GenericItem createItemOfType(String itemType, String itemName) {
		if (itemType == null) {
			return null;
		}

		for (ItemFactory factory : itemFactorys) {
			GenericItem item = factory.createItem(itemType, itemName);
			if (item != null) {
				logger.trace("Created item '{}' of type '{}'", itemName, itemType);
				return item;
			}
		}

		logger.debug("Couldn't find ItemFactory for item '{}' of type '{}'", itemName, itemType);
		return null;
	}

}
