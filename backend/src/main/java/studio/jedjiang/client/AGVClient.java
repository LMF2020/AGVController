package studio.jedjiang.client;

import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import studio.jedjiang.bean.AGVStatus;

public class AGVClient {

	private static final Log log = Logs.get();
	public static String ONE_AVG_ID = "ONLY_FOR_ONE_AVG";

	public static final String SEND_CMD_PATTERN = "cmd=set task by name;name={CODE}.xml";
	public static final String END_CMD_PATTERN = "cmd=pause;pauseStat=1";
	public static final String REC_CMD_PATTERN = "cmd=pause;pauseStat=0";
	public static final String DEFAULT_DB_TASK_ID = "WCNMWCNMdde94b0e9e87f376efd80c30";

	// 缓存任务列表(后期采用LRU模型, 如果是多任务需要引入memcached)
	public static AGVStatusCacheClient agvCacheClient = AGVStatusCacheClient.getInstance();

	/**
	 * 检测心跳超时时间
	 */
	public static final int TIMEOUT = 5000;

	/**
	 * 发送的报文
	 */
	public static String createSendCommad(String reqCode) {
		int pos = reqCode.indexOf(".xml");
		if(pos != -1) {
			reqCode = reqCode.substring(0, pos);
		}
		return SEND_CMD_PATTERN.replace("{CODE}", reqCode);
	}

	/**
	 * 创建结束命令
	 */
	public static String createEndCommad() {
		return END_CMD_PATTERN;
	}

	/**
	 * 创建恢复执行的命令
	 */
	public static String createRecCommad() {
		return REC_CMD_PATTERN;
	}
	
	/**
	 * 解析返回报文
	 * 
	 * @param agvResponse 服务器返回的报文
	 * @return
	 */
	public static AGVStatus updateAGVStatus(String agvResponse) {

		if (Strings.isBlank(agvResponse)) {
			log.error("报文解析错误：null error");
			return null;
		}
		if (!agvResponse.contains("cmd=") || !agvResponse.contains("battery=") || !agvResponse.contains("task_isfinished=")
				|| !agvResponse.contains("task=")) {
			log.error("报文解析错误：format() error：" + agvResponse);
			return null;
		}

		AGVStatus me = null;
		try {
			me = AGVStatus.ofme(agvResponse);
		} catch (Exception e) {
			log.error("报文解析错误：ofme() error：" + agvResponse);
			return null;
		}
		
		agvCacheClient.put(ONE_AVG_ID, me);

		log.info("报文解析成功: 当前任务为=>" + me.getTaskName());

		return me;
	}

	/**
	 * 打印错误堆栈信息
	 */
	public static String getStackMsg(Throwable e) {

		StringBuffer sb = new StringBuffer();
		StackTraceElement[] stackArray = e.getStackTrace();
		for (int i = 0; i < stackArray.length; i++) {
			StackTraceElement element = stackArray[i];
			sb.append(element.toString() + "\n");
		}
		return sb.toString();
	}

}
