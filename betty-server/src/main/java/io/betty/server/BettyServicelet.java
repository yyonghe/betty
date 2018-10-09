package io.betty.server;

import co.paralleluniverse.fibers.SuspendExecution;
import kilim.Pausable;


/**
 * Servicelet all sub class impl {@link BettyServicelet#toString()} for log.
 *
 */
public interface BettyServicelet {

	public void doService(BettyServerContext bctx) throws Pausable, SuspendExecution, Exception;

}
