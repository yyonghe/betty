package io.betty.util;

import java.io.File;

import org.apache.logging.log4j.LogManager;

import io.betty.util.InternalSlf4JLoggerFactory;

public class BettyLog4jInitializer {
	
	public BettyLog4jInitializer(String basedir) {
		File file = new File(basedir, "conf/log4j2.xml");
        if (file.exists() && file.isFile() && file.canRead()) {
        	try {
				String finalFilename = file.getAbsolutePath();
				System.setProperty("log4j.configurationFile", finalFilename);
				System.out.printf("Configure log4j with %s.\n", finalFilename);
				InternalSlf4JLoggerFactory.getLogger(BettyLog4jInitializer.class);
				LogManager.getRootLogger();
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
            
        }
        System.out.println("Configure log4j with classpath.");
    }
	
}
