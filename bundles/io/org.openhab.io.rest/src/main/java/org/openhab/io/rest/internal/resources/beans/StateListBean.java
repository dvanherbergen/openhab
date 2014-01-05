/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.rest.internal.resources.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The StateListBean is a Java bean which is used with JAXB to serialize a list of item states.
 *  
 * @author Davy Vanherbergen
 * @since 1.4.0
 *
 */
@XmlRootElement(name="states")
public class StateListBean {

	public StateListBean() {}
	
	public StateListBean(Collection<StateBean> list) {
		entries.addAll(list);
	}
	
	@XmlElement(name="states")
	public final List<StateBean> entries = new ArrayList<StateBean>();
	
}
