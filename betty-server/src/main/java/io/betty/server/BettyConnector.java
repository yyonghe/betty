package io.betty.server;

import io.betty.BettyProtocolCodec;
import io.betty.lifecycle.Lifecycle;

public interface BettyConnector extends Lifecycle {

	public void setHost(String host);
	
	public String getHost();
	
	public void setPort(int port);
	
	public int getPort();
	
	public void setKind(BettyConnector.Kind kind);
	
	public BettyConnector.Kind getKind();
	
	public void setProtocol(String protocol);
	
	public String getProtocol();
	
	public void setProtocolCodec(BettyProtocolCodec protocolcodec);
	
	public BettyProtocolCodec getProtocolCodec();
	
	public void setServer(BettyServer server);
	
	public BettyServer getServer();
	
	public static final String KIND_TYPE_TCP = "TCP";
	public static final String KIND_TYPE_UDP = "UDP";
	public static final String KIND_TYPE_HTTP = "HTTP";
	
	public static enum Kind {
		TCP(0, KIND_TYPE_TCP),
		UDP(1, KIND_TYPE_UDP),
		HTTP(2, KIND_TYPE_HTTP),
		
		UKNOWN(100, "UKNOWN");
		
		public final int type;
		public final String name;
		private Kind(int type, String name) {
			this.type = type;
			this.name = name;
		}
	}
}
