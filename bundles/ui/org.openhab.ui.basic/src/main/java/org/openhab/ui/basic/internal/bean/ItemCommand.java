/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.basic.internal.bean;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Command;
import org.openhab.core.types.TypeParser;

/**
 * This is a java bean that is used with JAXB to serialize item states to JSON.
 * This is bean contains only a subset of the item bean in order to minimize
 * network traffic.
 * 
 * @author Davy Vanherbergen
 * @since 1.5.0
 * 
 */
@XmlRootElement(name = "item")
public class ItemCommand {

	public String name;
	public String command;

	public ItemCommand() {
	}

	public String getItemName() {
		return name;
	}

	public Command getCommand() {

		List<Class<? extends Command>> commandList = new ArrayList<Class<? extends Command>>();
		commandList.add(OnOffType.class);
		commandList.add(OpenClosedType.class);
		commandList.add(UpDownType.class);
		commandList.add(IncreaseDecreaseType.class);
		commandList.add(StopMoveType.class);
		commandList.add(HSBType.class);
		commandList.add(PercentType.class);
		commandList.add(DecimalType.class);
		commandList.add(StringType.class);

		return TypeParser.parseCommand(commandList, command);
	}
}
