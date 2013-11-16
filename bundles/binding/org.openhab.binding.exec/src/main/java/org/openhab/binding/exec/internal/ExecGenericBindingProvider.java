/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.exec.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.BindingConfigException;
import org.openhab.binding.exec.ExecBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.TypeParser;

/**
 * <p>
 * This class can parse information from the generic binding format and provides
 * Exec binding information from it. It registers as a
 * {@link ExecBindingProvider} service as well.
 * </p>
 * 
 * <p>
 * Here are some examples for valid binding configuration strings:
 * <ul>
 * <li><code>{ exec="ON:ssh user@openhab.org touch ~/test.txt" }</code> -
 * connect to openhab.org via ssh and issue the command 'touch ~/test.txt'</li>
 * <li><code>{ exec="OFF:ssh teichsta@openhab.org shutdown -p now" }</code></li>
 * <li><code>{ exec="OFF:ssh teichsta@wlan-router ifconfig wlan0 down" }</code></li>
 * <li>
 * <code>{ exec="OFF:some command, ON:'some other command\, \'which is quite \' more \\complex\\ ', *:and a fallback" }</code>
 * </li>
 * <li>
 * <code>{ exec=">[1:open /path/to/my/mp3/gong.mp3] >[2:open /path/to/my/mp3/greeting.mp3] >[*:open /path/to/my/mp3/generic.mp3]" }</code>
 * </li>
 * <li>
 * <code>{ exec="<[curl -s http://weather.yahooapis.com/forecastrss?w=566473&u=c:60000:XSLT(demo_yahoo_weather.xsl)]" }</code>
 * <li>
 * <li>
 * <code>{ exec="<[/bin/sh@@-c@@uptime | awk '{ print $10 }':60000:REGEX((.*?))]" }</code>
 * </li>
 * </ul>
 * 
 * @author Thomas.Eichstaedt-Engelen
 * @author Pauli Anttila
 * @since 0.6.0
 */
public class ExecGenericBindingProvider {

	/**
	 * Artificial command for the exec-in configuration (which has no command
	 * part by definition). Because we use this artificial command we can reuse
	 * the {@link ExecBindingConfig} for both in- and out-configuration.
	 */
	protected static final Command IN_BINDING_KEY = StringType.valueOf("IN_BINDING");

	/**
	 * Artificial command to identify that state changes should be taken into
	 * account
	 */
	protected static final Command CHANGED_COMMAND_KEY = StringType.valueOf("CHANGED");

	protected static final Command WILDCARD_COMMAND_KEY = StringType.valueOf("*");

	/** {@link Pattern} which matches a binding configuration part */
	private static final Pattern BASE_CONFIG_PATTERN = Pattern.compile("(<|>)\\[(.*?)\\](\\s|$)");

	/** {@link Pattern} which matches an In-Binding */
	private static final Pattern IN_BINDING_PATTERN = Pattern.compile("(.*?)?:(?!//)(\\d*):(.*)");

	/** {@link Pattern} which matches an Out-Binding */
	private static final Pattern OUT_BINDING_PATTERN = Pattern.compile("(.*?):(.*)");

	private HashMap<String, ExecBindingConfig> bindingConfigs;

	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "exec";
	}

	/**
	 * {@inheritDoc}
	 */
	public void processBindingConfiguration(String itemName, Class<? extends Item> itemType, String bindingConfig)
			throws BindingConfigException {

		ExecBindingConfig config = new ExecBindingConfig();
		config.itemType = itemType;

		Matcher matcher = BASE_CONFIG_PATTERN.matcher(bindingConfig);

		if (!matcher.matches()) {

			if (bindingConfig.startsWith("<") || bindingConfig.startsWith(">")) {
				throw new BindingConfigException("Exec binding legacy format cannot start with '<' or '>' ");
			}

			// backward compatibility for old format
			parseLegacyOutBindingConfig(itemName, itemType, bindingConfig, config);

		} else {

			matcher.reset();

			while (matcher.find()) {
				String direction = matcher.group(1);
				String bindingConfigPart = matcher.group(2);

				if (direction.equals("<")) {
					config = parseInBindingConfig(itemName, bindingConfigPart, config);
				} else if (direction.equals(">")) {
					config = parseOutBindingConfig(itemName, itemType, bindingConfigPart, config);
				} else {
					throw new BindingConfigException("Unknown command given! Configuration must start with '<' or '>' ");
				}
			}
		}

		bindingConfigs.put(itemName, config);

	}

	protected ExecBindingConfig parseInBindingConfig(String itemName, String bindingConfig, ExecBindingConfig config)
			throws BindingConfigException {

		Matcher matcher = IN_BINDING_PATTERN.matcher(bindingConfig);

		if (!matcher.matches()) {
			throw new BindingConfigException("bindingConfig '" + bindingConfig
					+ "' doesn't represent a valid in-binding-configuration.");
		}
		matcher.reset();

		ExecBindingConfigElement configElement;

		while (matcher.find()) {
			configElement = new ExecBindingConfigElement();
			configElement.commandLine = matcher.group(1).replaceAll("\\\\\"", "");
			configElement.refreshInterval = Integer.valueOf(matcher.group(2)).intValue();
			configElement.transformation = matcher.group(3).replaceAll("\\\\\"", "\"");
			config.put(IN_BINDING_KEY, configElement);
		}

		return config;
	}

	protected ExecBindingConfig parseOutBindingConfig(String itemName, Class<? extends Item> itemType,
			String bindingConfig, ExecBindingConfig config) throws BindingConfigException {

		Matcher matcher = OUT_BINDING_PATTERN.matcher(bindingConfig);

		if (!matcher.matches()) {
			throw new BindingConfigException("bindingConfig '" + bindingConfig
					+ "' doesn't represent a valid in-binding-configuration.");
		}
		matcher.reset();

		ExecBindingConfigElement configElement;

		while (matcher.find()) {
			Command command = createCommandFromString(itemName, itemType, matcher.group(1));

			configElement = new ExecBindingConfigElement();
			configElement.commandLine = matcher.group(2).replaceAll("\\\\\"", "");

			config.put(command, configElement);
		}

		return config;
	}

	protected void parseLegacyOutBindingConfig(String itemName, Class<? extends Item> itemType, String bindingConfig,
			ExecBindingConfig config) throws BindingConfigException {

		String command = StringUtils.substringBefore(bindingConfig, ":").trim();
		String tmpCommandLine = StringUtils.substringAfter(bindingConfig, ":").trim();

		if (StringUtils.isBlank(command) && StringUtils.isBlank(tmpCommandLine)) {
			return;
		}

		String commandLine;

		// if commandLine is surrounded by quotes, life is easy ...
		if (tmpCommandLine.startsWith("'")) {
			commandLine = tmpCommandLine.substring(1).split("(?<!\\\\)'")[0];

			// is there another command we have to parse?
			String tail = tmpCommandLine.replaceFirst(".*(?<!\\\\)' ?,", "").trim();
			if (!tail.isEmpty()) {
				parseLegacyOutBindingConfig(itemName, itemType, tail, config);
			}
		} else {
			// if not, we have to search for the next "," (if there are more
			// than
			// one commandLines) or for the end of this line.
			String[] tmpCommandLineElements = tmpCommandLine.split("(?<!\\\\),");
			if (tmpCommandLineElements.length == 0) {
				commandLine = tmpCommandLine;
			} else {
				commandLine = tmpCommandLineElements[0];
				String tail = StringUtils.join(tmpCommandLineElements, ", ", 1, tmpCommandLineElements.length);
				parseLegacyOutBindingConfig(itemName, itemType, tail, config);
			}
		}

		ExecBindingConfigElement configElement = new ExecBindingConfigElement();
		configElement.commandLine = commandLine.replaceAll("(?<!\\\\)\\\\", "");

		Command cmd = createCommandFromString(itemName, itemType, command);
		config.put(cmd, configElement);
	}

	/**
	 * Creates a {@link Command} out of the given <code>commandAsString</code>
	 * taking the special Commands "CHANGED" and "*" into account and
	 * incorporating the {@link TypeParser}.
	 * 
	 * @param item
	 * @param commandAsString
	 * 
	 * @return an appropriate Command (see {@link TypeParser} for more
	 *         information
	 * 
	 * @throws BindingConfigException
	 *             if the {@link TypeParser} couldn't create a command
	 *             appropriately
	 * 
	 * @see {@link TypeParser}
	 */
	private Command createCommandFromString(String itemName, Class<? extends Item> itemType, String commandAsString)
			throws BindingConfigException {

		if (CHANGED_COMMAND_KEY.equals(commandAsString)) {
			return CHANGED_COMMAND_KEY;
		} else if (WILDCARD_COMMAND_KEY.equals(commandAsString)) {
			return WILDCARD_COMMAND_KEY;
		} else {

			Item item = null;
			try {
				item = itemType.newInstance();
			} catch (Exception e) {
				return null;
			}

			Command command = TypeParser.parseCommand(item.getAcceptedCommandTypes(), commandAsString);

			if (command == null) {
				throw new BindingConfigException("couldn't create Command from '" + commandAsString + "' ");
			}

			return command;
		}
	}

	/**
	 * @{inheritDoc
	 */
	public Class<? extends Item> getItemType(String itemName) {
		ExecBindingConfig config = (ExecBindingConfig) bindingConfigs.get(itemName);
		return config != null ? config.itemType : null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCommandLine(String itemName, Command command) {
		try {
			ExecBindingConfig config = (ExecBindingConfig) bindingConfigs.get(itemName);
			return config != null ? config.get(command).commandLine : null;
		} catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCommandLine(String itemName) {
		ExecBindingConfig config = (ExecBindingConfig) bindingConfigs.get(itemName);
		return config != null && config.get(IN_BINDING_KEY) != null ? config.get(IN_BINDING_KEY).commandLine : null;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getRefreshInterval(String itemName) {
		ExecBindingConfig config = (ExecBindingConfig) bindingConfigs.get(itemName);
		return config != null && config.get(IN_BINDING_KEY) != null ? config.get(IN_BINDING_KEY).refreshInterval : 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTransformation(String itemName) {
		ExecBindingConfig config = (ExecBindingConfig) bindingConfigs.get(itemName);
		return config != null && config.get(IN_BINDING_KEY) != null ? config.get(IN_BINDING_KEY).transformation : null;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getInBindingItemNames() {
		List<String> inBindings = new ArrayList<String>();
		for (String itemName : bindingConfigs.keySet()) {
			ExecBindingConfig config = (ExecBindingConfig) bindingConfigs.get(itemName);
			if (config.containsKey(IN_BINDING_KEY)) {
				inBindings.add(itemName);
			}
		}
		return inBindings;
	}

	/**
	 * This is an internal data structure to store information from the binding
	 * config strings and use it to answer the requests to the Exec binding
	 * provider.
	 */
	static class ExecBindingConfig extends HashMap<Command, ExecBindingConfigElement> implements BindingConfig {

		/** generated serialVersion UID */
		private static final long serialVersionUID = 6164971643530954095L;
		Class<? extends Item> itemType;
	}

	/**
	 * This is an internal data structure to store information from the binding
	 * config strings and use it to answer the requests to the Exec binding
	 * provider.
	 */
	static class ExecBindingConfigElement implements BindingConfig {

		public String commandLine = null;
		int refreshInterval = 0;
		String transformation = null;

		@Override
		public String toString() {
			return "ExecBindingConfigElement [command=" + commandLine + ", refreshInterval=" + refreshInterval
					+ ", transformation=" + transformation + "]";
		}

	}

}
