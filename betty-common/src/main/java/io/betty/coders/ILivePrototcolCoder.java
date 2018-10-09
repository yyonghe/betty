package io.betty.coders;

import java.util.Random;

import io.betty.BettyContext;
import io.betty.BettyProtocolCoder;
import io.betty.client.DefaultClientContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Just a simple test {@link BettyServerProtocolCoder}. 
 * We recommend to put *PrototcolCoder classes files into package <b>com.company_name.buziness_name.coders</b>
 */
public class ILivePrototcolCoder implements BettyProtocolCoder {

	@Override
	public byte[] encode(ChannelHandlerContext ctx, Object data) throws Exception {
		String s = (String) data;
		return s.getBytes();
	}

	@Override
	public Object decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
		int len = buf.readableBytes();
		byte[] databuf = new byte[len];
		buf.readBytes(databuf);
		String data = new String(databuf);
		return data;
	}

	@Override
	public BettyContext unscramble(Object data) {
		DefaultClientContext bctx = new DefaultClientContext(new Random().nextInt(2100000000), data);
		return bctx;
	}

	@Override
	public String toString(Object data) {
		// TODO Auto-generated method stub
		return null;
	}
}
