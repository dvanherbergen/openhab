package org.eclipse.smarthome.api.binding;

import java.util.Properties;

import org.eclipse.smarthome.api.events.EventPublisher;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * Interface which every Binding should implement. A binding is registered with
 * a SmartHome binding manager using Declarative Services. A binding will
 * receive status updates or commands from the SmartHome event bus. A binding
 * will also receive binding properties and item configurations which are
 * relevant for the binding.
 * 
 * @author Davy Vanherbergen
 */
public interface Binding {

	/**
	 * Return the binding type. This type is used as a key to determine which
	 * binding properties and which item configurations are relevant for this
	 * binding. e.g. knx, hue.
	 */
	public String getBindingType();

	/**
	 * Process a Command received from the SmartHome runtime.
	 * 
	 * @param itemName
	 *            name of the item which has received the command.
	 * @param command
	 *            command to process.
	 */
	public void processCommand(String itemName, Command command);

	/**
	 * Process a Status update received from the SmartHome runtime.
	 * 
	 * @param itemName
	 *            name of the item which has received the status update.
	 * @param state
	 *            status update to process.
	 */
	public void processUpdate(String itemName, State state);

	/**
	 * Process binding specific configuration properties. Called when the
	 * binding configuration properties have changed or when the binding is
	 * first registered with the {@link BindingManager}.
	 * 
	 * @param config
	 *            all available properties for this binding.
	 */
	public void processBindingProperties(Properties config) throws BindingConfigException;

	/**
	 * Process an item configuration for a binding. This method is called for
	 * each configured item in the .items file after the binding has been
	 * initialized with the binding properties. Whenever an item is updated or
	 * removed, this method will be called again. When an item configuration is
	 * removed from the .items file, this method will be called with a null
	 * configuration string.
	 * 
	 * If the item configuration will not be handled by the binding, this method
	 * must throw a BindingConfigException.
	 * 
	 * @param itemName
	 *            name of the item for which to process the configuration.
	 * @param itemConfig
	 *            configuration string or null if the item was removed.
	 * @throws BindingConfigException
	 *             when the configuration string is invalid or when the binding
	 *             cannot/does not provide a binding for the item.
	 */
	public void processItemConfig(String itemName, String itemConfig) throws BindingConfigException;

	/**
	 * Sets an EventPublisher on the binding which can be used for sending
	 * commands and updates to the SmartHome event bus. The EventPublisher is
	 * set as soon as the binding is registered with the BindingManager.
	 * 
	 * @param publisher
	 *            EventPublisher to call for sending updates and events.
	 */
	public void setEventPublisher(EventPublisher publisher);
}
