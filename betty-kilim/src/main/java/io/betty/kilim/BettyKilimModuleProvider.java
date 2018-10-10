package io.betty.kilim;

import com.google.inject.AbstractModule;

import io.betty.BettyExecutor;
import io.betty.BettyModuleProvider;
import io.betty.BettyResultWaitStrategy;

public class BettyKilimModuleProvider implements BettyModuleProvider {
	
	@Override
	public AbstractModule get() {
		return new BettyKilimModule();
	}

	private static class BettyKilimModule extends AbstractModule {
		
		@Override
		protected void configure() {
			
			bind(BettyResultWaitStrategy.class).to(BettyKilimExecutorResultWaitStrategy.class);
//			bind(BettyExecutor.class).to(BettyKilimExecutor.class);
			
		}
		
	}

}
