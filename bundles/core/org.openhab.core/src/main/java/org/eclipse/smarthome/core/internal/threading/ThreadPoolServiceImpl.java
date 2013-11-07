/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.threading;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.services.threading.ThreadPoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class ThreadPoolServiceImpl implements ThreadPoolService {

	private static final Logger log = LoggerFactory.getLogger(ThreadPoolServiceImpl.class);

	private static final int DEFAULT_MIN_POOLSIZE = 4;

	private static final int DEFAULT_SCHEDULED_POOLSIZE = 8;

	private static final int DEFAULT_MAX_POOLSIZE = 16;

	private static final int DEFAULT_KEEP_ALIVE = 1000;

	private ExecutorService executor;

	private ScheduledExecutorService scheduledExecutor;

	private ConcurrentHashMap<String, CopyOnWriteArrayList<Future<?>>> scheduledJobs = new ConcurrentHashMap<>();

	/**
	 * Start thread pool service.
	 */
	public void activate() {
		log.debug("Starting thread pool service.");

		int minSize = getSystemProperty("threadPool.min", DEFAULT_MIN_POOLSIZE);
		int maxSize = getSystemProperty("threadPool.max", DEFAULT_MAX_POOLSIZE);
		long keepAlive = getSystemProperty("threadPool.keepAlive", DEFAULT_KEEP_ALIVE);

		executor = new ThreadPoolExecutor(minSize, maxSize, keepAlive, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());

		int coreSize = getSystemProperty("threadPool.background.size", DEFAULT_SCHEDULED_POOLSIZE);
		scheduledExecutor = new ScheduledThreadPoolExecutor(coreSize);
	}

	/**
	 * Shutdown all thread pools.
	 */
	public void deactivate() {
		log.debug("Shutting down thread pool service.");
		if (executor != null) {
			executor.shutdownNow();
		}
		if (scheduledExecutor != null) {
			scheduledExecutor.shutdownNow();
		}
	}

	/**
	 * Read a system property with a given key or get the default value if no
	 * system property is specified.
	 * 
	 * @param key
	 *            system property name.
	 * @param defaultValue
	 *            value to use if no property is available.
	 * @return value as stored in system property or default value if none is
	 *         available.
	 */
	private int getSystemProperty(String key, int defaultValue) {

		String property = System.getProperty(key);
		if (property != null) {
			try {
				return Integer.parseInt(property);
			} catch (NumberFormatException e) {
				log.error("Invalid thread pool property value specified: {}", key);
			}
		}
		return defaultValue;
	}

	@Override
	public void submit(Runnable task) {
		executor.submit(task);
	}

	@Override
	public void submitRepeating(String key, Runnable task, long interval) {
		CopyOnWriteArrayList<Future<?>> jobs = scheduledJobs.get(key);
		if (jobs == null) {
			jobs = new CopyOnWriteArrayList<>();
			scheduledJobs.put(key, jobs);
		}
		jobs.add(scheduledExecutor.scheduleWithFixedDelay(task, 0, interval, TimeUnit.MILLISECONDS));		
	}

	@Override
	public void cancelJobs(String key) {
		CopyOnWriteArrayList<Future<?>> jobs = scheduledJobs.get(key);
		if (jobs != null) {
			for (Future<?> future : jobs) {
				future.cancel(true);
			}
			scheduledJobs.remove(key);
		}
	}

	@Override
	public boolean containsJobs(String key) {
		return scheduledJobs.containsKey(key);
	}

}
