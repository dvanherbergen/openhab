/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.basic.internal.bean;

import javax.xml.bind.annotation.XmlRootElement;

import org.openhab.core.items.Item;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

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
public class ItemState {

	public String name;
	public String state;

	public ItemState(Item item) {
		this(item.getName(), item.getState());
	}
	
	public ItemState(String name, State itemState) {
		
		this.name = name;
		if (itemState != null) {
			if (itemState.equals(UnDefType.NULL)
					|| itemState.equals(UnDefType.UNDEF)) {
				this.state = null;
			} else {
				this.state = itemState.toString();
			}
		}
	}

}
