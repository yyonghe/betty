package io.betty;

import io.betty.lifecycle.LifecycleListener;

public interface BettyModuleListenerProvider {

	public LifecycleListener get();
	
}
