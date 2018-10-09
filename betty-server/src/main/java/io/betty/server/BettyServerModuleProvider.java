package io.betty.server;

import com.google.inject.AbstractModule;

import io.betty.BettyModuleProvider;
import io.betty.server.bootstrap.BettyServerBootStrap;
import io.betty.util.BettyNativeInitializer;

public class BettyServerModuleProvider implements BettyModuleProvider {
	
	@Override
	public AbstractModule get() {
		return new BettyServerModule();
	}

	private static class BettyServerModule extends AbstractModule {
		
		@Override
		protected void configure() {
			
			//init native
			new BettyNativeInitializer(BettyServerBootStrap.getPwd());
			//
			DefaultServer server = new DefaultServer();
			bind(BettyServer.class).toInstance(server);
			bind(DefaultServer.class).toInstance(server);
		}
		
	}

}
