package studio.jedjiang.client;

import org.tio.client.intf.ClientAioListener;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.intf.Packet;
import org.tio.server.ServerGroupContext;
import org.tio.utils.json.Json;
import org.tio.websocket.common.WsResponse;

import studio.jedjiang.bean.Result;

public class AgvClientListener implements ClientAioListener {

	private ServerGroupContext wsGroupCtx;
	
	public void setWsGroupCtx(ServerGroupContext wsGroupCtx) {
		this.wsGroupCtx = wsGroupCtx;
	}
	
	@Override
	public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect)
			throws Exception {
		if(!isConnected) {
			WsResponse resp = WsResponse.fromText(Json.toJson(Result.error()), MessagePacket.CHARSET);
			Tio.sendToAll(wsGroupCtx, resp);
			
			// 以下是车载测试数据模拟
//			AGVStatus taskStatus = new AGVStatus();
//			taskStatus.setBattery(50);
//			taskStatus.setError("0x00009");
//			taskStatus.setTaskName("F801172");
//			taskStatus.setX("-40500");
//			taskStatus.setY("-34545");
//			WsResponse resp = WsResponse.fromText(Json.toJson(Result.success("", taskStatus)), MessagePacket.CHARSET);
//			Tio.sendToAll(wsGroupCtx, resp);
			
			
		}
	}

	@Override
	public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize) throws Exception {

	}

	@Override
	public void onAfterReceivedBytes(ChannelContext channelContext, int receivedBytes) throws Exception {

	}

	@Override
	public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess) throws Exception {

	}

	@Override
	public void onAfterHandled(ChannelContext channelContext, Packet packet, long cost) throws Exception {

	}

	@Override
	public void onBeforeClose(ChannelContext channelContext, Throwable throwable, String remark, boolean isRemove)
			throws Exception {

	}

}
