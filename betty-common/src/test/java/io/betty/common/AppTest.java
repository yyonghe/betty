package io.betty.common;

import io.betty.ant.tools.BettyScriptExportTask;
import io.betty.util.MiscUtils;

/**
 * Unit test for simple App.
 */
public class AppTest {
	
	@org.junit.Test
	public void testIndexOfArray() {
		
		int numOfPowerTwo = MiscUtils.ceilingNextPowerOfTwo(30);
		int mask = numOfPowerTwo - 1;
		
		System.out.println(numOfPowerTwo);
		System.out.println(0 & mask);
		System.out.println(1 & mask);
		System.out.println(numOfPowerTwo & mask);
		System.out.println(mask & mask);
		System.out.println(216549874 & mask);
		System.out.println(-216549874 & mask);
		System.out.println(-1 & mask);
		System.out.println(Integer.MIN_VALUE & mask);
		System.out.println(Integer.MAX_VALUE & mask);
	}
	
	@org.junit.Test
	public void testPropertyToken() {
		BettyScriptExportTask task = new BettyScriptExportTask();
		
		
		System.out.println(task.getTokens("_appname=${betty:app.appname}"));
	}
}
