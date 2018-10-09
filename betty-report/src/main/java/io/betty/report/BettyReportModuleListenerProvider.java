package io.betty.report;

import io.betty.BettyModuleListenerProvider;
import io.betty.lifecycle.LifecycleListener;

public class BettyReportModuleListenerProvider implements BettyModuleListenerProvider {

	@Override
	public LifecycleListener get() {
		return new BettyReportModuleListener();
	}
}
