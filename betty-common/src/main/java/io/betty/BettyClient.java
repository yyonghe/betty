package io.betty;

import co.paralleluniverse.fibers.SuspendExecution;
import io.netty.channel.ChannelHandlerContext;
import kilim.Pausable;

public interface BettyClient {
	
	public void initialize();
	
	/**
	 * 
	 * @param pkgdata
	 * @param uid
	 * @param timeout
	 * @return
	 */
	public BettyClientContext send(Object pkgdata, int seq, long uid, long timeout);
	
	public BettyResultWaitStrategy getResultWaitStrategy();
	
	public <T> T waitFor(BettyClientContext reqctx) throws Pausable, SuspendExecution, Exception;
	
	public void received(ChannelHandlerContext ctx, BettyClientContext rspctx);
	
	public void shutdown();
	
}
