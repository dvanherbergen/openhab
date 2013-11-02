package org.eclipse.smarthome.services.threading;

/**
 * Enum containing the name and type of available shared thread pools.
 * 
 * @author Davy Vanherbergen
 */
public enum ThreadPoolId {

	/** default executor pool for regular use */
	DEFAULT_EXECUTOR_POOL("bindings", false),

	/** dedicated executor pool for handling events */
	EVENT_EXECUTOR_POOL("events", false),

	/** the 'only' pool to use for scheduled background tasks */
	SCHEDULED_EXECUTOR_POOL("background", true);

	private String propertyKey;

	private boolean isScheduledExecutor;

	/**
	 * Create new ThreadPoolId.
	 * 
	 * @param propertyKey
	 *            property key as used in system properties to configure the
	 *            pool.
	 * @param isScheduledExecutor
	 *            use true if the service should return a
	 *            ScheduledExecutorService.
	 */
	private ThreadPoolId(String propertyKey, boolean isScheduledExecutor) {
		this.propertyKey = propertyKey;
		this.isScheduledExecutor = isScheduledExecutor;
	}

	/**
	 * @return property key to be used as part of the available system
	 *         properties.
	 */
	public String getPropertyKey() {
		return propertyKey;
	}

	/**
	 * @return true if this type of pool requires a ScheduledExecutorService.
	 */
	public boolean isScheduledExecutor() {
		return isScheduledExecutor;
	}

	@Override
	public String toString() {
		return propertyKey;
	}
	
}
