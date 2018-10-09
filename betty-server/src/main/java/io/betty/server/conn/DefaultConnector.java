package io.betty.server.conn;

import org.slf4j.Logger;

import io.betty.BettyProtocolCoder;
import io.betty.lifecycle.LifecycleBase;
import io.betty.lifecycle.LifecycleException;
import io.betty.lifecycle.LifecycleState;
import io.betty.server.BettyConnector;
import io.betty.server.BettyServer;
import io.betty.util.InternalSlf4JLoggerFactory;
import io.netty.bootstrap.AbstractBootstrap;

public abstract class DefaultConnector extends LifecycleBase implements BettyConnector {
	
	private static final Logger logger = InternalSlf4JLoggerFactory.getLogger(DefaultConnector.class);
	
	private BettyServer server;

	private String host = "0.0.0.0";
	
	private int port = -1;
	
	private String protocol;

	private BettyConnector.Kind kind;

	private BettyProtocolCoder protocolCoder;

	private String options;
	
	protected int nWorkerThreads;
	
	@SuppressWarnings("rawtypes")
	protected AbstractBootstrap bootstrap;
	
	public BettyServer getServer() {
		return this.server;
	}

	public void setServer(BettyServer server) {
		this.server = server;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port number on which this connector is configured to listen
	 *         for requests. The special value of 0 means select a random free
	 *         port when the socket is bound.
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Set the port number on which we listen for requests.
	 *
	 * @param port The new port number
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the Coyote protocol handler in use.
	 */
	public String getProtocol() {
		return this.protocol;
	}

	/**
	 * Pause the connector.
	 */
	public void pause() {

	}

	/**
	 * Resume the connector.
	 */
	public void resume() {

	}

	@Override
	protected void initInternal() throws LifecycleException {
	}

	@Override
	protected void startInternal() throws LifecycleException {
		setState(LifecycleState.STARTING);
		//
		try {
			bootstrap.bind(host, port).sync();
		} catch (InterruptedException e) {
			logger.error("bootstrap bind InterruptedException", e);
		}

	}

	@Override
	protected void stopInternal() throws LifecycleException {
		setState(LifecycleState.STOPPING);
		
	}

	@Override
	protected void destroyInternal() throws LifecycleException {
	}

	/**
	 * @return the options
	 */
	public String getOptions() {
		return options;
	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(String options) {
		this.options = options;
	}

	/**
	 * @return the kind
	 */
	public BettyConnector.Kind getKind() {
		return kind;
	}

	/**
	 * @param kind
	 *            the kind to set
	 */
	public void setKind(BettyConnector.Kind kind) {
		this.kind = kind;
	}

	/**
	 * @return the protocolCoder
	 */
	public BettyProtocolCoder getProtocolCoder() {
		return protocolCoder;
	}

	/**
	 * @param protocolCoder
	 *            the protocolCoder to set
	 */
	public void setProtocolCoder(BettyProtocolCoder protocolCoder) {
		this.protocolCoder = protocolCoder;
	}

	/**
	 * @param protocol
	 *            the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * @return the bootstrap
	 */
	@SuppressWarnings("rawtypes")
	public AbstractBootstrap getBootstrap() {
		return bootstrap;
	}

	
	@Override
	public String getName() {
		return "connector:" + kind + ":" + host + ":" + port + "//[" + protocol + "]" + protocolCoder;
	}
	
	/**
	 * Provide a useful toString() implementation as it may be used when logging
	 * Lifecycle errors to identify the component.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[port=");
		builder.append(port);
		builder.append(", kind=");
		builder.append(kind);
		builder.append(", protocol=");
		builder.append(protocol);
		builder.append(", protocolCoder=");
		builder.append(protocolCoder);
		builder.append("]");
		return builder.toString();
	}
}
