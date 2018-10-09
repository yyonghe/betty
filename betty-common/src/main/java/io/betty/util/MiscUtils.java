package io.betty.util;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

public class MiscUtils {
	
	/**
	 * request sequence generator.
	 */
	public static final AtomicInteger SG = new AtomicInteger(0);
	
	private static final int BITS_PER_INT = 32;

	public static void addLibraryDir(String dir) {
		Field field = null;
		try {
			field = ClassLoader.class.getDeclaredField("usr_paths");
			field.setAccessible(true);
			String[] paths = (String[]) field.get(null);
			for (int i = 0; i < paths.length; i++) {
				if (dir.equals(paths[i])) {
					return;
				}
			}
			String[] tmp = new String[paths.length + 1];
			System.arraycopy(paths, 0, tmp, 0, paths.length);
			tmp[paths.length] = dir;
			field.set(null, tmp);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to set library path: " + dir, e);
		} finally {
			if (field != null) {
				field.setAccessible(false);
			}
		}
	}

	public static String getLocalInet4Address(String ethName) {
		Enumeration<NetworkInterface> netInterfaces;
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = netInterfaces.nextElement();
				if(ni.getName().equals(ethName)) {
					Enumeration<InetAddress> addresses = ni.getInetAddresses();
					while (addresses.hasMoreElements()) {
						InetAddress ip = addresses.nextElement();
						if(ip instanceof java.net.Inet4Address) {
							return ip.getHostAddress();
						}
					}
				}
			}
		} catch (Exception e) {
			throw new IllegalStateException("Find eth exception " + ethName, e);
		}
		throw new IllegalStateException("No eth "+ethName+" found.");
	}

	/**
     * Calculate the next power of 2, greater than or equal to x.
     * <p>
     * From Hacker's Delight, Chapter 3, Harry S. Warren Jr.
     *
     * @param x Value to round up
     * @return The next power of 2 from x inclusive
     */
    public static int ceilingNextPowerOfTwo(final int x) {
        return 1 << (BITS_PER_INT - Integer.numberOfLeadingZeros(x - 1));
    }
}
