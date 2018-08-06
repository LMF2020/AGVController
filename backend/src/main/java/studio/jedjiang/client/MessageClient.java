package studio.jedjiang.client;

import java.io.UnsupportedEncodingException;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
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
	 * 连接服务器
	 * 
	 * @throws Exception
	 */
	public void connect() throws Exception {
		Node serverNode = new Node(conf.get("io.socket.host"), conf.getInt("io.socket.port"));
		clientGroupContext = new ClientGroupContext(messageClientAioHandler, aioListener, reconnConf);
		clientGroupContext.setHeartbeatTimeout(AGVClient.TIMEOUT);
		tioClient = new TioClient(clientGroupContext);
		clientChannelContext = tioClient.connect(serverNode);

		log.info("正在尝试连接服务器：" + conf.get("io.socket.host") + ":" + conf.getInt("io.socket.port"));
	}
	
	/**
	 * 配置websocket服务
	 * @param wsGroupCtx
	 */
	public void setWebsocketGroupCtx(ServerGroupContext wsGroupCtx) {
		messageClientAioHandler.setWsGroupCtx(wsGroupCtx);
	}

	/**
	 * 恢复执行任务
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
		log.info("正在结束命令：：" + message);
		return Result.success("结束命令发送成功!");
	}

	/**
	 * 发送任务
	 * 
	 * @param message
	 * @return
	 */
	public synchronized Result send(String message) {
		message = AGVClient.createSendCommad(message);
		if (clientChannelContext == null || clientChannelContext.isClosed) {
			log.error("正在发送命令：但服务器尚未连接成功");
			return Result.error("服务器连接失败");
		}

		// 任务没结束不能发送新任务
		AGVStatus status = AGVStatusCacheClient.getInstance().get(AGVClient.ONE_AVG_ID);
		if(!status.isFinished()) {
			log.error("车载任务尚未结束： " + status.getTaskName() + "，CMD：" + status.getCmd());
			return Result.error("车载任务尚未结束：" + status.getTaskName() + "，设备电量：" + status.getBattery());
		}

		MessagePacket packet = new MessagePacket();
		try {
			packet.setBody(message.getBytes(MessagePacket.CHARSET));
		} catch (UnsupportedEncodingException e) {
			return Result.error("任务发送失败，失败原因：" + e.getMessage());
		}
		Tio.send(clientChannelContext, packet);
		log.info("正在发送命令：任务发送成功：" + message);

		// TODO: 这里有一个bug： 用户发送第一个调度命令的之后，状态还没来及上报就接着触发下一个调度命令，这时候我不知道该如何阻止用户连续发送

		// 解决方案：

		return Result.success("任务发送成功");
	}

}
