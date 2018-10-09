package io.betty.example.servicelets;

import co.paralleluniverse.fibers.SuspendExecution;
import io.betty.coders.StringProtocolPacket;
import io.betty.server.BettyServerContext;
import io.betty.server.BettyServicelet;
import kilim.Pausable;

/**
 * Just a simple test {@link BettyServicelet}. 
 * We recommend to put *Servicelet classes files into package <b>com.company_name.buziness_name.servicelets</b>
 */
public class SimpleEchoServicelet implements BettyServicelet {

	@Override
	public void doService(BettyServerContext bctx) throws Pausable, SuspendExecution, Exception {
		
		StringProtocolPacket req = bctx.getRequest();
		
		StringProtocolPacket rsp = new StringProtocolPacket(req.seq, req.uid, req.version, "world!!!");
		
		bctx.setResponse(rsp);
	}

}
