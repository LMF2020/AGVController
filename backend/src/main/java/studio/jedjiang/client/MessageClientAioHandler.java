package studio.jedjiang.client;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
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
	private MessageClient messageClient;
	private TaskService taskService;
	
	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}

	public ServerGroupContext getWsGroupCtx() {
		return wsGroupCtx;
	}

	public void setWsGroupCtx(ServerGroupContext wsGroupCtx) {
		this.wsGroupCtx = wsGroupCtx;
	}
	
	public void setMessageClient(MessageClient messageClient) {
		this.messageClient = messageClient;
	}

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
			AGVStatus taskStatus = AGVClient.updateCache(agvResponse);
			log.info("收到服务器消息：" + agvResponse);

			// 处理结束的任务
			if (taskStatus.isFinished()) {

				synchronized (this) {
					// 获取执行中的任务
					Task task = taskService.getOngoingTask();
					if (task != null) {
						// 如果数据库里执行中的任务和上报完成的任务是同一个任务，则设置状态为完成
						if(taskStatus.getTaskName().contains(task.getName())){
							task.setStatus(Task.TASK_FINISHED);
							taskService.update(task);
							
							// 然后取下一条待办任务
							Task nextTask = taskService.findNext();
							if (nextTask != null) {
								// 如果待办任务存在, 则发送任务
								messageClient.send(nextTask.getName());
								nextTask.setStatus(Task.TASK_IN_PROCESS);
								taskService.update(nextTask);
							} else {
								// 如果没有可执行的任务，回待办区
								
								// 判断任务是否在待命区
								String finishedTaskName = task.getName().replace(".xml", "").trim();
								if(finishedTaskName.endsWith("00") || finishedTaskName.endsWith("70")){
									// \\\\\如果在待命区， 则返回，不做处理
									return;
								}
								
								// 如果不在待命区, 则回待命区
								String startSiteTask= AGVClient.getCmdFromSiteToStartSite(finishedTaskName);
								messageClient.send(startSiteTask);
								taskService.addByStatus(startSiteTask,Task.TASK_IN_PROCESS);
							}
							
						}

					}

				}

			}

			// 不论任务是否完成，都会推送AGV状态给浏览器
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

	// 后台消息推送
	public void messagePush(AGVStatus taskStatus) throws UnsupportedEncodingException {
		if (taskStatus != null) {
			WsResponse resp = WsResponse.fromText(Json.toJson(taskStatus), MessagePacket.CHARSET);
			Tio.sendToAll(wsGroupCtx, resp);
		}
	}

}
