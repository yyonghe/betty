package io.betty.coders;

public class StringProtocolPacket {

	public final int seq;
	public final long uid;
	public final int version;
	public final String data;
	public StringProtocolPacket(int seq, long uid, int version, String data) {
		super();
		this.seq = seq;
		this.uid = uid;
		this.version = version;
		this.data = data;
	}
}
