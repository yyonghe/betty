package io.betty.server.digester;

import java.util.HashMap;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.digester.Rule;
import org.slf4j.Logger;
import org.xml.sax.Attributes;

import com.google.inject.Injector;

import io.betty.BettyProtocolCoder;
import io.betty.server.BettyConnector;
import io.betty.server.BettyServer;
import io.betty.server.conn.DefaultTcpConnector;
import io.betty.server.conn.DefaultTcpEpollConnector;
import io.betty.server.conn.DefaultUdpConnector;
import io.betty.server.conn.DefaultUdpEpollConnector;
import io.betty.util.InternalSlf4JLoggerFactory;
import io.betty.util.MiscUtils;

/**
 * Rule implementation that creates a connector.
 */

public class ConnectorCreateRule extends Rule {
	
	private Injector injector;

	private static Logger logger = InternalSlf4JLoggerFactory.getLogger(ConnectorCreateRule.class);
	
	public ConnectorCreateRule(Injector injector) {
		this.injector = injector;
	}
	
	// --------------------------------------------------------- Public Methods

    /**
     * Process the beginning of this element.
     *
     * @param namespace the namespace URI of the matching element, or an
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just
     *   the element name otherwise
     * @param attributes The attribute list for this element
     */
    @Override
    public void begin(String namespace, String name, Attributes attributes)
            throws Exception {

    	BettyConnector conn = null;
    	//
    	boolean tryepoll = false;
    	String tryepolls = attributes.getValue("tryEpoll");
    	if(tryepolls != null) {
    		try {
    			tryepoll = Boolean.valueOf(tryepolls);
    		} catch (Exception e) {
    			logger.warn("Invalide tryepoll config true or false?: {}", tryepolls);
    		}
    	}
    	if(tryepoll) {
    		tryepoll = io.netty.channel.epoll.Epoll.isAvailable();
    		if(!tryepoll) {
    			logger.warn("Setting to try epoll, but <{}>", io.netty.channel.epoll.Epoll.unavailabilityCause().getCause().getMessage());
    		}
    	}
    	String kind = attributes.getValue("kind").trim().toUpperCase();
    	
    	BettyConnector.Kind okind = null;
    	
        if(BettyConnector.KIND_TYPE_TCP.equals(kind)) {
        	okind = BettyConnector.Kind.TCP;
        	//
        	if(tryepoll) {
        		conn = injector.getInstance(DefaultTcpEpollConnector.class);
        	} else {
        		conn =injector.getInstance(DefaultTcpConnector.class);
        	}
		} else if(BettyConnector.KIND_TYPE_UDP.equals(kind)) {
			okind = BettyConnector.Kind.UDP;
			//
			if(tryepoll) {
				conn = injector.getInstance(DefaultUdpEpollConnector.class);
			} else {
				conn = injector.getInstance(DefaultUdpConnector.class);
			}
		} else if(BettyConnector.KIND_TYPE_HTTP.equals(kind)) {
			okind = BettyConnector.Kind.HTTP;
			
		} else {
			okind = BettyConnector.Kind.UKNOWN;
			Class<?> clazz = Class.forName(kind);
			conn = (BettyConnector) injector.getInstance(clazz);
		}
		conn.setKind(okind);
		conn.setServer((BettyServer) digester.peek());
		//
//		if(PlatformDependent.isWindows()) {
//			conn.setHost(MiscUtils.getLocalInet4Address("eth1"));
//		} else {
//			conn.setHost(MiscUtils.getLocalInet4Address("eth0"));
//		}
		// Build a set of attribute names and corresponding values
        HashMap<String, String> values = new HashMap<String, String>();
        //
    	int al = attributes.getLength();
		for (int i = 0; i < al; i++) {
    		String qn = attributes.getQName(i).trim().toLowerCase();
    		String value = attributes.getValue(i).trim();
    		if("kind".equals(qn) || "protocolCoder".equals(qn)) {
    			// do nothing...
    			continue;
    		} else if("protocol".equals(qn)) {
    			BettyProtocolCoder pc = null;
    			value = value.toUpperCase();
    			if("ILIVE".equals(value)) {
    				pc = injector.getInstance(io.betty.coders.ILivePrototcolCoder.class);
    			} else if("HTTP/1.1".equals(value)) {
    				pc = injector.getInstance(io.betty.coders.ILivePrototcolCoder.class);
    			} else if("STRING".equals(value)) {
    				pc = injector.getInstance(io.betty.server.coders.StringServerProtocolCoder.class);
    			} else {
    				
    			}
    			conn.setProtocol(value);
    			conn.setProtocolCoder(pc);
    		} else if("eth".equals(qn)) {
    			value = value.toLowerCase();
    			String host = MiscUtils.getLocalInet4Address(value);
    			conn.setHost(host);
    		} else {
    			values.put(qn, value);
    		}
    	}
		//
		BeanUtils.populate(conn, values);
		//
		digester.push(conn);
    }

    @Override
    public void end(String namespace, String name) throws Exception {
    	digester.pop();
    }
}
