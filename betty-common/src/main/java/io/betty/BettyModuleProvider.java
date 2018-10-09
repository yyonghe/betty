package io.betty;

import javax.inject.Provider;

import com.google.inject.AbstractModule;

public interface BettyModuleProvider extends Provider<AbstractModule>{

	@Override
	AbstractModule get();
}
