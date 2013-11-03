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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.api.services.threading.ThreadPoolId;
import org.eclipse.smarthome.api.services.threading.ThreadPoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class ThreadPoolServiceImpl implements ThreadPoolService {

	private static final Logger log = LoggerFactory.getLogger(ThreadPoolServiceImpl.class);

	private static final int DEFAULT_MIN_POOLSIZE = 2;

	private static final int DEFAULT_SCHEDULED_POOLSIZE = 10;

	private static final int DEFAULT_MAX_POOLSIZE = 20;

	private static final int DEFAULT_KEEP_ALIVE = 500;

	private ConcurrentHashMap<ThreadPoolId, ExecutorService> executorMap = new ConcurrentHashMap<ThreadPoolId, ExecutorService>();

	/**
	 * Start thread pool service.
	 */
	public void activate() {
		log.debug("Starting thread pool service.");
	}

	/**
	 * Shutdown all thread pools.
	 */
	public void deactivate() {
		log.debug("Shutting down thread pool service.");
		for (ExecutorService executor : executorMap.values()) {
			executor.shutdownNow();
		}
		executorMap.clear();
	}

	@Override
	public synchronized ExecutorService getExecutor(ThreadPoolId id) {

		ExecutorService executor = executorMap.get(id);
		log.trace("Retrieving executor for {}", id);

		if (id.isScheduledExecutor()) {
			return null;
		}

		if (executor == null) {

			int minSize = getSystemProperty("threadPool." + id + ".min", DEFAULT_MIN_POOLSIZE);
			int maxSize = getSystemProperty("threadPool." + id + ".max", DEFAULT_MAX_POOLSIZE);
			long keepAlive = getSystemProperty("threadPool." + id + ".keepAlive", DEFAULT_KEEP_ALIVE);

			if (id.isScheduledExecutor()) {
				executor = new ScheduledThreadPoolExecutor(minSize);
			} else {
				executor = new ThreadPoolExecutor(minSize, maxSize, keepAlive, TimeUnit.MILLISECONDS,
						new LinkedBlockingQueue<Runnable>());
			}

			executorMap.put(id, executor);
		}

		return executor;
	}

	@Override
	public synchronized ScheduledExecutorService getScheduledExecutor(ThreadPoolId id) {

		ExecutorService executor = executorMap.get(id);
		log.trace("Retrieving executor for {}", id);

		if (!id.isScheduledExecutor()) {
			return null;
		}

		if (executor == null) {

			int coreSize = getSystemProperty("threadPool." + id + ".size", DEFAULT_SCHEDULED_POOLSIZE);
			executor = new ScheduledThreadPoolExecutor(coreSize);
			executorMap.put(id, executor);

		}

		return (ScheduledExecutorService) executor;
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
}
