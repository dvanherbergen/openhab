package org.openhab.ui.basic.internal;

import org.openhab.ui.basic.internal.bean.ItemState;

public interface BasicAppListener {

	public void publishState(ItemState state);
	
}
