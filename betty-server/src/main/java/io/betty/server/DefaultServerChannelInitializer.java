package io.betty.server;

import java.net.InetSocketAddress;
import java.util.List;

import org.slf4j.Logger;

import io.betty.BettyExecutor;
import io.betty.BettyProtocolCoder;
import io.betty.lifecycle.LifecycleState;
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
public class DefaultServerChannelInitializer<C extends Channel> extends ChannelInitializer<C>{
	
	private static final Logger logger = InternalSlf4JLoggerFactory.getLogger(DefaultServerChannelInitializer.class);
	
	private BettyConnector connector;
	private BettyProtocolCoder protocolCodec;
	private boolean tcp;

	public DefaultServerChannelInitializer(BettyConnector connector, BettyProtocolCoder protocolCoder) {
		
		this.tcp = (connector.getKind() == BettyConnector.Kind.TCP);
		//
		this.connector = connector;
		this.protocolCodec = protocolCoder;
	}
	

	@Override
	protected void initChannel(C channel) throws Exception {
		ChannelPipeline pipeline = channel.pipeline();
		if(InternalSlf4JLoggerFactory.isEnableDump()) {
			pipeline.addLast("LoggingHandler", new LoggingHandler());
		}
		if(tcp) {
			pipeline.addLast("Encoder", new TcpEncoder());
			pipeline.addLast("Decoder", new TcpDecoder());
		} else {
			pipeline.addLast("Encoder", new UdpEncoder());
			pipeline.addLast("Decoder", new UdpDecoder());
		}
		pipeline.addLast("DefaultHandler", new DefaultHandler());
	}
	
	class DefaultHandler extends SimpleChannelInboundHandler<BettyServerContext> {
		
		@Override
		protected void channelRead0(ChannelHandlerContext ctx, BettyServerContext bctx) throws Exception {
			try {
				BettyExecutor executor = connector.getServer().getExecutor();
				
				executor.execute(ctx, bctx);

			} catch (Exception e) {
				connector.getServer().publishAsyncEvent(bctx.getService(), 
						LifecycleState.DISPATCH_TO_EXECUTE_FAILED_EVENT, bctx);
				logger.error("dispatch request to executor failed, overload...?", e);
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			logger.error("Unknown exception caught on: " + ctx, cause);
		}
	}

	@Sharable
	class TcpEncoder extends MessageToByteEncoder<Object> {

		@Override
		protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf buf) throws Exception {
			BettyServerContext bsctx = (BettyServerContext) msg;
			byte[] data = protocolCodec.encode(ctx, bsctx.getResponse());
        	buf.writeBytes(data);
		}
		
	}
	
	class TcpDecoder extends ByteToMessageDecoder {
		
		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
			
			
			Object data = protocolCodec.decode(ctx, buf);
			
			if(data != null) {
				
				BettyServerContext bctx = (BettyServerContext) protocolCodec.unscramble(data);
				
				bctx.setService(connector.getServer().findService(bctx.getSubcmd()));
				
				bctx.setRemote((InetSocketAddress) ctx.channel().remoteAddress());
				bctx.setLocal((InetSocketAddress) ctx.channel().localAddress());
				bctx.setProtocolCoder(protocolCodec);
				
				out.add(bctx);
			}
			
		}
	}
	
	@Sharable
	class UdpEncoder extends ChannelOutboundHandlerAdapter {

		@Override
		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
			
			BettyServerContext bsctx = (BettyServerContext) msg;
			
			byte[] data = protocolCodec.encode(ctx, bsctx.getResponse());
			
			ByteBuf buf = ctx.alloc().ioBuffer();
			buf.writeBytes(data);
			
			DatagramPacket packet = new DatagramPacket(buf, (InetSocketAddress) bsctx.getRemote());
			
			super.write(ctx, packet, promise);
		}
		
	}
	
	@Sharable
	class UdpDecoder extends SimpleChannelInboundHandler<DatagramPacket> {

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
			
			ByteBuf buf = msg.content();
			
			Object data = protocolCodec.decode(ctx, buf);
			
			if(data != null) {
				BettyServerContext bctx = (BettyServerContext) protocolCodec.unscramble(data);
				
				bctx.setService(connector.getServer().findService(bctx.getSubcmd()));
				
				bctx.setRemote(msg.sender());
				
				bctx.setLocal(msg.recipient());
				
				bctx.setProtocolCoder(protocolCodec);
				
				ctx.fireChannelRead(bctx);
			}
		}
		
	}
}
