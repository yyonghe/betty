package io.betty;

/**
 * Temporary holder class, this class defines which fields betty will be more attention.<p>
 * All {@link BettyProtocolcodec} should decodes out a instance of the class. 
 */
final public class BettyProtocolCodecOrigin {

	public final int seq;
	
	public final long uid;
	
	public final String cmd;
	
	public final String subcmd;
	
	public final Object data;
	
	public final int retCode;
	
	public final String retMessage;
	
	/**
	 * More useful when remote returns success.
	 * 
	 */
	public BettyProtocolCodecOrigin(int seq, long uid, String cmd, String subcmd, Object data) {
		this(seq, uid, cmd, subcmd, data, 0, "ok");
	}

	public BettyProtocolCodecOrigin(int seq, long uid, String cmd, String subcmd, Object data, int retCode, String retMessage) {
		this.seq = seq;
		this.uid = uid;
		this.cmd = cmd;
		this.subcmd = subcmd;
		this.data = data;
		this.retCode = retCode;
		this.retMessage = retMessage;
	}

}
