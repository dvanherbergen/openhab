/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.threading;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for requesting thread pools. To create a thread pool it's sufficient to request
 * a new executor from this service. Pool properties can be defined using system
 * properties in the following format (where [name] is the name of the pool):<br/>
 * 
 * threadPool.[name].min=XX to define the minimum number of threads in the pool.<br/>
 * threadPool.[name].max=XX to define the maximum number of threads in the pool.<br/>
 * threadPool.[name].keepAlive=XX to define the time to keep a thread alive
 * after it completes its' work.
 * 
 * @author Davy Vanherbergen
 * @since 1.4.0
 */
public class ThreadPoolService {

	private static final Logger log = LoggerFactory
			.getLogger(ThreadPoolService.class);

	public static final String EVENT_POOL_EXECUTOR = "events";

	private static final int DEFAULT_MIN_POOLSIZE = 2;

	private static final int DEFAULT_MAX_POOLSIZE = 20;

	private static final int DEFAULT_KEEP_ALIVE = 500;

	public static final String DEFAULT_POOL_EXECUTOR = "default";

	private ConcurrentHashMap<String, ExecutorService> executorMap = new ConcurrentHashMap<String, ExecutorService>();

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

	/**
	 * Get a thread pool executor with the given pool name. If it exists, the
	 * existing one is returned, otherwise a new one will be created.
	 * 
	 * The thread pool properties can be configured using system properties.
	 * 
	 * @param name
	 *            of the pool.
	 * @return ThreadPoolExecutor.
	 */
	public synchronized ExecutorService getExecutor(String name) {

		ExecutorService executor = executorMap.get(name);
		log.trace("Retrieving executor for {}", name);

		if (executor == null) {

			int minSize = getSystemProperty("threadPool." + name + ".min",
					DEFAULT_MIN_POOLSIZE);
			int maxSize = getSystemProperty("threadPool." + name + ".max",
					DEFAULT_MAX_POOLSIZE);
			long keepAlive = getSystemProperty("threadPool." + name
					+ ".keepAlive", DEFAULT_KEEP_ALIVE);

			executor = new ThreadPoolExecutor(minSize, maxSize, keepAlive,
					TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

			executorMap.put(name, executor);
		}

		return executor;
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
				log.error("Invalid thread pool property value specified: {}",
						key);
			}
		}
		return defaultValue;
	}
}
