package studio.jedjiang.client;

import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import com.google.common.collect.Lists;

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

	public static final String NO_TASK = "no_task";
	
	// 高于该值表示已充满电
	public static final int BATTERY_FULL_MAX_VAL = 99;
	// 低于该值表示需要充电
	public static final int BATTERY_LOWER_MIN_VAL= 30;
	
	// 定子，转子，返仓
	public static final String DZ_NUM = "1";
	public static final String ZZ_NUM = "2";
	public static final String FC_NUM = "0";
	public static final String CHARGE_NUM = "71";
	
	// 定子，转子，返仓
	public static final String DZ_FLAG = "D";
	public static final String ZZ_FLAG = "Z";
	public static final String FC_FLAG = "F";
	
	// 需要改变起始点的站点
	public static final List<String> NEED_CHANGED_FROM_SITE = Lists.newArrayList("10","20","30","40","50");
	
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
			e.printStackTrace();
			return null;
		}
		
		agvCacheClient.put(ONE_AVG_ID, me);

		// log.infof("报文解析成功: 当前任务:%s",me.getTaskName());

		return me;
	}

	/**
	 * 获取回待命区的指令 
	 */
	// for example : Z3221
	public static String getCmdFromSiteToBackSite(String fromSite){
		StringBuilder cmd = new StringBuilder();
		// 定子转子回待命区 SXX00, 返仓回待命区 S6070, 充电回待命区S7170
		fromSite = fromSite.replace(".xml", "").trim();
		// 截取后两位 :21
		String target = fromSite.substring(fromSite.length() - 2);
		// 充电返仓
		if(target.equals(CHARGE_NUM)){
			return "S7170";
		}
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
	 * 获取当前站点到充电站的任务
	 */
	public static String getFromSiteToChargeSite(String fromSite){
		fromSite = fromSite.replace(".xml", "").trim().substring(fromSite.length() -2);
		return "O" + fromSite + "71";
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
	 * 满足以下条件，改变待办任务的起始点：10,20,30,40,50结尾的都以60开头
	 */
	public static String updateFromSite(String fromSite) {
		if(NEED_CHANGED_FROM_SITE.contains(fromSite)) {
			fromSite = "60";
		}
		return fromSite;
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
		// String word = "Sty679";
		// System.out.println(word.substring(2));
				
		
		
	}

}
