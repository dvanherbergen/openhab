/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.rest.internal.resources.beans;

import javax.xml.bind.annotation.XmlRootElement;

import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * This is a java bean that is used with JAXB to serialize item states
 * to XML or JSON.
 * This is bean contains only a subset of the item bean in order to minimize 
 * network traffic.
 *    
 * @author Davy Vanherbergen
 * @since 1.4.0
 *
 */
@XmlRootElement(name="item")
public class StateBean {

	public String name;	
	public String state;
	
	public StateBean() {}

	public StateBean(String name, State state) {
		this.name = name;
		if (state != null) {
			if (state.equals(UnDefType.NULL) || state.equals(UnDefType.UNDEF) ) {
				this.state = "";
			} else {
				this.state = state.toString();
			}
		}
	}
		
}
