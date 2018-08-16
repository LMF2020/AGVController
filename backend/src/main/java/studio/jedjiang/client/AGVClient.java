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
	public static final String DEFAULT_DB_TASK_ID = "AGVUniqueId_dde94b0e9e87f376efd80c30";

	// 缓存任务列表(后期采用LRU模型, 如果是多任务需要引入memcached)
	public static AGVStatusCacheClient agvCacheClient = AGVStatusCacheClient.getInstance();

	
	// 定子，转子，返仓
	public static final String DZ_NUM = "1";
	public static final String ZZ_NUM = "2";
	public static final String FC_NUM = "0";
	
	// 定子，转子，返仓
	public static final String DZ_FLAG = "D";
	public static final String ZZ_FLAG = "Z";
	public static final String FC_FLAG = "F";
	
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
	public static AGVStatus updateCache(String agvResponse) {

		if (Strings.isBlank(agvResponse)) {
			log.error("报文解析错误：xml empty error");
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
	 * 获取回待命区的指令 
	 */
	// for example : Z3221
	public static String getCmdFromSiteToStartSite(String fromSite){
		StringBuilder cmd = new StringBuilder();
		// 定子转子回待命区 SXX00, 返仓回待命区 S6070
		fromSite = fromSite.replace(".xml", "").trim();
		// 截取后两位 :21
		String target = fromSite.substring(fromSite.length() - 2);
		// 截取后一位:1
		String type = target.substring(target.length() - 1);
		// 如果是定子，转子
		if(type.equals(DZ_NUM) || type.equals(ZZ_NUM)){
			cmd.append("S").append(target).append("00");
		}else if(type.equals(FC_NUM)){
			// 如果是返仓
			cmd.append("S6070");
		}
		return cmd.toString();
	}
	
	/**
	 * 根据类型获取前缀
	 */
	public static String getPrefixByType(String type){
		if(type.equals(DZ_NUM)){
			return DZ_FLAG;
		}
		if(type.equals(ZZ_NUM)){
			return ZZ_FLAG;
		}
		if(type.equals(FC_NUM)){
			return FC_FLAG;
		}
		return null;
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
	
	public static void main(String[] args) {
		String word = "Sty679";
		System.out.println(word.substring(word.length() - 1));
	}

}
