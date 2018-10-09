package io.betty.server.example;

import java.util.Random;

import io.betty.BettyClientContext;
import io.betty.client.DefaultClient;
import io.betty.coders.StringProtocolCoder;
import io.betty.coders.StringProtocolPacket;
import io.betty.kilim.KilimResultWaitStrategy;
import junit.framework.TestCase;

public class TestUdpClient extends TestCase {

//	private static final Logger logger = InternalSlf4JLoggerFactory.getLogger(TestClient.class);

	public void testRun() throws Exception {
		
		DefaultClient client = new DefaultClient(new String[]{"192.168.215.129"}, 
				new int[] {8087}, new KilimResultWaitStrategy(), new StringProtocolCoder());
		client.useUdp();
		
		StringProtocolPacket req = new StringProtocolPacket(new Random().nextInt(2100000000),
				new Random().nextInt(2100000000), 232, "hello!!!");
		
		BettyClientContext reqctx = client.send(req, req.seq, req.uid, 100);
		
		try {
			StringProtocolPacket rsp = client.waitFor(reqctx);
			
			System.out.println("rrrrrr: " + reqctx.getProtocolCoder().toString(rsp));
		} catch (Exception e) {
			e.printStackTrace();
		}

		Thread.sleep(2000);
		
		client.shutdown();
	}
	
}
