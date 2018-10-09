package io.betty;

import co.paralleluniverse.fibers.SuspendExecution;
import io.netty.channel.ChannelHandlerContext;
import kilim.Pausable;

public interface BettyExecutable {
	
	public void run(ChannelHandlerContext ctx) throws Pausable, SuspendExecution, Exception;

}
