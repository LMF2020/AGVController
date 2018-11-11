package studio.jedjiang.client;

import java.io.UnsupportedEncodingException;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.tio.client.ClientChannelContext;
import org.tio.client.ClientGroupContext;
import org.tio.client.ReconnConf;
import org.tio.client.TioClient;
import org.tio.client.intf.ClientAioListener;
import org.tio.core.Node;
import org.tio.core.Tio;
import org.tio.server.ServerGroupContext;

import studio.jedjiang.bean.AGVStatus;
import studio.jedjiang.bean.Result;
import studio.jedjiang.service.TaskService;


/**
 * Created by Jed on 2018/08/04
 * 
 * @author Jed
 *
 */
@IocBean
public class MessageClient {

	private static final Log log = Logs.get();

	@Inject
	protected PropertiesProxy conf;

	// handler, 包括编码、解码、消息处理
	@Inject
	private MessageClientAioHandler messageClientAioHandler;

	// 事件监听器，可以为null，但建议自己实现该接口，可以参考showcase了解些接口
	private ClientAioListener aioListener = null;

	// 断链后自动连接的，不想自动连接请设为null
	private static ReconnConf reconnConf = new ReconnConf(5000L);

	// 一组连接共用的上下文对象
	private ClientGroupContext clientGroupContext = null;

	private TioClient tioClient = null;
	private ClientChannelContext clientChannelContext = null;

	/**
	 * 连接AGV服务器
	 * @throws Exception
	 */
	public void connect() throws Exception {
		Node serverNode = new Node(conf.get("io.socket.host"), conf.getInt("io.socket.port"));
		clientGroupContext = new ClientGroupContext(messageClientAioHandler, aioListener, reconnConf);
		clientGroupContext.setHeartbeatTimeout(AGVClient.TIMEOUT);
		tioClient = new TioClient(clientGroupContext);
		clientChannelContext = tioClient.connect(serverNode);
		if(!clientChannelContext.isClosed){
			log.info("连接服务器成功：" + conf.get("io.socket.host") + ":" + conf.getInt("io.socket.port"));
		}
	}
	
	/**
	 * 初始化服务 (websocket/bettery)
	 */
	public void initService(ServerGroupContext wsGroupCtx, TaskService taskService) {
		messageClientAioHandler.setWsGroupCtx(wsGroupCtx);
		messageClientAioHandler.setMessageClient(this);
		messageClientAioHandler.setTaskService(taskService);
	}

	/**
	 * 初始化系统配置
	 */
	public void initConfig() {
		String chargeFullMaxVal = conf.get("charge.full_max_val");
		String chargeLowerMinVal = conf.get("charge.lower_min_val");
		String chargeRecoverMinVal = conf.get("charge.recover_min_val");

		if (Strings.isNotBlank(chargeFullMaxVal) && Strings.isNumber(chargeFullMaxVal)) {
			AGVClient.CHARGE_FULL_MAX_VAL = Integer.parseInt(chargeFullMaxVal);
		}
		if (Strings.isNotBlank(chargeLowerMinVal) && Strings.isNumber(chargeLowerMinVal)) {
			AGVClient.CHARGE_LOWER_MIN_VAL = Integer.parseInt(chargeLowerMinVal);
		}
		if (Strings.isNotBlank(chargeRecoverMinVal) && Strings.isNumber(chargeRecoverMinVal)) {
			AGVClient.CHARGE_RECOVER_MIN_VAL = Integer.parseInt(chargeRecoverMinVal);
		}
		
		log.infof("系统配置:饱和电量：%d,最低电量：%d,任务电量:%d", AGVClient.CHARGE_FULL_MAX_VAL, AGVClient.CHARGE_LOWER_MIN_VAL, AGVClient.CHARGE_RECOVER_MIN_VAL);
	}
	
	/**
	 * 暂停任务
	 * 
	 * @param message
	 * @return
	 */
	public synchronized Result pause() {
		String message = AGVClient.createPauseCommad();
		if (clientChannelContext == null || clientChannelContext.isClosed) {
			log.error("正在恢复命令：但服务器尚未连接成功");
			return Result.error("服务器连接失败");
		}

		MessagePacket packet = new MessagePacket();
		try {
			packet.setBody(message.getBytes(MessagePacket.CHARSET));
		} catch (UnsupportedEncodingException e) {
			return Result.error("恢复命令发送失败：" + e.getMessage());
		}
		Tio.send(clientChannelContext, packet);
		log.info("正在发送命令：" + message);
		return Result.success("恢复命令发送成功!");
	}
	
	/**
	 * 恢复任务
	 * 
	 * @param message
	 * @return
	 */
	public synchronized Result recover() {
		String message = AGVClient.createRecCommad();
		if (clientChannelContext == null || clientChannelContext.isClosed) {
			log.error("正在恢复命令：但服务器尚未连接成功");
			return Result.error("服务器连接失败");
		}

		MessagePacket packet = new MessagePacket();
		try {
			packet.setBody(message.getBytes(MessagePacket.CHARSET));
		} catch (UnsupportedEncodingException e) {
			return Result.error("恢复命令发送失败：" + e.getMessage());
		}
		Tio.send(clientChannelContext, packet);
		log.info("正在发送命令：" + message);
		return Result.success("恢复命令发送成功!");
	}
	
	/**
	 * 结束任务
	 * 
	 * @param message
	 * @return
	 */
	public synchronized Result end() {
		String message = AGVClient.createEndCommad();
		if (clientChannelContext == null || clientChannelContext.isClosed) {
			log.error("正在结束命令：但服务器尚未连接成功");
			return Result.error("服务器连接失败");
		}

		MessagePacket packet = new MessagePacket();
		try {
			packet.setBody(message.getBytes(MessagePacket.CHARSET));
		} catch (UnsupportedEncodingException e) {
			return Result.error("结束命令发送失败：" + e.getMessage());
		}
		Tio.send(clientChannelContext, packet);
		log.info("结束任务：：" + message);
		return Result.success("结束命令发送成功!");
	}

	/**
	 * 发送任务
	 * 
	 * @param message
	 * @return
	 */
	public synchronized Result send(String taskName) {
		
		if(!AGVClient.isTaskValid(taskName)) {
			log.errorf("无效任务,放弃发送：%s", taskName);
			return Result.error();
		}
		
		taskName = AGVClient.createSendCommad(taskName);
		if (clientChannelContext == null || clientChannelContext.isClosed) {
			log.error("无法发送命令，服务器连接中断");
			return Result.error("服务器连接中断");
		}

		AGVStatus status = AGVStatusCacheClient.getInstance().get(AGVClient.ONE_AVG_ID);
		if(!status.isFinished()) {
			log.errorf("尚有进行中的任务，无法发送新任务：%s ", status.getTaskName());
			return Result.error("尚有进行中的任务，无法发送新任务：%s" + status.getTaskName() + "，设备电量剩余：" + status.getBattery());
		}

		MessagePacket packet = new MessagePacket();
		try {
			packet.setBody(taskName.getBytes(MessagePacket.CHARSET));
		} catch (UnsupportedEncodingException e) {
			return Result.error("任务发送失败：" + e.getMessage());
		}
		boolean success = Tio.send(clientChannelContext, packet);
		
		if(success) {
			log.info("任务发送成功：" + taskName);
			return Result.success("任务发送成功");
		}
		return Result.error();
	}

}
