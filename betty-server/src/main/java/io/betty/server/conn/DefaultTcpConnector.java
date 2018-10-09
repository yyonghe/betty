package io.betty.server.conn;

import io.betty.lifecycle.LifecycleException;
import io.betty.server.BettyConnector;
import io.betty.server.DefaultServerChannelInitializer;
import io.betty.util.InternalSlf4JLoggerFactory;
import io.betty.util.IntrospectionUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;

public class DefaultTcpConnector extends DefaultConnector implements BettyConnector {
	
	private EventLoopGroup bossGroup;
	
	private EventLoopGroup workerGroup;
	
	@Override
	protected void initInternal() throws LifecycleException {
		super.initInternal();

		// Configure the server.
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = null;
		if(nWorkerThreads > 0) {
			workerGroup = new NioEventLoopGroup(nWorkerThreads);
		} else {
			workerGroup = new NioEventLoopGroup();
		}
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup);
		bootstrap.channel(NioServerSocketChannel.class);
		// default options
		bootstrap.option(ChannelOption.SO_BACKLOG, 100);
		bootstrap.option(ChannelOption.SO_REUSEADDR, true);
		// options
		try {
			IntrospectionUtils.addOption(bootstrap, getOptions());
		} catch (Exception e) {
			new RuntimeException("StandardTcpConnector initInternal", e);
		}
		if(InternalSlf4JLoggerFactory.isEnableDump()) {
			bootstrap.handler(new LoggingHandler());
		}
		bootstrap.childHandler(new DefaultServerChannelInitializer<SocketChannel>(this, this.getProtocolCoder()));
		//
		this.bootstrap = bootstrap;
		this.bossGroup = bossGroup;
		this.workerGroup = workerGroup;
	}
	
	@Override
	protected void destroyInternal() throws LifecycleException {
		//
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
		//
		super.destroyInternal();
	}
}
