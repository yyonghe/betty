package io.betty.report;

import com.google.inject.AbstractModule;

import io.betty.BettyModuleProvider;

public class BettyReportModuleProvider implements BettyModuleProvider {

	@Override
	public AbstractModule get() {
		return new BettyReportModule();
	}

}
