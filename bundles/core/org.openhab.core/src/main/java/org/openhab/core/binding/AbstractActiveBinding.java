/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.binding;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Base class for active bindings which require a scheduled execution for polling values etc.
 */
public abstract class AbstractActiveBinding<T extends AbstractBindingItemConfigProvider> extends AbstractBinding<T> {

	private Future<?> refreshService;

	/**
	 * The working method which is called by the refresh thread frequently.
	 * Developers should put their binding code here.
	 */
	protected abstract void execute();

	/**
	 * Returns the refresh interval in milliseconds to be used by the
	 * RefreshThread between to calls of the execute method.
	 * 
	 * @return the refresh interval
	 */
	protected abstract int getRefreshInterval();

	/**
	 * Mark the binding as completely configured or not. When configured, the
	 * refresh job will start.
	 * 
	 * @param configured
	 */
	protected final void setProperlyConfigured(boolean configured) {

		if (!configured && refreshService != null) {
			// stop the current task
			refreshService.cancel(true);
			return;
		}

		if (configured) {
			if (refreshService != null) {
				// cancel the existing task, so that we can reschedule (possibly
				// with different timings)
				refreshService.cancel(false);
			}

			final AbstractActiveBinding<T> thisBinding = this;
			Runnable task = new Runnable() {

				@Override
				public void run() {
					thisBinding.execute();
				}
			};

			refreshService = submitRepeatingTask(task, getRefreshInterval(), TimeUnit.MILLISECONDS);
		}
	}
}
