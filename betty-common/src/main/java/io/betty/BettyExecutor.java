package io.betty;

import io.betty.lifecycle.Lifecycle;
import io.netty.channel.ChannelHandlerContext;

public interface BettyExecutor extends Lifecycle {

	
	public BettyResultWaitStrategy getResultWaitStrategy();
	
	
	public void execute(ChannelHandlerContext ctx, BettyContext bctx);
	
	/**
	 * Current running state is in the executor loop or not. 
	 * @return
	 */
	public boolean inLoop();
	
}
