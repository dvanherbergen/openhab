package org.eclipse.smarthome.binding;

/**
 * Interface for active bindings which require a scheduled execution for polling
 * values etc.
 * 
 * @author Davy Vanherbergen
 */
public interface ActiveBinding extends Binding {

	/**
	 * This method is called every x milliseconds and should contain binding
	 * code which needs to be executed periodically. E.g. the polling of a
	 * value.
	 * 
	 * This method will not be called before the {@link
	 * Binding.processBindingProperties()} method has been called at least once.
	 */
	public void execute();

	/**
	 * Get the delay in milliseconds between consecutive executions of this
	 * bindings execute method.
	 * 
	 * @return the interval between executions in milliseconds.
	 */
	public long getScheduleInterval();

	/**
	 * Get the current state of the binding. Return true to enable the binding
	 * and have the execute method called at the specified interval. When false
	 * is returned, the execute method of the binding will not be called.
	 * 
	 * @return true when the binding is active.
	 */
	public boolean isEnabled();
}
