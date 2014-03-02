/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.basic.internal;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.openhab.io.net.http.SecureHttpContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket Servlet for broadcasting item state changes to connected clients.
 * 
 * @since 1.5.0
 * @author Davy Vanherbergen
 */
public class BasicAppServlet extends WebSocketServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory
			.getLogger(BasicAppServlet.class);

	private static final String SERVLET_ALIAS = "/basic-ws";

	private HttpService httpService;

	private BasicAppService service;

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request,
			String protocol) {
		return new BasicAppWebSocket(service);
	}

	/**
	 * Creates a {@link SecureHttpContext} which handles the security for this
	 * Servlet
	 * 
	 * @return a {@link SecureHttpContext}
	 */
	private HttpContext createHttpContext() {
		HttpContext defaultHttpContext = httpService.createDefaultHttpContext();
		return new SecureHttpContext(defaultHttpContext, "openHAB.org");
	}

	public void activate() {

		logger.info("Starting Basic UI at /basic");
		try {
			Hashtable<String, String> props = new Hashtable<String, String>();
			httpService.registerServlet(SERVLET_ALIAS, this, props,
					createHttpContext());
			// httpService.registerResources("/basic", "web", null);
			logger.info("Started Basic UI at /basic");
		} catch (Exception e) {
			logger.error("Error during servlet startup", e);
		}
	}

	public void deactivate() {
		httpService.unregister(SERVLET_ALIAS);
	}

	public void setHttpService(HttpService httpService) {
		this.httpService = httpService;
	}

	public void unsetHttpService(HttpService httpService) {
		this.httpService = null;
	}

	public void setBasicAppService(BasicAppService service) {
		this.service = service;
	}

	public void unsetBasicAppService(BasicAppService service) {
		this.service = null;
	}
}
