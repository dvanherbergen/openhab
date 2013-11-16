/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.events.ConfigurationEventPublisher;
import org.eclipse.smarthome.core.events.SystemEventSubscriber;
import org.eclipse.smarthome.core.events.types.ConfigurationEvent;
import org.eclipse.smarthome.core.events.types.ConfigurationEventType;
import org.eclipse.smarthome.core.events.types.SystemEvent;
import org.eclipse.smarthome.core.events.types.SystemEventType;
import org.eclipse.smarthome.services.threading.ThreadPoolService;
import org.openhab.config.core.ConfigConstants;
import org.openhab.config.core.internal.ConfigActivator;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a mean to read any kind of configuration data from a
 * shared config file and dispatch it to the different bundles using the
 * {@link ConfigurationAdmin} service.
 * 
 * <p>
 * The name of the configuration file can be provided as a program argument
 * "openhab.configfile". If this argument is not set, the default
 * "configurations/openhab.cfg" will be used. In case the configuration file
 * does not exist, a warning will be logged and no action will be performed.
 * </p>
 * 
 * <p>
 * The format of the configuration file is similar to a standard property file,
 * with the exception that the property name must be prefixed by the service pid
 * of the {@link ManagedService}:
 * </p>
 * <p>
 * &lt;service-pid&gt;:&lt;property&gt;=&lt;value&gt;
 * </p>
 * <p>
 * The prefix "org.openhab" can be omitted on the service pid, it is
 * automatically added if the pid does not contain any "."
 * </p>
 * 
 * <p>
 * A quartz job can be scheduled to reinitialize the Configurations on a regular
 * basis (defaults to '1' minute)
 * </p>
 * 
 * @author Kai Kreuzer
 * @author Thomas.Eichstaedt-Engelen
 * @author Davy Vanherbergen
 * @since 0.3.0
 */
public class ConfigDispatcher implements ManagedService, Runnable, SystemEventSubscriber {

	private static final Logger logger = LoggerFactory.getLogger(ConfigDispatcher.class);

	// by default, we use the "configurations" folder in the home directory, but
	// this location
	// might be changed in certain situations (especially when setting a config
	// folder in the
	// openHAB Designer).
	private static String configFolder = ConfigConstants.MAIN_CONFIG_FOLDER;

	/** the last refresh timestamp in milliseconds */
	private long lastReload = -1;

	private static ConfigDispatcher instance;

	/**
	 * the refresh interval. A value of '-1' deactivates the scan (optional,
	 * defaults to '-1' hence scanning is deactivated)
	 */
	private int refreshInterval = -1;

	private ThreadPoolService threadPoolService;

	private ConfigurationEventPublisher eventPublisher;

	public ConfigDispatcher() {
		instance = this;
	}

	@Override
	public void run() {

		initializeMainConfiguration(lastReload);

		if (refreshInterval > -1) {
			// schedule the next execution
			threadPoolService.submitDelayed(ConfigActivator.context, this, refreshInterval);
		}
	}

	/**
	 * Set the thread pool service which we can use to schedule the refresh job.
	 * 
	 * @param threadPoolService
	 *            threadPoolService.
	 */
	public void setThreadPoolService(ThreadPoolService threadPoolService) {
		this.threadPoolService = threadPoolService;
	}

	/**
	 * Unsetter for DS.
	 * 
	 * @param threadPoolService
	 */
	public void unsetThreadPoolService(ThreadPoolService threadPoolService) {
		this.threadPoolService = null;
	}

	/**
	 * Setter for DS.
	 * 
	 * @param EventBus
	 */
	public void setEventPublisher(ConfigurationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	/**
	 * Unsetter for DS.
	 * 
	 * @param EventBus
	 */
	public void unsetEventPublisher(ConfigurationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	/**
	 * Returns the configuration folder path name. The main config folder
	 * <code>&lt;openhabhome&gt;/configurations</code> could be overwritten by
	 * setting the System property <code>openhab.configdir</code>.
	 * 
	 * @return the configuration folder path name
	 */
	public static String getConfigFolder() {
		String progArg = System.getProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT);
		if (progArg != null) {
			return progArg;
		} else {
			return configFolder;
		}
	}

	/**
	 * Sets the configuration folder to use. Calling this method will
	 * automatically trigger the loading and dispatching of the contained
	 * configuration files.
	 * 
	 * @param configFolder
	 *            the path name to the new configuration folder
	 */
	public static void setConfigFolder(String configFolder) {
		ConfigDispatcher.configFolder = configFolder;
		instance.initializeDefaultConfiguration();
		instance.initializeMainConfiguration(0);
	}

	public void activate() {
		initializeDefaultConfiguration();
		run();
	}

	private void initializeDefaultConfiguration() {
		String defaultConfigFilePath = getDefaultConfigurationFilePath();
		File defaultConfigFile = new File(defaultConfigFilePath);
		try {
			logger.debug("Processing openHAB default configuration file '{}'.", defaultConfigFile.getAbsolutePath());
			processConfigFile(defaultConfigFile);
		} catch (FileNotFoundException e) {
			// we do not care if we do not have a default file
		} catch (IOException e) {
			logger.error("Default openHAB configuration file '{}' cannot be read.", defaultConfigFilePath, e);
		}
	}

	private void initializeMainConfiguration(long lastReload) {
		String mainConfigFilePath = getMainConfigurationFilePath();
		File mainConfigFile = new File(mainConfigFilePath);

		if (lastReload > -1 && mainConfigFile.lastModified() <= lastReload) {
			logger.trace(
					"main configuration file '{}' hasn't been changed since '{}' (lasModified='{}') -> initialization aborted.",
					new Object[] { mainConfigFile.getAbsolutePath(), lastReload, mainConfigFile.lastModified() });
			lastReload = System.currentTimeMillis();
			return;
		}

		try {
			lastReload = System.currentTimeMillis();
			logger.debug("Processing openHAB main configuration file '{}'.", mainConfigFile.getAbsolutePath());
			processConfigFile(mainConfigFile);
		} catch (FileNotFoundException e) {
			logger.warn("Main openHAB configuration file '{}' does not exist.", mainConfigFilePath);
		} catch (IOException e) {
			logger.error("Main openHAB configuration file '{}' cannot be read.", mainConfigFilePath, e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void processConfigFile(File configFile) throws IOException, FileNotFoundException {
		ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) ConfigActivator.configurationAdminTracker
				.getService();

		if (configurationAdmin != null) {
			// we need to remember which configuration needs to be updated
			// because values have changed.
			Map<Configuration, Dictionary> configsToUpdate = new HashMap<Configuration, Dictionary>();

			// also cache the already retrieved configurations for each pid
			Map<Configuration, Dictionary> configMap = new HashMap<Configuration, Dictionary>();

			List<String> lines = IOUtils.readLines(new FileInputStream(configFile));
			for (String line : lines) {
				String[] contents = parseLine(configFile.getPath(), line);
				// no valid configuration line, so continue
				if (contents == null)
					continue;
				String pid = contents[0];
				String property = contents[1];
				String value = contents[2];
				Configuration configuration = configurationAdmin.getConfiguration(pid, null);
				if (configuration != null) {
					Dictionary configProperties = configMap.get(configuration);
					if (configProperties == null) {
						configProperties = new Properties();
						configMap.put(configuration, configProperties);
					}
					if (!value.equals(configProperties.get(property))) {
						configProperties.put(property, value);
						configsToUpdate.put(configuration, configProperties);
					}
				}
			}

			for (Entry<Configuration, Dictionary> entry : configsToUpdate.entrySet()) {
				entry.getKey().update(entry.getValue());
				eventPublisher.postConfigurationEvent(createConfigurationEvent(entry.getKey(), entry.getValue()));
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private ConfigurationEvent createConfigurationEvent(Configuration conf, Dictionary dict) {

		String pid = conf.getPid();
		String service = pid.substring(pid.lastIndexOf('.') + 1);
		Properties props = (Properties) dict;
		StringWriter writer = new StringWriter();
		props.list(new PrintWriter(writer));
		String value = writer.getBuffer().toString();
		return new ConfigurationEvent(ConfigurationEventType.SERVICE_CONFIG, service, value);
	}

	private String[] parseLine(final String filePath, final String line) {
		String trimmedLine = line.trim();
		if (trimmedLine.startsWith("#") || trimmedLine.isEmpty()) {
			return null;
		}

		if (trimmedLine.substring(1).contains(":")) {
			String pid = StringUtils.substringBefore(line, ":");
			String rest = line.substring(pid.length() + 1);
			if (!pid.contains(".")) {
				pid = "org.openhab." + pid;
			}
			if (!rest.isEmpty() && rest.substring(1).contains("=")) {
				String property = StringUtils.substringBefore(rest, "=");
				String value = rest.substring(property.length() + 1);
				return new String[] { pid.trim(), property.trim(), value.trim() };
			}
		}

		logger.warn("Cannot parse line '{}' of main configuration file '{}'.", line, filePath);
		return null;
	}

	private String getDefaultConfigurationFilePath() {
		return configFolder + "/" + ConfigConstants.DEFAULT_CONFIG_FILENAME;
	}

	private String getMainConfigurationFilePath() {
		String progArg = System.getProperty(ConfigConstants.CONFIG_FILE_PROG_ARGUMENT);
		if (progArg != null) {
			return progArg;
		} else {
			return getConfigFolder() + "/" + ConfigConstants.MAIN_CONFIG_FILENAME;
		}
	}

	@Override
	public void updated(Dictionary<String, ?> config) throws ConfigurationException {
		if (config != null) {
			String refreshIntervalString = (String) config.get("refresh");
			if (isNotBlank(refreshIntervalString)) {
				try {
					refreshInterval = Integer.valueOf(refreshIntervalString);
				} catch (IllegalArgumentException iae) {
					logger.warn("couldn't parse '{}' to an integer");
				}

				if (lastReload > 0) {
					// only reschedule if the main configuration has been
					// previously processed.
					threadPoolService.cancelJobs(ConfigActivator.context);
					if (refreshInterval > 0) {
						threadPoolService.submitDelayed(ConfigActivator.context, this, refreshInterval);
					}
				}
			}
		}
	}

	@Override
	public void receiveSystemEvent(SystemEvent systemEvent) {

		if (lastReload < 0) {
			// ignore events if we haven't read any files yet.
			return;
		}
		
		if (systemEvent.getType().equals(SystemEventType.BINDING_ADDED)) {
			
			// when a new binding has been added to the runtime, 
			// we should send it it's configuration properties

			ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) ConfigActivator.configurationAdminTracker
					.getService();

			if (configurationAdmin == null) {
				return;
			}

			try {
				Configuration configuration = configurationAdmin.getConfiguration(
						"org.openhab." + systemEvent.getService(), null);
				if (configuration != null) {
					eventPublisher.postConfigurationEvent(createConfigurationEvent(configuration,
							configuration.getProperties()));
				}
			} catch (IOException e) {
				logger.error("Coulnd read eventadmin configuration.");
			}

		}

	}
}
