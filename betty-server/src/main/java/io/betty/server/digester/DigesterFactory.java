package io.betty.server.digester;

import org.apache.commons.digester.Digester;
import org.slf4j.Logger;

import com.google.inject.Injector;

import io.betty.server.DefaultNamingService;
import io.betty.server.DefaultServer;
import io.betty.util.InternalSlf4JLoggerFactory;

public class DigesterFactory {
	
	private static final Logger logger = InternalSlf4JLoggerFactory.getLogger(DigesterFactory.class);

	public static Digester createDigester(Injector injector) {
		long t1 = System.currentTimeMillis();
		// Initialize the digester
		Digester digester = new Digester();
		digester.setValidating(false);
		
		// Configure the actions we will be using
		digester.addRule("Server", new ObjectHexAttrCreateRule(injector, DefaultServer.class.getName(), "cmd"));
		digester.addRule("Server/Listener", new LifecycleListenerRule(injector));
		// Executor
		digester.addRule("Server/Executor", new ExecutorCreateRule(injector));
		digester.addSetProperties("Server/Executor");
		digester.addSetNext("Server/Executor", "setExecutor");
		// Connector
		digester.addRule("Server/Connectors/Connector", new ConnectorCreateRule(injector));
		digester.addSetNext("Server/Connectors/Connector", "addConnector");
		// Properties
		digester.addRule("Server/Properties/Property", new PropertyCreateRule());
		// NamingServices
		digester.addRule("Server/NamingServices/NamingService", 
				new ObjectHexAttrCreateRule(injector, DefaultNamingService.class.getName(), "cmd", "subcmd"));
		digester.addSetNext("Server/NamingServices/NamingService", "addNamingService");
		// Services
		digester.addSetProperties("Server/Services"); //set server's service class name
		//
		digester.addRule("Server/Services/Service", new ServiceCreateRule(injector, "subcmd"));
		digester.addSetNext("Server/Services/Service", "addService");
		
		long t2=System.currentTimeMillis();
        if (logger.isDebugEnabled()) {
            logger.debug("Digester for server.xml created " + ( t2-t1 ));
        }
        
		return digester;
	}
}
