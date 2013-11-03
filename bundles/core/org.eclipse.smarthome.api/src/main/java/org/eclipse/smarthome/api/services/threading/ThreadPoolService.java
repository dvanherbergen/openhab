package org.eclipse.smarthome.api.services.threading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Factory for requesting thread pools. To create a thread pool it's sufficient
 * to request a new executor from this service. ThreadPool properties can be defined
 * using system properties in the following format (where [name] is the name of
 * the pool):<br/>
 * 
 * threadPool.[name].min=XX to define the minimum number of threads in the pool.<br/>
 * threadPool.[name].max=XX to define the maximum number of threads in the pool.<br/>
 * threadPool.[name].keepAlive=XX to define the time to keep a thread alive
 * after it completes its' work.
 * 
 * For the ScheduleThreadPool, the following system property is available
 * <br/>
 * 
 * threadPool.background.size=XX to define the number of threads in the pool.<br/>
 * 
 * @author Davy Vanherbergen
 * @since 1.4.0
 */
public interface ThreadPoolService {

	/**
	 * Get a thread pool executor with the given pool id. If it exists, the
	 * existing one is returned, otherwise a new one will be created.
	 * 
	 * The thread pool properties can be configured using system properties.
	 * 
	 * @param id
	 *            id of the thread pool to return.
	 * @return ThreadPoolExecutor.
	 */
	public ExecutorService getExecutor(ThreadPoolId id);

	/**
	 * Get a scheduled thread pool executor with the given pool name. If it
	 * exists, the existing one is returned, otherwise a new one will be
	 * created.
	 * 
	 * The thread pool properties can be configured using system properties.
	 * 
	 * @param id
	 *            id of the thread pool to return.
	 * @return ScheduledExecutorService.
	 */
	public ScheduledExecutorService getScheduledExecutor(ThreadPoolId id);
}
