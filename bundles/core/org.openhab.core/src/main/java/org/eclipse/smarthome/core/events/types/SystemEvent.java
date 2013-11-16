package org.eclipse.smarthome.core.events.types;

import org.eclipse.smarthome.core.util.NodeNameHelper;

/**
 * A System Event represents a state change of one of the internal components of
 * Eclipse SmartHome. System Events do not contain any payload and are only used
 * to signal the occurrence of an event, e.g. that a binding was added, the
 * system was started, etc.
 * 
 * System events are always linked to a node (the runtime where the event
 * originated) and can optionally contain a service name if the event is related
 * to a specific service (e.g. a binding).
 * 
 * @author Davy Vanherbergen
 * @since 1.4.0
 */
public class SystemEvent {

	private SystemEventType type;

	private String node;

	private String service;

	/**
	 * Create a new system event of the given type.
	 * 
	 * @param type
	 *            of event that occurred.
	 */
	public SystemEvent(SystemEventType type) {
		this(type, null);
	}

	/**
	 * Create a new system event of the given type.
	 * 
	 * @param type
	 *            of event that occurred.
	 * @param service
	 *            for which the event occurred.
	 */
	public SystemEvent(SystemEventType type, String service) {
		this(type, NodeNameHelper.getName(), service);
	}

	/**
	 * Create a new system event of the given type.
	 * 
	 * @param type
	 *            of event that occurred.
	 * @param node
	 *            node on which this event occurred.
	 * @param service
	 *            for which the event occurred.
	 */
	public SystemEvent(SystemEventType type, String node, String service) {
		if (type == null) {
			throw new IllegalArgumentException("Missing type.");
		}
		this.type = type;
		this.node = node;
		this.service = service;
	}

	/**
	 * Get the name of the node where this event was created. The node name is
	 * determined automatically if none was provided.
	 * 
	 * @return name of the node.
	 */
	public String getNode() {
		return node;
	}

	/**
	 * Get the service pid to which this system event relates. In most cases,
	 * this will be the id of the binding, e.g. knx, mqtt, etc.
	 * 
	 * @return service or null if the event is not related to a specific
	 *         service.
	 */
	public String getService() {
		return service;
	}

	/**
	 * Get the type of state change this event represents.
	 * 
	 * @return type of this event.
	 */
	public SystemEventType getType() {
		return type;
	}

	@Override
	public String toString() {
		return "SystemEvent [type=" + type + ", node=" + node + ", service=" + service + "]";
	}

}
