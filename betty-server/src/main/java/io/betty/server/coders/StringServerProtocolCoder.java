package io.betty.server.coders;

import io.betty.BettyContext;
import io.betty.coders.StringProtocolCoder;
import io.betty.coders.StringProtocolPacket;
import io.betty.server.DefaultServerContext;

public class StringServerProtocolCoder extends StringProtocolCoder {

	@Override
	public BettyContext unscramble(Object data) {
		StringProtocolPacket packet = (StringProtocolPacket) data;
		DefaultServerContext bctx = new DefaultServerContext(packet.uid, "0", "231", packet);
		return bctx;
	}
}
