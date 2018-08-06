package studio.jedjiang.client;

import org.tio.core.intf.Packet;

/**
 * 消息报文
 * @author Jed
 *
 */
public class MessagePacket extends Packet {
	private static final long serialVersionUID = -172060606924066412L;
	public static final String CHARSET = "utf-8";
	private byte[] body;

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}
}
