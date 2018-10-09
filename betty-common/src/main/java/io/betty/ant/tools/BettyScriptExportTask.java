package io.betty.ant.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class BettyScriptExportTask extends Task {
	
	private String bindir;
	
	@Override
	public void execute() throws BuildException {
		try {
			ClassLoader classLoader = BettyScriptExportTask.class.getClassLoader();
			Enumeration<URL> urls = classLoader.getResources("META-INF/scripts");
			File basedirFile = new File(bindir);
			if(urls != null && urls.hasMoreElements()) {
				if(!basedirFile.exists()) {
					basedirFile.mkdirs();
				}
			}
			while(urls.hasMoreElements()) {
				URL url = urls.nextElement();
				if("jar".equals(url.getProtocol())) {
					// jar:file:/...jar!META-INF/scripts
					String filepath = url.getFile();
					filepath = filepath.substring("file:".length(), filepath.indexOf("!"));
					JarFile jarFile = new JarFile(filepath);
					Enumeration<JarEntry> jarEntries = jarFile.entries();
					while(jarEntries.hasMoreElements()) {
						JarEntry jarEntry = jarEntries.nextElement();
						String name = jarEntry.getName();
						if(!jarEntry.isDirectory() && name.startsWith("META-INF/scripts")) {
							doExport(classLoader, basedirFile, name);
						}
					}
					jarFile.close();
				} else {
					// file:/.../META-INF/scripts
					File file = new File(url.toURI());
					if(file.exists()) {
						for(File src : file.listFiles()) {
							doExport(classLoader, basedirFile, "META-INF/scripts/"+src.getName());
						}
					}
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
			throw new BuildException("BettyScriptExportTask execute failed", e);
		}
		
	}
	
	private void doExport(ClassLoader classLoader, File basedirFile, String resName) throws Exception {
		
		String filename = resName;
		int index = filename.lastIndexOf("/");
		if(index != -1) {
			filename = filename.substring(index + 1, filename.length());
		}
		File dst = new File(basedirFile, filename);
		log("Exporting script " + filename + " to " + dst.getAbsolutePath());
		InputStream input = classLoader.getResourceAsStream(resName);
		OutputStream output = new FileOutputStream(dst);
		byte[] buf = new byte[1024];
		int len = 0;
		while((len = input.read(buf)) != -1) {
			output.write(buf, 0, len);
		}
		output.flush();
		output.close();
		input.close();
	}

	/**
	 * @return the bindir
	 */
	public String getBindir() {
		return bindir;
	}

	/**
	 * @param bindir the bindir to set
	 */
	public void setBindir(String bindir) {
		this.bindir = bindir;
	}

}
