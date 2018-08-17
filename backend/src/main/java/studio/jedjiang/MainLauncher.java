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
			// 配置websocket服务|任务服务
			messageClient.initService(appStarter.getServerGroupContext(), taskService);
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
	@At("/task/add/?/?")
	@Ok("json") // site - 站点(1-5), type - 返仓,转子,定子
	public Result addTask(String site, String type) {
		try {
			
			// 逻辑： 1.优先查找待办任务
				//  2. 若找不到，查找进行中的任务，
				//  3. 若找不到，查最新完成的任务
				//  4. 再找不到认为车子就在待命区(1)
				//  5. 新增任务
				//  6. 取任务并发送
				//  7. 更新任务状态
			
			// 获取前缀 D/Z/F
			String prefix = AGVClient.getPrefixByType(type);
			String fromSite = "";
			String targetSite = site + type;
			boolean find = false;
			// 1. 查找前一个待办任务
			Task lastTask = taskService.findNext();
			if(lastTask != null){
				String lastTaskName = lastTask.getName();
				// 后两位就是起始站点
				fromSite = lastTaskName.substring(lastTaskName.length() - 2);
				log.infof("前一个待办任务:%s, 算出起始站点:%s", lastTaskName, fromSite);
				find = true;
			}
			
			// 2. 查找是否有执行中的任务
			if(!find){
				lastTask = taskService.getOngoingTask();
				if(lastTask!=null){
					String lastTaskName = lastTask.getName();
					// 后两位就是起始站点
					fromSite = lastTaskName.substring(lastTaskName.length() - 2);
					log.infof("有执行中任务:%s, 算出起始站点:%s", lastTaskName, fromSite);
					find = true;
				}
			}
			
			// 3. 查找前一个最新完成的任务
			if(!find){
				lastTask = taskService.getLatestFinished();
				if(lastTask!=null){
					String lastTaskName = lastTask.getName();
					// 后两位就是起始站点
					fromSite = lastTaskName.substring(lastTaskName.length() - 2);
					log.infof("前一个完成的任务:%s, 算出起始站点:%s", lastTaskName, fromSite);
					find = true;
				}
			}
			// 3. 此时认为车子在待命区(1)
			if(!find){
				log.info("找不到任务，算出起始站点：00");
				fromSite = "00";
			}
			// 推算出任务名
			String _thisTask = prefix + fromSite + targetSite;
			// 添加任务 (到数据库)
			taskService.add(_thisTask);
			log.infof("任务:%s, 已添加入库", _thisTask);

			// 检查数据库是否有执行中的任务
			Task ongoingTask = taskService.getOngoingTask();
			// 如果没有执行中的任务，则发送待办任务
			if(ongoingTask == null){
				Task nextTask = taskService.findNext();
				// 因为此方法就是添加待办任务，所以数据库必然有一个待办任务，而且就是 _thisTask = (nextTask)
				messageClient.send(nextTask.getName());
				// 更新任务状态为，执行中
				nextTask.setStatus(Task.TASK_IN_PROCESS);
				taskService.update(nextTask);
				log.infof("没有执行中的任务, 发送任务：%s", nextTask.getName());
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
	@At("/task/reset")
	@Ok("json")
	public Result reset() {
		try {
			taskService.clearAll();
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
