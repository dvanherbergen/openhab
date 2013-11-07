package org.eclipse.smarthome.services.threading;

/**
 * Service for submitting jobs to be executed in a shared thread pool. There are
 * 2 thread pools available, a default one for executing jobs immediately and
 * background one for executing fixed interval repeating jobs. ThreadPool
 * properties can be defined using system properties:
 * 
 * threadPool.min=XX to define the minimum number of threads in the pool.<br/>
 * threadPool.max=XX to define the maximum number of threads in the pool.<br/>
 * threadPool.keepAlive=XX to define the time to keep a thread alive after it
 * completes its' work.
 * 
 * For the background jobs, the following system property is available <br/>
 * 
 * threadPool.background.size=XX to define the number of threads in the pool.<br/>
 * 
 * @author Davy Vanherbergen
 * @since 1.4.0
 */
public interface ThreadPoolService {

	/**
	 * Submit a job to be run immediately in the shared thread pool.
	 * 
	 * @param runnable
	 *            job to run.
	 */
	public void submit(Runnable runnable);

	/**
	 * Submit a job which should be repeated every x milliseconds.
	 * 
	 * @param key
	 *            identifier which can be used to locate the job(s) afterwards.
	 *            The same key can be used for multiple jobs.
	 * @param runnable
	 *            job to run.
	 * @param interval
	 *            the time in ms between job executions
	 */
	public void submitRepeating(String key, Runnable runnable, long interval);

	/**
	 * Cancels all running repeating jobs which were submitted using the given
	 * key. This method should be called when the calling bundle is
	 * unloaded.
	 * 
	 * @param key
	 *            identifier which was used to submit the jobs.
	 */
	public void cancelJobs(String key);

	/**
	 * Checks whether there are any jobs running which were submitted with the
	 * given identifier.
	 * 
	 * @param key
	 *            identifier to check
	 * @return true if at least one job is there.
	 */
	public boolean containsJobs(String key);
}
