package io.betty.server.example;

import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Unit test for simple App.
 */
public class AppTest {

	@org.junit.Test
	public void testJarFileRead() throws Exception {
		
		ClassLoader classLoader = AppTest.class.getClassLoader();
		Enumeration<URL> urls = classLoader.getResources("META-INF");
		while(urls.hasMoreElements()) {
			URL url = urls.nextElement();
			if("jar".equals(url.getProtocol())) {
				String filepath = url.getFile();
				filepath = filepath.substring("file:".length(), filepath.indexOf("!"));
				System.out.println(filepath);
				JarFile jarFile = new JarFile(filepath);
				System.out.println(jarFile);
//				JarEntry jarEntry = jarFile.getJarEntry("META-INF/maven");
				
				Enumeration<JarEntry> jarEntries = jarFile.entries();
				while(jarEntries.hasMoreElements()) {
					JarEntry jarEntry = jarEntries.nextElement();
					String name = jarEntry.getName();
					if(!jarEntry.isDirectory() && name.startsWith("META-INF")) {
						String filename = name.substring(name.lastIndexOf("/") + 1, name.length());
						System.out.println(name);
						System.out.println(filename);
					}
				}
			}
		}
	}
	
}
