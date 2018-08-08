package studio.jedjiang.client;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Date;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.Mvcs;
import org.tio.client.intf.ClientAioHandler;
import org.tio.core.ChannelContext;
import org.tio.core.GroupContext;
import org.tio.core.Tio;
import org.tio.core.exception.AioDecodeException;
import org.tio.core.intf.Packet;
import org.tio.server.ServerGroupContext;
import org.tio.utils.json.Json;
import org.tio.websocket.common.WsResponse;

import studio.jedjiang.bean.AGVStatus;
import studio.jedjiang.bean.Task;
import studio.jedjiang.service.TaskService;

@IocBean
public class MessageClientAioHandler implements ClientAioHandler {

	private static final Log log = Logs.get();
	private static MessagePacket heartbeatPacket = new MessagePacket();

	// 配置websocket服务
	private ServerGroupContext wsGroupCtx;
	
	private static TaskService taskService = Mvcs.ctx().getDefaultIoc().get(TaskService.class);
	private static MessageClient messageClient = Mvcs.ctx().getDefaultIoc().get(MessageClient.class);

	/**
	 * 接收端解码: 字节转换成可发送的消息
	 */
	@Override
	public Packet decode(ByteBuffer buffer, int limit, int position, int readableLength, ChannelContext channelContext) throws AioDecodeException {
		
		MessagePacket imPacket = new MessagePacket();

		int bodyLength = buffer.limit();
		if (bodyLength > 0) {
			byte[] dst = new byte[bodyLength];
			buffer.get(dst);
			imPacket.setBody(dst);
		}
		return imPacket;
	}

	/**
	 * 发送端转码: 消息转换成可发送的字节
	 */
	@Override
	public ByteBuffer encode(Packet packet, GroupContext groupContext, ChannelContext channelContext) {

		MessagePacket imPacket = (MessagePacket) packet;
		byte[] body = imPacket.getBody();
		int bodyLen = 0;
		if (body != null) {
			bodyLen = body.length;
		}
		//创建一个新的bytebuffer
		ByteBuffer buffer = ByteBuffer.allocate(bodyLen);
		//设置字节序
		buffer.order(groupContext.getByteOrder());
		
		// 写入消息体
		if (body != null) {
			buffer.put(body);
		}

		return buffer;
	}

	/**
	 * 客户端处理消息: 监听服务端发送过来的消息
	 */
	@Override
	public void handler(Packet packet, ChannelContext channelContext) throws Exception {
		MessagePacket imPacket = (MessagePacket) packet;
		byte[] body = imPacket.getBody();
		if (body != null) {
			String agvResponse = new String(body, MessagePacket.CHARSET);
			// 更新缓存
			AGVStatus taskStatus = AGVClient.updateAGVStatus(agvResponse);
			log.info("收到服务器消息：" + agvResponse);

			// 如果任务结束
			if (taskStatus.isFinished()) {

				synchronized (this) {
					// 试图取进行中的任务
					Task task = taskService.getOngoingTask();
					if (task != null) {
						long now = new Date().getTime();
						// 如果上一条数据更新时间还没到10秒就又过来一条完成的任务的话, 这条任务极有可能是仍旧是上条完成的任务！
						if (now - task.getOpAt() < 10 * 1000) {
							// 返回不做处理
							return;
						}
						// 否则更新任务状态为完成
						task.setStatus(Task.TASK_FINISHED);
						taskService.update(task);
					}

					// 试图取一条新任务发送
					task = taskService.findNext();
					if (task != null) {
						messageClient.send(task.getName());
						// 更新任务状态为：执行中
						task.setStatus(Task.TASK_IN_PROCESS);
						taskService.update(task);
					}
				}

			}

			// 推送给浏览器
			messagePush(taskStatus);
		}
		return;
	}
	
	/**
	 * 如果返回null，框架层面则不会发心跳；如果返回非null，框架层面会定时发本方法返回的消息包
	 */
	@Override
	public Packet heartbeatPacket() {
		return heartbeatPacket;
	}

	public ServerGroupContext getWsGroupCtx() {
		return wsGroupCtx;
	}

	public void setWsGroupCtx(ServerGroupContext wsGroupCtx) {
		this.wsGroupCtx = wsGroupCtx;
	}

	// 后台消息推送
	public void messagePush(AGVStatus taskStatus) throws UnsupportedEncodingException {
		if (taskStatus != null) {
			WsResponse resp = WsResponse.fromText(Json.toJson(taskStatus), MessagePacket.CHARSET);
			Tio.sendToAll(wsGroupCtx, resp);
		}
	}

}
