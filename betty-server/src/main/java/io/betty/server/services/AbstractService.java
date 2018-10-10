package io.betty.server.services;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;

import com.google.inject.Injector;

import co.paralleluniverse.fibers.SuspendExecution;
import io.betty.lifecycle.Lifecycle;
import io.betty.lifecycle.LifecycleBase;
import io.betty.lifecycle.LifecycleException;
import io.betty.lifecycle.LifecycleState;
import io.betty.server.BettyServer;
import io.betty.server.BettyServerContext;
import io.betty.server.BettyService;
import io.betty.server.BettyServicelet;
import io.betty.util.InternalSlf4JLoggerFactory;
import io.netty.channel.ChannelHandlerContext;
import kilim.Pausable;

/**
 * The service implementation, the betty will use {@link DefaultProtocolBufferService} as default service. </br>
 * You can specified a custom service in server.xlm.
 */
public abstract class AbstractService extends LifecycleBase implements BettyService, Lifecycle {
	
	private static final Logger logger = InternalSlf4JLoggerFactory.getLogger(AbstractService.class);
	
	protected ThreadLocal<SimpleDateFormat> sdfLocal = new ThreadLocal<SimpleDateFormat>() {
		
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return sdf;
		};
	};
	
	/**
     * The name of this service.
     */
	private String name;
	
	private String subcmd;
	
	private BettyServer server;
	
	protected BettyServicelet servicelet;
	
	private Injector injector;
	
	public AbstractService(Injector injector) {
		this.injector = injector;
	}
	
	@Override
	public void service(ChannelHandlerContext ctx, BettyServerContext bctx) throws Pausable, SuspendExecution, Exception {
		try {
			doService(ctx, bctx);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try{
			server.publishAsyncEvent(this, LifecycleState.REQUEST_HANDLED_EVENT, bctx);
		} catch (Exception e) {
			logger.error("Exception on publish async request handled event", e);
		}
	}
	
	/**
	 * doService...
	 * @param ctx
	 * @param bctx
	 * @throws Pausable
	 * @throws Exception
	 */
	public abstract void doService(ChannelHandlerContext ctx, BettyServerContext bctx) throws Pausable, SuspendExecution, Exception;
	
	@Override
	public String formatflow(BettyServerContext bctx) throws Exception {
		SimpleDateFormat sdf = sdfLocal.get();
		Date now = new Date();
		sdf.format(now);
		StringBuilder sb = new StringBuilder();
		sb.append(sdf.format(new Date())).append(","); // request time
		sb.append(bctx.getSeq()).append(","); // sequence
		sb.append(bctx.getUid()).append(","); // user id
		sb.append(bctx.getCmd()).append(","); // cmd
		sb.append(bctx.getSubcmd()).append(","); // subcmd
		sb.append(bctx.getLocal()).append(","); // local ip
		sb.append(bctx.getRemote()).append(","); // remote ip
		sb.append(bctx.getStartTime()).append(","); // start time
		sb.append(now.getTime()).append(","); // now
		sb.append(now.getTime() - bctx.getStartTime()).append(","); // coast
		sb.append(bctx.getRetCode()).append(","); // ret code
		sb.append('<').append(bctx.getRetMessage()).append('>').append(',');
		sb.append('<').append(formatflowRequest(bctx)).append('>').append(',');
		sb.append('<').append(formatflowResponse(bctx)).append('>');
		return sb.toString();
	}
	
	protected String formatflowRequest(BettyServerContext bctx) {
		return bctx.getProtocolCoder().toString(bctx.getRequest());
	}
	
	protected String formatflowResponse(BettyServerContext bctx) {
		return bctx.getProtocolCoder().toString(bctx.getResponse());
	}

	@Override
	protected void initInternal() throws LifecycleException {
		
	}

	@Override
	protected void startInternal() throws LifecycleException {
		
	}

	@Override
	protected void stopInternal() throws LifecycleException {
		
	}

	@Override
	protected void destroyInternal() throws LifecycleException {
		
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the subcmd
	 */
	public String getSubcmd() {
		return subcmd;
	}
	
	/**
	 * @param subcmd the subcmd to set
	 */
	public void setSubcmd(String subcmd) {
		this.subcmd = subcmd;
	}

	@Override
	public BettyServer getServer() {
		return this.server;
	}

	@Override
	public void setServer(BettyServer server) {
		this.server = server;
	}
	
	/**
	 * @return the servicelet
	 */
	public String getServicelet() {
		return servicelet.getClass().getName();
	}

	/**
	 * @param servicelet the servicelet to set
	 */
	public void setServicelet(String servicelet) {
		try {
			//this.servicelet = (BettyServicelet) Class.forName(servicelet).newInstance();
			this.servicelet = (BettyServicelet) injector.getInstance(Class.forName(servicelet));
		} catch (Exception e) {
			throw new IllegalArgumentException("Create service let failed for " + servicelet, e);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[name=");
		builder.append(name);
		builder.append(", subcmd=");
		builder.append(subcmd);
		builder.append("]");
		return builder.toString();
	}
}
