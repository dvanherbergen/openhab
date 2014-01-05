package org.openhab.io.rest.internal.resources;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.atmosphere.annotation.Broadcast;
import org.atmosphere.annotation.Suspend;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterListener;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.StateChangeListener;
import org.openhab.core.types.State;
import org.openhab.io.rest.RESTApplication;
import org.openhab.io.rest.internal.resources.beans.StateBean;
import org.openhab.io.rest.internal.resources.beans.StateListBean;
import org.openhab.ui.items.ItemUIRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class acts as a REST resource for item states. It is specifically
 * intended for use with WebSockets. On the initial connection, all item states
 * are returned to the client. During the lifetime of the websocket connection,
 * any update on item states will be broadcasted to listeners and any listener
 * may post item commands back to the websocket.
 * 
 * This class differs from the ItemResource in 3 ways:
 * <ol><li>The item information sent is only a subset of the data sent in the ItemResource</li>
 * <li>It allows for a single connection to listen to any item state changes versus the ItemResource, 
 * which requires a connection per item or page.</li>
 * <li>It only supports JSON format.</li></ol>
 * 
 * This resource is registered with the Jersey servlet.
 * 
 * @author Davy Vanherbergen
 * @since 1.4.0
 */
@Path("states")
public class StateResource {

	Logger logger = LoggerFactory.getLogger(StateResource.class);
	
	/**
	 * Retrieve all item states and open a suspended connection.
	 * During the lifetime of the suspended connection, item state changes will be broadcasted
	 * to the connected client.
	 * @return all available item states initially and ad-hoc state changes as they occur.  
	 */
	@Suspend
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Broadcast()
	public Response getItemStates(@Context final Broadcaster broadcaster, @Context final AtmosphereResource resource) {
		
		final StateChangeListener changeListener = new StateChangeListener() {
			
			@Override
			public void stateUpdated(Item item, State state) {
				//noop
			}
			
			@Override
			public void stateChanged(Item item, State oldState, State newState) {				
				broadcaster.broadcast(new StateBean(item.getName(), newState));
			}
		}; 
		
		broadcaster.addBroadcasterListener(new BroadcasterListener() {
			
			@Override
			public void onRemoveAtmosphereResource(Broadcaster b, AtmosphereResource r) {
				removeChangeListener(changeListener);
			}
			
			@Override
			public void onPreDestroy(Broadcaster b) {
				removeChangeListener(changeListener);
				try {
					resource.close();
				} catch (IOException e) {
					logger.error("Error closing resource.", e);
				}
			}
			
			@Override
			public void onPostCreate(Broadcaster b) {
				// noop
			}
			
			@Override
			public void onComplete(Broadcaster b) {
				// noop
			}
			
			@Override
			public void onAddAtmosphereResource(Broadcaster b, AtmosphereResource r) {
				// noop
			}
		});
		
		Object responseObject = getAllItemStates(changeListener);
		return Response.ok(responseObject).build();
	}

		

	/**
	 * Compose a list of all item states. 
	 * @param changeListener 
	 * @return list of all item states.
	 */
	private StateListBean getAllItemStates(StateChangeListener changeListener) {

		List<StateBean> beans = new LinkedList<StateBean>();
		ItemUIRegistry registry = RESTApplication.getItemUIRegistry();
		for (Item item : registry.getItems()) {
			beans.add(new StateBean(item.getName(), item.getState()));
			if (item instanceof GenericItem) {
				logger.debug("Adding state change listener to item {}", item.getName());
				((GenericItem) item).addStateChangeListener(changeListener);
			}
		}
		return new StateListBean(beans);
	}
	
	
	/**
	 * Remove the given change listener from all items.
	 * @param changeListener to remove
	 */
	private void removeChangeListener(StateChangeListener changeListener) {
		
		ItemUIRegistry registry = RESTApplication.getItemUIRegistry();
		for (Item item : registry.getItems()) {		
			if (item instanceof GenericItem) {
				logger.debug("Removing state change listener from item {}", item.getName());
				((GenericItem) item).removeStateChangeListener(changeListener);
			}
		}
	}
}
