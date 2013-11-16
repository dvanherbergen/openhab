/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.config.core.internal;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigActivator implements BundleActivator {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigActivator.class);
	
	public static BundleContext context;
	
	/** Tracker for the ConfigurationAdmin service */
	public static ServiceTracker<ConfigurationAdmin, ConfigurationAdmin> configurationAdminTracker;
	
	/**
	 * Called whenever the OSGi framework starts our bundle
	 */
	public void start(BundleContext bc) throws Exception {
		context = bc;
		configurationAdminTracker = new ServiceTracker<ConfigurationAdmin, ConfigurationAdmin>(bc, ConfigurationAdmin.class, null);
		configurationAdminTracker.open();
		logger.debug("Starting Config Bundle");		
	}

	/**
	 * Called whenever the OSGi framework stops our bundle
	 */
	public void stop(BundleContext bc) throws Exception {
		configurationAdminTracker.close();
		logger.debug("Stopping Config Bundle");
	}
	
}
