package studio.jedjiang.client;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

import cn.hutool.core.thread.ThreadUtil;
import studio.jedjiang.bean.AGVStatus;
import studio.jedjiang.bean.Result;
import studio.jedjiang.bean.Task;
import studio.jedjiang.service.TaskService;
/**
 * 消息处理核心逻辑
 * @author Jed
 *
 */
@IocBean
public class MessageClientAioHandler implements ClientAioHandler {

	private static final Log log = Logs.get();
	private static MessagePacket heartbeatPacket = new MessagePacket();
	
	private Lock lock = new ReentrantLock();

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
			
			// 报文解析失败不处理
			if(taskStatus == null) {
				return;
			}

			// 如果是充电任务:
			// 1. 如果电量达到99%，结束任务
			// 2. 如果电量达到40%并且有待办任务，结束任务
			if (taskStatus.getTaskName().replace(".xml", "").trim().endsWith("81")) {
				
				// 3. 充电任务不会上报结束状态，结束状态只代表站点->充电桩任务结束
				if(taskStatus.isFinished()){
					taskStatus.setFinished(false);
				}
				
				int battery = taskStatus.getBattery();
				boolean isChargeFull = battery >= AGVClient.CHARGE_FULL_MAX_VAL;
				boolean isChargeReadyToAcceptTask = (battery >= AGVClient.CHARGE_RECOVER_MIN_VAL) && AGVClient.hasNextTask;
				if (isChargeFull || isChargeReadyToAcceptTask) {
					if (isChargeFull) {
						log.infof("电已充满，当前电量：%d，目标电量：%d", battery, AGVClient.CHARGE_FULL_MAX_VAL);
					}
					if (isChargeReadyToAcceptTask) {
						log.infof("电已达标，可继续接任务，当前电量：%d，目标电量：%d", battery, AGVClient.CHARGE_RECOVER_MIN_VAL);
					}
					taskStatus.setFinished(true);
				}
			}
			
			// 处理结束的任务
			if (taskStatus.isFinished()) {
				// 线程锁并发控制
				if(lock.tryLock()) {
					try {
						log.debugf("任务完成：%s" , agvResponse);
						handleFinished(taskStatus);
					} finally {
						lock.unlock();
					}
				}
			}

			// 不论任务是否完成，都会推送AGV状态给浏览器
			messagePush(taskStatus);
		}
		return;
	}
	
	private void handleFinished (AGVStatus taskStatus) throws Exception {
		// 获取执行中的任务
		Task onGoingTask = taskService.getOngoingTask();
		if (onGoingTask != null) {
			// 如果数据库里执行中的任务和上报完成的任务是同一个任务，则设置状态为完成
			if(taskStatus.getTaskName().contains(onGoingTask.getName())){
				
				// 设置完成状态前，删除所有之前已完成的任务
				taskService.clearFinished();
				onGoingTask.setStatus(Task.TASK_FINISHED);
				taskService.update(onGoingTask);
				
				// 电量小于20%需要充电
				if(taskStatus.getBattery() < AGVClient.CHARGE_LOWER_MIN_VAL){
					log.infof("当前任务完成:%s，但电量低于临界值，准备发送充电任务", taskStatus.getTaskName());
					String chargeTask = AGVClient.getFromSiteToChargeSite(onGoingTask.getName());
					handleChargeTask(chargeTask, taskStatus.getBattery());
					// \\\\\充电情况处理完毕，返回
					return;
				}
				
				// 然后取下一条待办任务
				Task waitingTask = taskService.findNext();
				if (waitingTask != null) {
					handleTodoTask(waitingTask);
				} else {
					// 如果没有可执行的任务，回待命区
					// 条件：判断任务是否在待命区
					autoReturnBack(onGoingTask.getName());
				}
			}

		} else {
			// 任务完成后，且没有下个任务，当电量低于临界值，就去充电
			String finishedTaskName = taskStatus.getTaskName().replace(".xml", "").trim();
			// 是否在待命区
			boolean isStartSite = finishedTaskName.endsWith("00") || finishedTaskName.endsWith("80");
			// 电量是否低于临界值
			boolean isLowBettery = taskStatus.getBattery() < AGVClient.CHARGE_LOWER_MIN_VAL;
			log.infof("已完成任务:%s, 是否在待命区:" + isStartSite + ",是否需要充电:" + isLowBettery + ", 设备电量：%d, 低电量临界值：%d", finishedTaskName, taskStatus.getBattery(), AGVClient.CHARGE_LOWER_MIN_VAL);
			if(!AGVClient.hasNextTask && isStartSite && isLowBettery){
				// 可以安排去充电了
				log.infof("设备在待命区，电量低于临界值，准备发送充电任务，当前任务:%s", taskStatus.getTaskName());
				String chargeTask = AGVClient.getFromSiteToChargeSite(finishedTaskName);
				handleChargeTask(chargeTask, taskStatus.getBattery());
			}
		}
	}
	
	private void handleTodoTask(Task waitingTask) throws Exception {
		// 然后取下一条待办任务
		if (waitingTask != null) {
			log.infof("发送待办任务：%s", waitingTask.getName());
			// 如果待办任务存在, 则发送任务
			Result r = messageClient.send(waitingTask.getName());
			if(r.getCode() == 0) {
				waitingTask.setStatus(Task.TASK_IN_PROCESS);
				taskService.update(waitingTask);
				ThreadUtil.safeSleep(TimeUnit.SECONDS.toMillis(1));
			}
		}
	}
	
	private void handleChargeTask(String chargeTask, int betteryLeft) throws Exception{
		// 1，新增充电任务
		taskService.addByStatus(chargeTask, Task.TASK_IN_PROCESS);
		log.infof("发送充电任务:%s, 当前电量:%d", chargeTask, betteryLeft);
		// 2，发送新增的充电任务
		Result r = messageClient.send(chargeTask);
		if(r.getCode() == 0) {
			// TDDO: 发送成功后，清空所有的待办任务
			taskService.clearTodo();
			ThreadUtil.safeSleep(TimeUnit.SECONDS.toMillis(1));
		}
		// \\\\\充电情况处理完毕，返回
	}
	
	// 没有任务自动回待命区
	private void autoReturnBack(String taskName) {

		// 判断任务是否在待命区
		String finishedTaskName = taskName.replace(".xml", "").trim();
		if(finishedTaskName.endsWith("00") || finishedTaskName.endsWith("80") || taskName.equals(AGVClient.NO_TASK)){
			// \\\\\如果在待命区， 则返回，不做处理
			return;
		}
		
		// 无待办任务自动回到待命区
		String autoBackTask = AGVClient.getBackSite(finishedTaskName);
		Result r = messageClient.send(autoBackTask);
		try {
			if (r.getCode() == 0) {
				log.infof("无待办任务，自动回到待命区任务发送成功:%s", autoBackTask);
				taskService.addByStatus(autoBackTask, Task.TASK_IN_PROCESS);
				ThreadUtil.safeSleep(TimeUnit.SECONDS.toMillis(1));
			} else {
				log.errorf("无待办任务，自动回待命区任务发送失败: %s", autoBackTask);
			}
		} catch (Exception e) {
			// e.printStackTrace();
			log.errorf("无待办任务，自动回待命区任务发送失败, 上次任务 : %s", autoBackTask);
		}
	
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
