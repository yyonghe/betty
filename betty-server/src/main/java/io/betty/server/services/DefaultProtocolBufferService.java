package io.betty.server.services;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

import co.paralleluniverse.fibers.SuspendExecution;
import io.betty.BettyContext;
import io.betty.lifecycle.Lifecycle;
import io.betty.server.BettyServerContext;
import io.betty.server.BettyService;
import io.betty.util.InternalSlf4JLoggerFactory;
import io.netty.channel.ChannelHandlerContext;
import kilim.Pausable;

/**
 * The default service define for betty. this requires the request and response is encoded by protocol buffer.
 * Also see {@link BettyContext#getRequest()} and {@link BettyContext#getResponse()}
 */
public class DefaultProtocolBufferService extends AbstractService implements BettyService, Lifecycle {
	
	private static final Logger logger = InternalSlf4JLoggerFactory.getLogger(DefaultProtocolBufferService.class);
	
	@Inject
	public DefaultProtocolBufferService(Injector injector) {
		super(injector);
	}
	
	@Override
	public void doService(ChannelHandlerContext ctx, BettyServerContext bctx) throws Pausable, SuspendExecution, Exception {
		try {
			servicelet.doService(bctx);
			ctx.writeAndFlush(bctx);
			
			// write flow log.
			Message req = bctx.getRequest();
			Message rsp = bctx.getResponse();
			
			
			logger.info(TextFormat.shortDebugString(req)+", "+TextFormat.shortDebugString(rsp));
			
			
		} catch (Exception e) {
			logger.error("service error on", e);
		}
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
