package org.eclipse.smarthome.binding;

/**
 * BindingConfigException. Used for signaling errors in the binding
 * configuration (properties relating to the whole binding) or in the item
 * binding configurations (properties relating to a single item). This
 * exception can be used to indicate that an binding item configuration
 * property is missing or a binding (item) configuration could not be processed
 * successfully.
 * 
 * @author Davy Vanherbergen
 */
public class BindingConfigException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Create a new binding configuration exception.
	 * 
	 * @param message
	 *            error message.
	 */
	public BindingConfigException(String msg) {
		super(msg);
	}

	/**
	 * Create a new binding configuration exception.
	 * 
	 * @param name
	 *            name of the property or item which caused the exception.
	 * @param message
	 *            error message.
	 */
	public BindingConfigException(String name, String msg) {
		super("Error in '" + name+ "' : " + msg);
	}
}
