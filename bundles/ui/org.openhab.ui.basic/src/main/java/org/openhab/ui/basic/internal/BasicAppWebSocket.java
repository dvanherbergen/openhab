package org.openhab.ui.basic.internal;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.websocket.WebSocket;
import org.openhab.ui.basic.internal.bean.ItemCommand;
import org.openhab.ui.basic.internal.bean.ItemState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicAppWebSocket implements WebSocket.OnTextMessage,
		BasicAppListener {

	private static final Logger logger = LoggerFactory
			.getLogger(BasicAppWebSocket.class);

	private ObjectMapper mapper = new ObjectMapper();

	private Connection connection;

	private BasicAppService service;
	
	public BasicAppWebSocket(BasicAppService service) {
		this.service = service;
	}

	@Override
	public void onOpen(Connection connection) {
		logger.info("Opening websocket");
		this.connection = connection;
		service.registerListener(this);
	}

	@Override
	public void onClose(int closeCode, String message) {
		logger.info("Closing websocket");
		service.unregisterListener(this);
	}

	@Override
	public void onMessage(String data) {
		logger.info("Received message: '{}'", data);
		try {
			ItemCommand cmd = mapper.readValue(data, ItemCommand.class);
			service.postCommand(cmd);
		} catch (Exception e) {
			logger.error("Error processing command '{}'.", data, e);
		}
	}

	@Override
	public void publishState(ItemState state) {
		try {
			String message = mapper.writeValueAsString(state);
			logger.info("Sending state message '{}'", message);
			connection.sendMessage(message);
		} catch (Exception e) {
			logger.error("Error sending state to websocket.", e);
		}
	}

}
