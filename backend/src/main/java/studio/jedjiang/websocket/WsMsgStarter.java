package studio.jedjiang.websocket;

import java.io.IOException;

import org.tio.server.ServerGroupContext;
import org.tio.websocket.server.WsServerStarter;

/**
 * Websocket推送服务端
 * 
 * @author Jed
 *
 */
public class WsMsgStarter {

	public static void main(String[] args) throws IOException {
		WsMsgStarter appStarter = new WsMsgStarter(9321, new WsMsgHandler());
		appStarter.start();
	}

	private ServerGroupContext serverGroupContext;
	private WsServerStarter wsServerStarter;

	public WsMsgStarter(int port, WsMsgHandler wsMsgHandler) throws IOException {
		wsServerStarter = new WsServerStarter(port, wsMsgHandler);
		serverGroupContext = wsServerStarter.getServerGroupContext();
	}

	public ServerGroupContext getServerGroupContext() {
		return serverGroupContext;
	}

	public WsServerStarter getWsServerStarter() {
		return wsServerStarter;
	}

	public void start() throws IOException {
		wsServerStarter.start();
	}
}
