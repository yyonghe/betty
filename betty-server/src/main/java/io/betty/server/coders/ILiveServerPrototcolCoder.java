package io.betty.server.coders;

import java.util.Random;

import io.betty.BettyContext;
import io.betty.coders.ILivePrototcolCoder;
import io.betty.server.DefaultServerContext;

/**
 * Just a simple test {@link BettyServerProtocolCoder}. 
 * We recommend to put *PrototcolCoder classes files into package <b>com.company_name.buziness_name.coders</b>
 */
public class ILiveServerPrototcolCoder extends ILivePrototcolCoder {

	@Override
	public BettyContext unscramble(Object data) {
		DefaultServerContext bctx = new DefaultServerContext(new Random().nextInt(2100000000), "0", "231", data);
		return bctx;
	}

}
