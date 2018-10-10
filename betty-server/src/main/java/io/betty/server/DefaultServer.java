package io.betty.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import io.betty.BettyExecutor;
import io.betty.lifecycle.Lifecycle;
import io.betty.lifecycle.LifecycleBase;
import io.betty.lifecycle.LifecycleException;
import io.betty.lifecycle.LifecycleState;
import io.betty.server.services.DefaultProtocolBufferService;
import io.betty.server.services.ServiceAsyncEventLoop;

public class DefaultServer extends LifecycleBase implements BettyServer, Lifecycle {
	
	/**
     * The name of this service.
     */
	private String name;
	
	
	private int cmd;
	
	/**
     * The set of Connectors associated with this Server.
     */
	private BettyConnector connectors[] = new BettyConnector[0];

	
	private Properties properties = new Properties();
	
	private BettyExecutor executor;
	
	private String serviceClass = DefaultProtocolBufferService.class.getName();
	
	private ServiceAsyncEventLoop disruptor = new ServiceAsyncEventLoop();

	
	private Map<String, DefaultNamingService> namingServices = new HashMap<String, DefaultNamingService>();
	
	
	private Map<String, BettyService> services = new ConcurrentHashMap<String, BettyService>();
	
	private final Object connectorsLock = new Object();
	private final Object servicesLock = new Object();
	
	/**
     * Construct a default instance of this class.
     */
    public DefaultServer() {

    }
    
    public void publishAsyncEvent(final Lifecycle service, final LifecycleState state, final Object data) {
    	disruptor.publishAsyncEvent(service, state, data);
    }
    
    public void addService(BettyService service) {
    	synchronized (servicesLock) {
    		services.put(service.getSubcmd(), service);
		}
    }
    
    public void addNamingService(DefaultNamingService namingService) {
    	namingServices.put(namingService.getName(), namingService);
    }
    
    public void addProperty(String key, String value) {
    	properties.put(key, value);
    }
    
    public void addConnector(BettyConnector connector) {
    	synchronized (connectorsLock) {
    		BettyConnector results[] = new BettyConnector[connectors.length + 1];
            System.arraycopy(connectors, 0, results, 0, connectors.length);
            results[connectors.length] = connector;
            connectors = results;
		}
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
	 * @return the cmd
	 */
    public int getCmd() {
		return cmd;
	}
    
	/**
	 * @param cmd the cmd to set
	 */
    public void setCmd(int cmd) {
		this.cmd = cmd;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StandardServer [name=");
		builder.append(name);
		builder.append(", cmd=");
		builder.append(cmd);
		builder.append(", executor=");
		builder.append(executor);
		builder.append(", connectors=");
		builder.append(Arrays.toString(connectors));
		builder.append(", properties=");
		builder.append(properties);
		builder.append(", namingServices=");
		builder.append(namingServices);
		builder.append(", services=");
		builder.append(services);
		builder.append("]");
		return builder.toString();
	}

	@Override
	protected void initInternal() throws LifecycleException {
		//
		disruptor.init();
		// Init our defined Executor
        executor.init();

        // Init our defined Services
        synchronized (servicesLock) {
            for(BettyService service : services.values()) {
            	service.init();
            }
        }
        
        // Init our defined Services
        synchronized (connectorsLock) {
            for(BettyConnector connector : connectors) {
            	connector.init();
            }
        }
	}

	@Override
	protected void startInternal() throws LifecycleException {
        //
        disruptor.handleEventsWith(findLifecycleListeners());
        disruptor.start();
        //
        executor.start();

        // Start our defined Services
        synchronized (servicesLock) {
        	for(BettyService service : services.values()) {
            	service.start();
            }
        }
        
        // Init our defined Services
        synchronized (connectorsLock) {
            for(BettyConnector connector : connectors) {
            	connector.start();
            }
        }
	}

	@Override
	protected void stopInternal() throws LifecycleException {
		//
		disruptor.stop();
        // Init our defined Services
        synchronized (connectorsLock) {
            for(BettyConnector connector : connectors) {
            	connector.stop();
            }
        }
        
        // Start our defined Services
        synchronized (servicesLock) {
        	for(BettyService service : services.values()) {
            	service.stop();
            }
        }
        
        executor.stop();
	}

	@Override
	protected void destroyInternal() throws LifecycleException {
		// Init our defined Services
        synchronized (connectorsLock) {
            for(BettyConnector connector : connectors) {
            	connector.destroy();
            }
        }
        
        // Start our defined Services
        synchronized (servicesLock) {
        	for(BettyService service : services.values()) {
            	service.destroy();
            }
        }
        
        executor.destroy();
        //
        disruptor.destroy();
	}

	@Override
	public BettyService findService(String sumcmd) {
		BettyService service = services.get(sumcmd);
		return service;
	}

	@Override
	public BettyService[] findServices() {
		BettyService[] services = null;
		synchronized(servicesLock) {
			services = new BettyService[this.services.size()];
			int index = 0;
			for(BettyService service : this.services.values()) {
				services[index++] = service;
			}
		}
		return services;
	}

	@Override
	public void removeService(BettyService service) {
		synchronized (servicesLock) {
			services.remove(service.getSubcmd());
		}
	}

	@Override
	public void setExecutor(io.betty.BettyExecutor executor) {
		this.executor = executor;
	}

	@Override
	public io.betty.BettyExecutor getExecutor() {
		return executor;
	}

	/**
	 * @return the serviceClass
	 */
	public String getServiceClass() {
		return serviceClass;
	}

	/**
	 * @param serviceClass the serviceClass to set
	 */
	public void setServiceClass(String serviceClass) {
		this.serviceClass = serviceClass;
	}

}
