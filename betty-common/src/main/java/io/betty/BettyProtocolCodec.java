package io.betty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Protocol codec
 *
 */
public interface BettyProtocolCodec {
	
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
	public BettyProtocolCodecOrigin decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception;

	/**
	 * To text string for message showing.
	 * @param data
	 * @return
	 */
	public String toString(Object data);
    
}
