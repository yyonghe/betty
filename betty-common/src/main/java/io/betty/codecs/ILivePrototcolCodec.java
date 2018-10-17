package io.betty.codecs;

import java.util.Random;

import io.betty.BettyProtocolCodecOrigin;
import io.betty.BettyProtocolCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Just a simple test {@link BettyServerProtocolcodec}. 
 * We recommend to put *Prototcolcodec classes files into package <b>com.company_name.buziness_name.codecs</b>
 */
public class ILivePrototcolCodec implements BettyProtocolCodec {

	@Override
	public byte[] encode(ChannelHandlerContext ctx, Object data) throws Exception {
		String s = (String) data;
		return s.getBytes();
	}

	@Override
	public BettyProtocolCodecOrigin decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
		int len = buf.readableBytes();
		byte[] databuf = new byte[len];
		buf.readBytes(databuf);
		String data = new String(databuf);
		return new BettyProtocolCodecOrigin(0, new Random().nextInt(2100000000), "", "", data);
	}

	@Override
	public String toString(Object data) {
		// TODO Auto-generated method stub
		return null;
	}
}
