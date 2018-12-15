package studio.jedjiang;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.nutz.boot.NbApp;
import org.nutz.dao.Dao;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Encoding;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.filter.CrossOriginFilter;

import com.google.common.collect.ImmutableMap;

import cn.hutool.core.thread.ThreadUtil;
import studio.jedjiang.bean.AGVStatus;
import studio.jedjiang.bean.Result;
import studio.jedjiang.bean.Task;
import studio.jedjiang.client.AGVClient;
import studio.jedjiang.client.MessageClient;
import studio.jedjiang.license.LicenseProcessor;
import studio.jedjiang.license.LicenseUtil;
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
	
	Map<String, String> GetTask = ImmutableMap.<String, String>builder().put("T", "叫料任务,").put("F", "返仓任务,")
			.put("S", "待命任务,").put("O", "充电任务,").build();

	Map<String, String> GetGeo = ImmutableMap.<String, String>builder().put("00", "(00)待命").put("11", "E(1号位)")
			.put("12", "E(2号位)").put("21", "D(1号位)").put("22", "D(2号位)").put("31", "C(1号位)").put("32", "C(2号位)")
			.put("41", "B(1号位)").put("42", "B(2号位)").put("51", "A(1号位)").put("52", "A(2号位)").put("61", "机加(1号位)")
			.put("62", "机加(2号位)").put("70", "仓库(0号位)").put("71", "仓库(1号位)").put("72", "仓库(2号位)").put("80", "(80)待命")
			.put("81", "充电桩").build();
	

	public void init() throws Exception {
		// 环境检查
		if (!Charset.defaultCharset().name().equalsIgnoreCase(Encoding.UTF8)) {
			log.warn("This project must run in UTF-8, pls add -Dfile.encoding=UTF-8 to JAVA_OPTS");
		}
		
		// 读取并验证授权
		LicenseUtil.readLocalLicense();

		// 初始化数据表
		initDataSource();

		try {
			// 启动websocket服务
			appStarter = new WsMsgStarter(Integer.parseInt(conf.get("io.ws.port")), new WsMsgHandler());
			appStarter.start();
			// 配置websocket服务|任务服务
			messageClient.initService(appStarter.getServerGroupContext(), taskService);
			// 初始化系统配置
			messageClient.initConfig();
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
	
//	@Filters({@By(type = CrossOriginFilter.class), @By(type=LicenseProcessor.class)})
//	@At("/cmd/task/create/?")
//	@Ok("json")
//	public Result sendCommand(String command) {
//		try {
//			return messageClient.send(command);
//		} catch (Exception e) {
//			return Result.error(e.getMessage());
//		}
//	}

	@Filters(@By(type = CrossOriginFilter.class))
	@At("/cmd/task/end")
	@Ok("json")
	public Result endCommand() {
		try {
			Task ongoingTask = taskService.getOngoingTask();
			if(ongoingTask == null) {
				return  Result.error("没有进行中的任务");
			}
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

	@Filters(@By(type = CrossOriginFilter.class))
	@At("/cmd/task/pause")
	@Ok("json")
	public Result pauseCommand() {
		try {
			return messageClient.pause();
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
			
			convertTaskList(beanList);
			
			return Result.success("", beanList);
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}
	}
	
	private void convertTaskList(List<Task> beanList) {
		for (Task t : beanList) {
			String name = t.getName();
			if (Strings.isNotBlank(name)) {
				// F801172
				StringBuilder sb = new StringBuilder();
				String n = name.substring(0, 1);
				String from = name.substring(1, 3);
				String to = name.substring(5);
				String uname = sb.append(GetTask.get(n)).append("从").append(GetGeo.get(from)).append("到").append(GetGeo.get(to)).toString();
				t.setUname(uname);
			}
		}
	}

	// 手工任务
	@Filters({ @By(type = CrossOriginFilter.class), @By(type = LicenseProcessor.class) })
	@At("/task/send/manual/?")
	@Ok("json")
	public Result sendManualTask(String taskName) {
		try {
			Task ongoingTask = taskService.getOngoingTask();
			if (ongoingTask != null) {
				return Result.error("执行手工任务前，请先结束进行中的任务");
			}
			Result r = messageClient.send(taskName);
			if (r.getCode() == 0) {
				taskService.addByStatus(taskName, Task.TASK_IN_PROCESS);
			}
			return Result.success("手工任务发送成功!");
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}
	}

	// 添加任务并检查是否需要发送任务
	@Filters({@By(type = CrossOriginFilter.class), @By(type=LicenseProcessor.class)})
	@At("/task/add/?")
	@Ok("json") // site - 站点(1-5), type - 返仓,转子,定子
	public Result addTask(String taskName) {
		try {
			
			// 任务分解 = 动作(1位) + 起始仓库(2位) + 目标仓库(2位)
			// 逻辑： 1.优先查找待办任务
				//  2. 若找不到，查找进行中的任务，
				//  3. 若找不到，查最新完成的任务
				//  4. 再找不到认为车子就在待命区(1)
				//  5. 新增任务
				//  6. 取任务并发送
				//  7. 更新任务状态
			
			String prefix = String.valueOf(taskName.charAt(0));
			String fromSite = "";
			String targetSite = taskName.substring(1);
			boolean find = false;
			// 1. 查找前一个待办任务
			Task prevTask = taskService.findLastTodo();
			if(prevTask != null){
				String prevTaskName = prevTask.getName();
				// 后(2)位作为起始点
				fromSite = prevTaskName.substring(prevTaskName.length() - 2);
				log.infof("发现有待办任务:%s, 设置起始位置:%s", prevTaskName, fromSite);
				find = true;
			}
			
			// 2. 查找是否有进行中的任务
			if(!find){
				prevTask = taskService.getOngoingTask();
				if(prevTask!=null){
					String lastTaskName = prevTask.getName();
					// 后(2)位作为起始点
					fromSite = lastTaskName.substring(lastTaskName.length() - 2);
					log.infof("发现有进行中的任务:%s, 设置起始位置:%s", lastTaskName, fromSite);
					find = true;
				}
			}
			
			// 3. 查找前一个最新完成的任务
			if(!find){
				prevTask = taskService.getLatestFinished();
				if(prevTask!=null){
					String lastTaskName = prevTask.getName();
					// 后两位就是起始站点
					fromSite = lastTaskName.substring(lastTaskName.length() - 2);
					log.infof("发现已完成任务:%s, 计算起始位置:%s", lastTaskName, fromSite);
					find = true;
				}
			}
			
			// 4. TODO: 查找服务器最后一次上报的任务
			int tryTimes = 5;
			while(!find && tryTimes > 0){
				AGVStatus lastAgvStatus = AGVClient.agvCacheClient.get(AGVClient.ONE_AVG_ID);
				try {
					if(lastAgvStatus!=null && lastAgvStatus.isFinished()) {
						String lastTaskName = lastAgvStatus.getTaskName();
						
						if(AGVClient.isTaskValid(lastTaskName)) {
							fromSite = lastTaskName.replace(".xml", "").substring(lastTaskName.length() - 2);
							log.infof("发现服务器最后完成任务是:%s, 设置起始位置:%s", lastTaskName, fromSite);
							find = true;
						}
					}
				} catch (Exception e) {
					log.error("无法获取服务器最后一次任务");
				} finally {
					ThreadUtil.safeSleep(500);
					tryTimes--;
				}
			}
			
			// 5. 此时认为车子在待命区(1)
			if(!find){
				log.info("无法分析任务的起始位置，设置起始位置：80");
				fromSite = "80";
			}
			
			// 6. 10,20,30,40,50结尾的都以60开头
			// fromSite = AGVClient.updateFromSite(fromSite);
			
			// 推算出任务名
			String _thisTask = prefix + fromSite + targetSite;
			// 添加任务 (到数据库)
			taskService.add(_thisTask);

			// 检查数据库是否有进行中的任务
			Task ongoingTask = taskService.getOngoingTask();
			// 如果没有进行中的任务，则发送待办任务
			if(ongoingTask == null){
				Task nextTask = taskService.findNext();
				// 数据库保证至少有一个待办任务，有可能是刚添加的，也有可能是后来添加的
				log.infof("未发现进行中的任务，所以发送待办任务：%s", nextTask.getName());
				Result r = messageClient.send(nextTask.getName());
				if(r.getCode() == 0) {
					nextTask.setStatus(Task.TASK_IN_PROCESS);
					taskService.update(nextTask);
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
			Task ongoingTask = taskService.getOngoingTask();
			if (ongoingTask != null) {
				return Result.error("请先结束进行中的任务.");
			}
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
			taskService.deleteTodo(id);
			return Result.success();
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}
	}

	@Filters(@By(type = CrossOriginFilter.class))
	@At("/task/clearTodo")
	@Ok("json")
	public Result clearTodoTask() {
		try {
			taskService.clearTodo();
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
