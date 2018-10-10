package io.betty.example;

import com.google.inject.Inject;
import com.google.inject.Injector;

import co.paralleluniverse.fibers.SuspendExecution;
import io.betty.server.BettyServerContext;
import io.betty.server.BettyService;
import io.betty.server.services.AbstractService;
import io.netty.channel.ChannelHandlerContext;
import kilim.Pausable;

/**
 * Just a simple test {@link BettyService}. Usauly extends from {@link AbstractService} 
 * We recommend to put *Service classes files into package <b>com.company_name.buziness_name.services</b>
 */
public class SimpleEchoService extends AbstractService {
	
	@Inject
	public SimpleEchoService(Injector injector) {
		super(injector);
	}

	@Override
	public void doService(ChannelHandlerContext ctx, BettyServerContext bctx) throws Pausable, SuspendExecution, Exception {
		// TODO Auto-generated method stub

	}

}
