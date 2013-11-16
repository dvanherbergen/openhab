package org.eclipse.smarthome.core.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Helper utility to help determine the node name of the current machine which
 * is running SmartHome.
 * 
 * All code wanting to determine the current node should use this class, as the
 * sequence to determine the node name may change in the future and lead to
 * different node names.
 * 
 * @author Davy Vanherbergen
 * @since 1.4.0
 */
public class NodeNameHelper {

	private static String nodeName;

	/**
	 * Hidden default constructor.
	 */
	private NodeNameHelper() {

	}

	/**
	 * Get the name of the current node. The node name is determined
	 * automatically using the following sequence:
	 * <ol>
	 * <li>Value of the smarthome.node system property</li>
	 * <li>Host name of the machine if different from localhost</li>
	 * </ol>
	 * 
	 * @return name of the current SmartHome node.
	 */
	public static String getName() {

		if (nodeName != null) {
			return nodeName;
		}

		nodeName = System.getProperty("smarthome.node");
		if (nodeName != null) {
			return nodeName;
		}

		try {
			InetAddress iAddress = InetAddress.getLocalHost();
			String hostName = iAddress.getHostName();
			if (hostName != null && !hostName.equalsIgnoreCase("localhost")) {
				nodeName = hostName;
				return nodeName;
			}
		} catch (UnknownHostException e) {
			// nothing found..
		}

		nodeName = "missing-node-name";
		return nodeName;
	}

}
