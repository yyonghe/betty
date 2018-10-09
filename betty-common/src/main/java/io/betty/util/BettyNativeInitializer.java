package io.betty.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import io.netty.util.internal.PlatformDependent;

public class BettyNativeInitializer {
	
	public BettyNativeInitializer(String basedir) {
		File file  = new File(basedir, "tmp");
		if(!file.exists()) {
			file.mkdirs();
		} else {
			for(File tFile : file.listFiles()) {
				if(tFile.isDirectory()) {
					clearTmpDir(tFile);
				} else {
					tFile.delete();
				}
			}
		}
		System.setProperty("java.io.tmpdir", file.getAbsolutePath());
		final File toPath = createTmpDir();
		toPath.deleteOnExit();
		tryLoadNettyEpoll(toPath);
		MiscUtils.addLibraryDir(toPath.getAbsolutePath());
    }
	
	private static boolean tryLoadNettyEpoll(File tmpDir) {
		String staticLibName = "netty_transport_native_epoll";
        String sharedLibName = staticLibName + '_' + PlatformDependent.normalizedArch();
        String libname = System.mapLibraryName(sharedLibName);
        String path = "META-INF/native/" + libname;
        
        InputStream in = null;
        OutputStream out = null;
        File tmpFile = null;
        URL url = BettyNativeInitializer.class.getClassLoader().getResource(path);
        if (url == null) {
            url = ClassLoader.getSystemResource(path);
        }
        if(url != null) {
        	try {
        		tmpFile = new File(tmpDir, libname);
				in = url.openStream();
				out = new FileOutputStream(tmpFile);

				byte[] buffer = new byte[8192];
				int length;
				while ((length = in.read(buffer)) > 0) {
				    out.write(buffer, 0, length);
				}
				out.flush();
				out.close();
				out = null;
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        return false;
	}
	
	
	private static void clearTmpDir(File file) {
		if(file.isDirectory()) {
			for(File tFile : file.listFiles()) {
				if(tFile.isDirectory()) {
					clearTmpDir(tFile);
				} else {
					tFile.delete();
				}
			}
		}
		file.delete();
	}
	
	private static File createTmpDir() {
		File temp = null;
		try {
			temp = File.createTempFile("tmp_", Long.toString(System.nanoTime()));
			if (!temp.delete() || !temp.mkdir())
				return null;
		} catch (IOException e) {
			// ignore
			System.out.println("Create tmp dir error.");
			e.printStackTrace(System.out);
			System.exit(-1);
		}
		System.out.printf("Configure native with %s.\n", temp.getAbsolutePath());
		return temp;
	}
	
}
