package studio.jedjiang;

import java.nio.charset.Charset;

import org.nutz.boot.NbApp;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Encoding;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.filter.CrossOriginFilter;

import studio.jedjiang.bean.Result;
import studio.jedjiang.client.AGVClient;
import studio.jedjiang.client.MessageClient;
import studio.jedjiang.websocket.WsMsgHandler;
import studio.jedjiang.websocket.WsMsgStarter;

/**
 * Created by Jed on 2018/08/04.
 *
 */
@IocBean(create = "init", depose = "depose")
public class MainLauncher {

	private static final Log log = Logs.get();

	private WsMsgStarter appStarter;

	@Inject
	protected PropertiesProxy conf;

	@Inject
	protected MessageClient messageClient;

	public void init() {
		// 环境检查
		if (!Charset.defaultCharset().name().equalsIgnoreCase(Encoding.UTF8)) {
			log.warn("This project must run in UTF-8, pls add -Dfile.encoding=UTF-8 to JAVA_OPTS");
		}

		// 初始化数据表
		// initDataSource();

		try {
			// 启动websocket服务
			appStarter = new WsMsgStarter(Integer.parseInt(conf.get("io.ws.port")), new WsMsgHandler());
			appStarter.start();
			// 配置websocket服务
			messageClient.setWebsocketGroupCtx(appStarter.getServerGroupContext());
			// 连接服务器
			messageClient.connect();

		} catch (Exception e) {
			log.error("连接服务器失败: " + AGVClient.getStackMsg(e));
		}

	}

	@Filters(@By(type = CrossOriginFilter.class))
	@At("/cmd/task/create/?")
	@Ok("json")
	public Result sendCommand(String command) {
		try {
			return messageClient.send(command);
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}
	}

	@Filters(@By(type = CrossOriginFilter.class))
	@At("/cmd/task/end")
	@Ok("json")
	public Result endCommand() {
		try {
			return messageClient.end();
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}
	}

	@Filters(@By(type = CrossOriginFilter.class))
	@At("/cmd/task/recover")
	@Ok("json")
	public Result recCommand() {
		try {
			return messageClient.recover();
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}
	}

	public void depose() {

	}

	public static void main(String[] args) throws Exception {
		new NbApp(MainLauncher.class).setPrintProcDoc(false).run();
	}

}
