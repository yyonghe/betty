package io.betty;

import co.paralleluniverse.fibers.SuspendExecution;
import kilim.Pausable;

public interface BettyResultWaitStrategy {
	
	
	public <T> T waitFor(Object waiter, long waitTime) throws Pausable, SuspendExecution, Exception;
	
	public <W> W newResultWaiter(BettyClientContext reqctx);

	/**
	 * Received
	 * @param ctx remote channel context.
	 * @param reqctx
	 * @throws Pausable
	 * @throws SuspendExecution
	 * @throws Exception
	 */
	public void notify(Object waiter, Object data);

}
