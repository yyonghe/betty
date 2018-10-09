package io.betty.util;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalSlf4JLoggerFactory {
	
	public static final org.apache.logging.log4j.Level DEFAULT_LEVEL = org.apache.logging.log4j.Level.DEBUG;
	
	public static final boolean DEFAULT_ENABLE_DUMP = false;
	
	private static org.apache.logging.log4j.Level level;
	
	private static boolean dump;
	
	private static Boolean INIT = true;
	
	public static Logger getLogger(Class<?> clazz) {
		if(INIT) {
			synchronized (INIT) {
				if(INIT) {
					init();
					INIT = false;
				}
			}
		}
		Logger logger = LoggerFactory.getLogger(clazz);
		Field field = null;
		try {
			field = org.apache.logging.slf4j.Log4jLogger.class.getDeclaredField("logger");
			field.setAccessible(true);
			org.apache.logging.log4j.core.Logger log4jLogger = 
						(org.apache.logging.log4j.core.Logger) field.get(logger);
			log4jLogger.setLevel(level);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(field != null) {
				field.setAccessible(false);
			}
		}
		return logger;
	}
	
	public static boolean isEnableDump() {
		return dump;
	}
	
	private static void init() {
		
		org.apache.logging.log4j.Level tlevel = DEFAULT_LEVEL;
		try {
			String status = System.getProperty("internnal.status");
			if(status != null && status.length() != 0) {
				tlevel = org.apache.logging.log4j.Level.toLevel(status, tlevel);
			}
			
		} catch (Exception e) {
			System.out.println("Configure internnal status failed " + e.getMessage());
		}
		level = tlevel;
		System.out.println("Configure internnal status with " + level);

		//
		boolean tdump = DEFAULT_ENABLE_DUMP;
		try {
			String dump = System.getProperty("internnal.status.enable.dump");
			if(dump != null && dump.length() != 0) {
				tdump = Boolean.valueOf(dump);
			}
		} catch (Exception ee) {
			System.out.println("Setup internnal dump failed " + ee.getMessage());
		}
		dump = tdump;
		System.out.println("Configure internnal status dump with " + dump);
	}
}
