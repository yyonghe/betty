package io.betty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface BettyProtocolCoder {
	
	/**
	 * @param ctx
	 * @param bctx
	 * @return
	 * @throws Exception
	 */
	public byte[] encode(ChannelHandlerContext ctx, Object data) throws Exception;
	
	/**
	 * @param ctx channel context.
	 * @param buf
	 * @return
	 * @throws Exception
	 */
	public Object decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception;
	
	/**
	 * Unscramble to betty context give informations about:
	 * user id
	 * user key
	 * req sequence
	 * req cmd
	 * req subcmd
	 * req data
	 * 
	 * @param data the data returned from {@link #decode(ChannelHandlerContext, ByteBuf)} method
	 * @return
	 */
	public BettyContext unscramble(Object data);
	
	/**
	 * To text string for logging or debug.
	 * @param data
	 * @return
	 */
	public String toString(Object data);
    
}
