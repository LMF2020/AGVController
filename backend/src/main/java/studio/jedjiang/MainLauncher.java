package studio.jedjiang;

import java.nio.charset.Charset;
import java.util.List;

import org.nutz.boot.NbApp;
import org.nutz.dao.Dao;
import org.nutz.dao.util.Daos;
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

import com.google.common.collect.Lists;

import studio.jedjiang.bean.AGVStatus;
import studio.jedjiang.bean.Result;
import studio.jedjiang.bean.Task;
import studio.jedjiang.client.AGVClient;
import studio.jedjiang.client.MessageClient;
import studio.jedjiang.service.TaskService;
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
	
	@Inject
	protected Dao dao;

	@Inject
	protected TaskService taskService;

	public void init() {
		// 环境检查
		if (!Charset.defaultCharset().name().equalsIgnoreCase(Encoding.UTF8)) {
			log.warn("This project must run in UTF-8, pls add -Dfile.encoding=UTF-8 to JAVA_OPTS");
		}

		// 初始化数据表
		initDataSource();

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
	
	private void initDataSource() {
		// 环境检查
		if (!Charset.defaultCharset().name().equalsIgnoreCase(Encoding.UTF8)) {
			log.warn("This project must run in UTF-8, pls add -Dfile.encoding=UTF-8 to JAVA_OPTS");
		}
		Daos.createTablesInPackage(dao, "studio.jedjiang.bean", false);
		Daos.migration(dao, "studio.jedjiang.bean", true, false);

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

	// 查询任务列表
	@Filters(@By(type = CrossOriginFilter.class))
	@At("/task/list")
	@Ok("json")
	public Result listTask() {
		try {
			List<Task> beanList = taskService.listAll();
			return Result.success("", beanList);
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}
	}

	// 添加任务并检查是否需要发送任务
	@Filters(@By(type = CrossOriginFilter.class))
	@At("/task/add/?")
	@Ok("json")
	public Result addTask(String taskName) {
		try {
			// 添加任务
			taskService.add(taskName);
			// 当前没有任务或者任务已完成就从数据库找一个任务发送
			AGVStatus agvStatus = AGVClient.agvCacheClient.get(AGVClient.ONE_AVG_ID);
			if(agvStatus == null || agvStatus.isFinished()){
				Task task = taskService.findNext();
				if(task != null){
					messageClient.send(task.getName());
					// 更新任务状态为：执行中
					task.setStatus(Task.TASK_IN_PROCESS);
					taskService.update(task);
				}
			}

			return Result.success();
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}
	}

	@Filters(@By(type = CrossOriginFilter.class))
	@At("/task/clear")
	@Ok("json")
	public Result clearTask() {
		try {
			taskService.clearAll();
			return Result.success();
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}
	}

	@Filters(@By(type = CrossOriginFilter.class))
	@At("/task/delete/?")
	@Ok("json")
	public Result deleteTask(String id) {
		try {
			taskService.delete(Lists.newArrayList(id));
			return Result.success();
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}
	}

	@Filters(@By(type = CrossOriginFilter.class))
	@At("/task/clearFinished")
	@Ok("json")
	public Result clearFinishedTask() {
		try {
			taskService.clearFinished();
			return Result.success();
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}
	}

	@Filters(@By(type = CrossOriginFilter.class))
	@At("/task/top/?")
	@Ok("json")
	public Result topTask(String id) {
		try {
			taskService.top(id);
			return Result.success();
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
