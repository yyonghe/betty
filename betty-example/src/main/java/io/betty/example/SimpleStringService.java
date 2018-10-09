package io.betty.example;

import org.slf4j.Logger;

import co.paralleluniverse.fibers.SuspendExecution;
import io.betty.lifecycle.Lifecycle;
import io.betty.server.BettyServerContext;
import io.betty.server.BettyService;
import io.betty.server.services.AbstractService;
import io.betty.util.InternalSlf4JLoggerFactory;
import io.netty.channel.ChannelHandlerContext;
import kilim.Pausable;

/**
 * Just a simple implementation of {@link BettyService}, uses {@link String} value for request and response. 
 * We recommend to put *Service classes files into package <b>com.company_name.buziness_name.services</b>
 */
public class SimpleStringService extends AbstractService implements BettyService, Lifecycle {
	
	private static final Logger logger = InternalSlf4JLoggerFactory.getLogger(SimpleStringService.class);
	
	@Override
	public void doService(ChannelHandlerContext ctx, BettyServerContext bctx) throws Pausable, SuspendExecution, Exception {
		try {
			servicelet.doService(bctx);
			ctx.writeAndFlush(bctx);
			
			// write flow log.
//			String req = bctx.getRequest();
//			String rsp = bctx.getResponse();
			
		} catch (Exception e) {
			logger.error("service error on", e);
		}
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
