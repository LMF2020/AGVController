package studio.jedjiang.websocket;

import java.nio.ByteBuffer;

import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.websocket.common.WsRequest;
import org.tio.websocket.server.handler.IWsMsgHandler;

public class WsMsgHandler implements IWsMsgHandler {

	/**
	 * 握手时走这个方法，业务可以在这里获取cookie，request参数等
	 */
	@Override
	public HttpResponse handshake(HttpRequest request, HttpResponse httpResponse, ChannelContext channelContext)
			throws Exception {
		// 握手时触发
		return httpResponse;
	}

	@Override
	public void onAfterHandshaked(HttpRequest arg0, HttpResponse arg1, ChannelContext arg2) throws Exception {
		// 握手后触发
	}
	
	/**
	 * 字节消息（binaryType = arraybuffer）过来后会走这个方法
	 */
	@Override
	public Object onBytes(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
		// String ss = new String(bytes, "utf-8");
		// byte[] bs1 = "收到byte消息".getBytes("utf-8");
		ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
		buffer.put(bytes);

		return buffer;
	}

	/**
	 * 当客户端发close flag时，会走这个方法
	 */
	@Override
	public Object onClose(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
		Tio.remove(channelContext, "receive close flag");
		return null;
	}

	/**
	 * 字符消息（binaryType = blob）过来后会走这个方法
	 */
	@Override
	public Object onText(WsRequest wsRequest, String text, ChannelContext channelContext) throws Exception {
		// 不需要处理客户端的消息，我这边只实现服务端的推送逻辑
		return "ws server receives client message:" + text;
	}

}
