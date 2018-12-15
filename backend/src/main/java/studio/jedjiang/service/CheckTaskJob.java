package studio.jedjiang.service;

import org.nutz.integration.quartz.annotation.Scheduled;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import studio.jedjiang.client.AGVClient;

@Scheduled(cron = "*/10 * * * * ?")
@IocBean
public class CheckTaskJob implements Job {

	private static final Log log = Logs.get();

	@Inject
	protected TaskService taskService;

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			// 定时任务判断当前是否有待办任务
			AGVClient.hasNextTask = taskService.findNext() != null;
		} catch (Exception e) {
			if (AGVClient.HASLOG) {
				log.error("定时查询待办任务失败");
			}
		}

	}

}
