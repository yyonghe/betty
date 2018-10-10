package io.betty.server.exec;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import co.paralleluniverse.fibers.SuspendExecution;
import io.betty.BettyClientContext;
import io.betty.BettyResultWaitStrategy;
import kilim.Pausable;

@SuppressWarnings("unchecked")
public class BettyThreadPoolResultWaitStrategy implements BettyResultWaitStrategy {

	@Override
	public <T> T waitFor(Object waiter, long waitTime) throws Pausable, SuspendExecution, Exception {
		CompletableFuture<Object> future = (CompletableFuture<Object>) waiter;
		
		return (T) future.get(waitTime, TimeUnit.MILLISECONDS);
	}

	@Override
	public <W> W newResultWaiter(BettyClientContext reqctx) {
		return (W) new CompletableFuture<Object>();
	}

	@Override
	public void notify(Object waiter, Object data) {
		CompletableFuture<Object> future = (CompletableFuture<Object>) waiter;
		future.complete(data);
	}

}
