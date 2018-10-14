package io.betty.ant.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class BettyScriptExportTask extends Task {
	
	private static final Pattern PATTERN = Pattern.compile("\\$\\{betty:([_a-zA-Z]+[\\._a-zA-Z0-9]+)\\}");
	
	private String bindir;
	
	private String basedir;
	
	private String appname;
	
	private String version;
	
	private String jvmargs;
	
	private String appargs;
	
	private Properties props = new Properties();
	
	@Override
	public void execute() throws BuildException {
		try {
			
			if(basedir == null || basedir.length() == 0) {
				throw new IllegalAccessException("basedir is empty.");
			}
			
			if(bindir == null || bindir.length() == 0) {
				bindir = basedir + "/bin";
			}
			
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
		if("betty.sh".equals(filename) || "betty.bat".equals(filename)) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream(resName)));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dst)));
			
			try {
				String line = null;
				
				while((line = reader.readLine()) != null) {
					List<Token> tokens = getTokens(line);
					
					for(Token token : tokens) {
						String value = props.getProperty(token.name);
						if(value == null) {
							throw new IllegalArgumentException("No value set to var " + token.name);
						}
						log("Exporting script " + filename + " resolved variable " + token.name + "=\"" + value + "\"");
						line = line.replace(token.fullname, value);
					}
					//
					writer.write(line);
					writer.write("\n");
				}
			} finally {
				if(writer != null) {
					writer.flush();
					writer.close();
				}
				if(reader != null) {
					reader.close();
				}
			}
			
		} else {
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
	}
	
	public List<Token> getTokens(String line) {
		
		List<Token> tokens = new ArrayList<Token>();
		
		Matcher matcher = PATTERN.matcher(line);
		int start = 0;
		while(matcher.find(start)) {

			tokens.add(new Token(matcher.group(0), matcher.group(1)));
			//
			start = matcher.end();
		}
		//
		return tokens;
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
	
	/**
	 * @return the basedir
	 */
	public String getBasedir() {
		return basedir;
	}

	/**
	 * @param basedir the basedir to set
	 */
	public void setBasedir(String basedir) {
		this.basedir = basedir;
	}
	
	/**
	 * @return the appname
	 */
	public String getAppname() {
		return appname;
	}

	/**
	 * @param appname the appname to set
	 */
	public void setAppname(String appname) {
		this.appname = appname;
		//
		props.put("app.appname", appname);
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
		//
		props.put("app.version", version);
	}

	/**
	 * @return the jvmargs
	 */
	public String getJvmargs() {
		return jvmargs;
	}

	/**
	 * @param jvmargs the jvmargs to set
	 */
	public void setJvmargs(String jvmargs) {
		this.jvmargs = jvmargs;
		//
		props.put("app.jvmargs", jvmargs);
	}

	/**
	 * @return the appargs
	 */
	public String getAppargs() {
		return appargs;
	}

	/**
	 * @param appargs the appargs to set
	 */
	public void setAppargs(String appargs) {
		this.appargs = appargs;
		//
		props.put("app.appargs", appargs);
	}



	static class Token {
		public final String fullname;
		public final String name;
		public Token(String fullname, String name) {
			super();
			this.fullname = fullname;
			this.name = name;
		}
		@Override
		public String toString() {
			return fullname + ", " + name;
		}
	}

}
