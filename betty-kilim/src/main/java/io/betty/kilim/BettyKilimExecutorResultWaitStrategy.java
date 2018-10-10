package io.betty.kilim;

import co.paralleluniverse.fibers.SuspendExecution;
import io.betty.BettyClientContext;
import io.betty.BettyResultWaitStrategy;
import kilim.Mailbox;
import kilim.Pausable;

public class BettyKilimExecutorResultWaitStrategy implements BettyResultWaitStrategy {
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T waitFor(Object waiter, long waitTime) throws Pausable, SuspendExecution, Exception {
		Mailbox<Object> mailbox = (Mailbox<Object>) waiter;
		return (T) mailbox.getb(waitTime);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void notify(Object waiter, Object data) {
		Mailbox<Object> mailbox = (Mailbox<Object>) waiter;
		mailbox.putnb(data);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <W> W newResultWaiter(BettyClientContext reqctx) {
		return (W) new Mailbox<Object>(1);
	}

}
