package io.betty.server.bootstrap;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.commons.digester.Digester;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import io.betty.BettyExecutor;
import io.betty.BettyModuleListenerProvider;
import io.betty.BettyModuleProvider;
import io.betty.server.BettyServer;
import io.betty.server.digester.DigesterFactory;
import io.betty.util.BettyLog4jInitializer;

public class BettyServerBootStrap {
	
	private static final String BIN_DIR_SUFIX=File.separator + "bin" + File.separator + "."; // ...\bin\.
	
	private static final String PWD_DIR_SUFIX = File.separator + "."; // ...\.
	
	private static final String PWD;
	
	private static final String appname;
	
	private static final File serverXmlConfFile;
	
	private static BettyServer server = null;
	
	static {
		//init pwd
		String dir = System.getProperty("betty.work.dir");
		if(dir == null) {
			File file = new File(".");
			String pwd = file.getAbsolutePath();
			String  sufix = PWD_DIR_SUFIX;
			if(pwd.endsWith(BIN_DIR_SUFIX)) {
				sufix = BIN_DIR_SUFIX;
			}
			int index = pwd.lastIndexOf(sufix);
			pwd = pwd.substring(0, index);
			dir = pwd;
			//
			System.setProperty("betty.work.dir", dir);
		}
		//
		String name = System.getProperty("betty.appname");
		if(name == null || name.length() == 0) {
			int index = dir.lastIndexOf("/");
			index = index + 1;
			if(index < 0) {
				index = 0;
			}
			name = dir.substring(index, dir.length());
			//
			System.setProperty("betty.appname", name);
		}
		//
		PWD = dir;
		appname = name;
		System.out.printf("Server %s work in %s.\n", appname, PWD);
		//
		addLib2Classpath(PWD);
		//
		File file = new File(PWD, "conf/server.xml");
		serverXmlConfFile = file;
		System.out.printf("Configure server with %s.\n", file.getAbsolutePath());
		
		//init log4j
		new BettyLog4jInitializer(PWD);
	}

	
	public static void main(String[] args) throws Exception {
		
		Injector injector = Guice.createInjector(loadModules());
		
		Digester digester = DigesterFactory.createDigester(injector);
		
		BettyServer server = BettyServerBootStrap.server = injector.getInstance(BettyServer.class);

		startServer(injector, server, digester);
	}
	
	private static void startServer(Injector injector, BettyServer server,Digester digester) throws Exception {

		digester.push(server);
		digester.parse(serverXmlConfFile);
		
		if(server.getExecutor() == null) {
			server.setExecutor(injector.getInstance(BettyExecutor.class));
		}
		
		addModuleLifecycleListeners(server);
		
		try {
			server.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
//			try {
//				cobuziDigester.getServer().destroy();
//			} catch (LifecycleException e1) {
//				e1.printStackTrace();
//			}
		}
	}
	
	private static void addModuleLifecycleListeners(BettyServer server) {
		for(BettyModuleListenerProvider provider : ServiceLoader.load(BettyModuleListenerProvider.class)) {
			server.addLifecycleListener(provider.get());
		}
	}
	
	private static void addLib2Classpath(String pwd) {
		Method method = null;
		try {
			File libdir = new File(pwd, "lib");
			if(libdir.exists()) {
				File[] libs = libdir.listFiles();
				if(libs.length != 0) {
					
					ClassLoader loader = BettyServerBootStrap.class.getClassLoader();
					method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class }); 
					method.setAccessible(true);
					
					for(int i=0;i<libs.length;i++) {
						method.invoke(loader, libs[i].toURI().toURL());
					}
				}
			}
		} catch (Exception e) {
			System.out.printf("Add libriry to classpath failed with %s\n", e.getMessage());
			e.printStackTrace(System.out);
		} finally {
			if(method != null) {
				method.setAccessible(false);
			}
		}
	}
	
	private static Module[] loadModules() {
		List<Module> list = new ArrayList<Module>();
		for(BettyModuleProvider provider : ServiceLoader.load(BettyModuleProvider.class)) {
			list.add(provider.get());
		}
		return list.toArray(new Module[0]);
	}
	
//	private static Module[] loadModules() {
//		Map<String, Module> modulemapping = new HashMap<String, Module>();
//		modulemapping.put(BettyServerModule.class.getName(), new BettyServerModule());
//		String strExtra = System.getProperty("betty.extra.modules");
//		if(strExtra != null && strExtra.length() != 0) {
//			String[] items = strExtra.split(",");
//			for(String item : items) {
//				try {
//					String moduleClass = item.trim();
//					if(modulemapping.containsKey(moduleClass)) {
//						System.out.printf("Existed module %s skiped.\n", moduleClass);
//						continue;
//					}
//					Class<?> clazz = BettyServerBootStrap.class.getClassLoader().loadClass(moduleClass);
//					Module extrModule = (Module) clazz.newInstance();
//					modulemapping.put(moduleClass, extrModule);
//				} catch (Exception e) {
//					System.out.printf("Loadding module %s failed with %s\n", item, e.getMessage());
//					e.printStackTrace(System.out);
//					System.exit(1);
//				}
//			}
//		}
//		return modulemapping.values().toArray(new Module[0]);
//	}
	
	public static String getPwd() {
		return PWD;
	}
	
	public static BettyServer getServer() {
		return server;
	}
	
	public static String getAppName() {
		return appname;
	}
}
