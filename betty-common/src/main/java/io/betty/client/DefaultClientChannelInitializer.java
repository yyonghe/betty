package io.betty.client;

import java.net.InetSocketAddress;
import java.util.List;

import org.slf4j.Logger;

import io.betty.BettyClient;
import io.betty.BettyClientContext;
import io.betty.BettyProtocolCodecOrigin;
import io.betty.BettyProtocolCodec;
import io.betty.util.InternalSlf4JLoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.logging.LoggingHandler;

@Sharable
public class DefaultClientChannelInitializer<C extends Channel> extends ChannelInitializer<C>{
	
	private static final Logger logger = InternalSlf4JLoggerFactory.getLogger(DefaultClientChannelInitializer.class);
	
	private BettyClient client;
	private BettyProtocolCodec protocolCodec;
	private boolean tcp = true;

	public DefaultClientChannelInitializer(BettyClient client, BettyProtocolCodec protocolcodec, boolean tcp) {
		//
		this.client = client;
		this.protocolCodec = protocolcodec;
		this.tcp = tcp;
	}
	

	@Override
	protected void initChannel(C channel) throws Exception {
		ChannelPipeline pipeline = channel.pipeline();
		if(InternalSlf4JLoggerFactory.isEnableDump()) {
			pipeline.addLast("LoggingHandler", new LoggingHandler());
		}
		if(tcp) {
			pipeline.addLast("Encodec", new TcpEncodec());
			pipeline.addLast("Decodec", new TcpDecodec());
		} else {
			pipeline.addLast("Encodec", new UdpEncodec());
			pipeline.addLast("Decodec", new UdpDecodec());
		}
		pipeline.addLast("DefaultHandler", new DefaultHandler());
	}
	
	class DefaultHandler extends SimpleChannelInboundHandler<BettyClientContext> {
		
		@Override
		protected void channelRead0(ChannelHandlerContext ctx, BettyClientContext rspctx) throws Exception {
			try {
				client.received(ctx, rspctx);
			} catch (Exception e) {
				logger.error("recv response", e);
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			logger.error("Unknown exception caught on: " + ctx, cause);
		}
	}

	@Sharable
	class TcpEncodec extends MessageToByteEncoder<Object> {

		@Override
		protected void encode(ChannelHandlerContext ctx, Object bctx, ByteBuf buf) throws Exception {
			BettyClientContext bcctx = (BettyClientContext) bctx;
			byte[] data = protocolCodec.encode(ctx, bcctx.getData());
        	buf.writeBytes(data);
		}
		
	}
	
	class TcpDecodec extends ByteToMessageDecoder {
		
		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
			
			BettyProtocolCodecOrigin data = protocolCodec.decode(ctx, buf);
			
			if(data != null) {
				BettyClientContext bctx = new DefaultClientContext(data.seq, data.uid,
						data.data, data.retCode, data.retMessage);
				
				bctx.setRemote(ctx.channel().remoteAddress());
				bctx.setLocal(ctx.channel().localAddress());
				
				out.add(bctx);
			}
			
		}
	}
	
	@Sharable
	class UdpEncodec extends ChannelOutboundHandlerAdapter {

		@Override
		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
			
			BettyClientContext bcctx = (BettyClientContext) msg;
			
			byte[] data = protocolCodec.encode(ctx, bcctx.getData());
			
			ByteBuf buf = ctx.alloc().ioBuffer();
			buf.writeBytes(data);
			
			DatagramPacket packet = new DatagramPacket(buf, (InetSocketAddress) bcctx.getRemote());
			
			super.write(ctx, packet, promise);
		}
		
	}
	
	@Sharable
	class UdpDecodec extends SimpleChannelInboundHandler<DatagramPacket> {

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
			
			ByteBuf buf = msg.content();
			
			BettyProtocolCodecOrigin data = protocolCodec.decode(ctx, buf);
			
			if(data != null) {
				BettyClientContext bctx = new DefaultClientContext(data.seq, data.uid,
						data.data, data.retCode, data.retMessage);
				
				bctx.setRemote(msg.sender());
				
				bctx.setLocal(msg.recipient());
				
				ctx.fireChannelRead(bctx);
			}
		}
		
	}
}
