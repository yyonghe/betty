package io.betty;

import java.net.SocketAddress;

import co.paralleluniverse.fibers.SuspendExecution;
import io.netty.channel.ChannelHandlerContext;
import kilim.Pausable;


/**
 * Context for current request. 
 *
 */
public interface BettyContext {
	
	/**
	 * request sequence number
	 * @return
	 */
	public int getSeq();

	/**
	 * @return the uid
	 */
	public long getUid();

	/**
	 * @return the start time
	 */
	public long getStartTime();

	/**
	 * @return the remote
	 */
	public SocketAddress getRemote();
	public void setRemote(SocketAddress remote);

	/**
	 * @return the local
	 */
	public SocketAddress getLocal();
	public void setLocal(SocketAddress local);

	/**
	 * 
	 * @return the process ret code, default should be 0.
	 */
	public int getRetCode();
	
	/**
	 * @return the process optional message, default should be "ok".
	 */
	public String getRetMessage();
	
	public void setProtocolCoder(BettyProtocolCoder protocolCoder);
	public BettyProtocolCoder getProtocolCoder();
	
	public void startContext(ChannelHandlerContext ctx) throws Pausable, SuspendExecution, Exception;
}
